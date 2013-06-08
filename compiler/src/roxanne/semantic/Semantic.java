package roxanne.semantic;

import java.util.Iterator;
import java.util.LinkedList;

import roxanne.ast.Expr.OpType;
import roxanne.types.*;
import roxanne.env.Env.Mode;
import roxanne.ast.*;
import roxanne.ast.Character;
import roxanne.ast.Void;
import roxanne.symbol.Symbol;
import roxanne.env.*;
import roxanne.error.Error;
import roxanne.codegen.*;

/*
 * addblock??
 */
public class Semantic {

	private DefList program;
	private Env env;
	private LinkedList<Symbol> funcs;
	
	enum BMode {
		NEWSCOPE, NO
	}
	private enum IntMode{
		CONV, CALC		// CONV for convert
	}
	private enum TMode {
		DEC, USE
	}
	private enum DMode {FUNC,VAR, TYPEDEFNAME, FUNCDEF,FUNCDEC,NULL, NOTHING, DEFED}		// def mode for checkDefinable, if var return nothing,which cannot be used;
	public Semantic() {
		program = null;
		env = null;
		funcs = new LinkedList<Symbol>();
	}

	
	public void trans(DefList deflist) throws Error{
		program = deflist;
		env = new Env();

		translate(program);
		checkUndefinedFuncs();
		//env.print();
	}
	
	boolean isDefinedFunc(Symbol name) {
		FuncEntry e = (FuncEntry) env.get(name, Mode.USE);
		if (e.block == null) return false;
		return true;
	}
	
	private void checkUndefinedFuncs() throws Error {
		Iterator<Symbol> iter = funcs.iterator();
		Symbol s = null;
		while(iter.hasNext()) {
			s = iter.next();
			if (!isDefinedFunc(s)) throw new Error("function "+s+" is used but undefined");
		}
	}
	
	public void translate(DefList p) throws Error{
		if (p == null) return;
		//System.out.println("in deflist");
		//env.print();
		
		translate(p.head);
		translate(p.tail);
	}
	private void translate(Def d) throws Error{
		if (d instanceof Dec) {
			translate((Dec) d, null);
		} else if (d instanceof TypeDef) {
			translate((TypeDef) d);
		} else if (d instanceof FuncDef) {
			translate((FuncDef) d);
		}
	}
	
	private void translate(TypeDef td) throws Error {
		TYPE t = translateTy(td.type, TMode.USE);
		if (t instanceof STRUCTUNION)
			td.su = (STRUCTUNION)t;
		translateInDec(t, td.list, null);		
	}
	
	private PARAS createParas(ParaList p) throws Error {
		PARAS paras = new PARAS(new RECORD());
		env.beginScope();
		translateInDec(paras, p);
		//System.out.println(paras.toString());
		env.endScope();
		if (paras.paras.size() == 0) paras.paras = null;
		return paras;
	}
	
	private void translate(FuncDef fd) throws Error{
		//struct union ignored use createandcheckType instead
		/*roxanne.types.Type t = translateTy(fd.returntype);
			Pack p = translateInDec(t, fd.f);
			p = checkDefinable(p);
			p.idtype = new Func(p.idtype, null);
			env.put(p);
			env.beginScope();
				env.return = new FuncBinder(p.idtype, env.return);
				Paras paras = new Paras();
				translateParaList(fd.paralist, paras);
				env.addParas(pp.sym, paras);
				env.put(paras);?
				translate(fd.block, BMode.NO);
			translate()
			env.endScope();
			env.func = env.return.prev;
		 */
		TYPE t = translateTy(fd.returntype, TMode.USE), returnty;
		Pack p = translateInDec(t, fd.f);
		returnty = p.idtype;
		PARAS paras = null;
		if (fd.paras == null) paras = new PARAS();
		else{
			paras = createParas(fd.paras);
		}
		p.idtype = new FUNC(p.idtype, paras);
		fd.pack = p;
		DMode m = checkDefinable(p, DMode.FUNC);
		if (m == DMode.FUNCDEF)	throw new Error("func redefined");
		FuncEntry fe = new FuncEntry((FUNC)p.idtype, fd.block);
		if (m == DMode.NULL)
			env.put(p.sym,fe);
		else if (m == DMode.FUNCDEC) env.addBlock(p.sym, fe.block);
		env.beginScope();
		env.pushReturn(returnty);
		env.put(paras);
		translate(fd.block, BMode.NO);				
		env.endScope();
		env.popReturn();
	}
	
	private roxanne.types.TYPE translate(Block b, BMode m) throws Error {
		if (m == BMode.NEWSCOPE) env.beginScope();
		translate(b.decs);
		translate(b.stmts);
		if (m == BMode.NEWSCOPE) env.endScope();
		return roxanne.types.VOID.getInstance();
	}
	
	private void translate(StmtList stmts) throws Error {
		if (stmts == null) return;
		
		TYPE t = translate(stmts.head);
		translate(stmts.tail);
	}
	
	private roxanne.types.TYPE translate(While s) throws Error {
		++env.loop;
			roxanne.types.TYPE t = translate(s.cond);
			if (!toInt(t, IntMode.CONV)) throw new Error("While's condition can not be converted to bool");
			translate(s.yes);
		--env.loop;
		return VOID.getInstance();
	}
	
	private roxanne.types.VOID translate(For s) throws Error {
		if (s.e1!=null)
			translate(s.e1);
		if (s.e2!=null) {
			TYPE t = translate(s.e2);
			if (!toInt(t,IntMode.CONV)) throw new Error("For's condition is not bool");
		}
		if (s.e3!=null)
			translate(s.e3);
		++env.loop;
			if (s.yes != null)
				translate(s.yes);
		--env.loop;
		return roxanne.types.VOID.getInstance();
	}
	
	private roxanne.types.VOID translate(Break s) throws Error {
		if (env.loop <= 0) throw new Error("break not in any loop");
		return roxanne.types.VOID.getInstance();
	}
	
	private roxanne.types.VOID translate(Continue s) throws Error {
		if (env.loop <=0) throw new Error("Continue not in any loop");
		return roxanne.types.VOID.getInstance();
	}
	
	public static boolean toInt(roxanne.types.TYPE t, IntMode m) {
		if (t instanceof roxanne.types.INT
				|| t instanceof roxanne.types.CHAR) 
			return true;
		if (m == IntMode.CONV) 
			if (t instanceof POINTER || t instanceof ARRAY) return true;
		
		return false;
	}
	private VOID translate(Return s) throws Error {
		if (env.returnty == null) throw new Error(" return not in any function");
		if (s.values == null) {
			if (!env.returnty.type.equals(VOID.getInstance())) throw new Error("return type should be void");
			return VOID.getInstance();					
		}
		if (env.returnty.type instanceof VOID) throw new Error("return type is  void");
		roxanne.types.TYPE t = translate(s.values);
		if (!convertable(t, env.returnty.type)) throw new Error("return type should match");
		return VOID.getInstance();
	}
	
	private roxanne.types.VOID translate(If s) throws Error {
		try {
			roxanne.types.TYPE t = translate(s.cond);
			if (!toInt(t,IntMode.CONV)) throw new Error("if's condition is not bool");
			translate(s.yes);
			if (s.no != null) translate(s.no);
			return roxanne.types.VOID.getInstance();
		} catch (Error e) {
			throw e;
		}
	}
	
	private roxanne.types.INT translate(Num n) {
		return roxanne.types.INT.getInstance();
	}
	
	private roxanne.types.CHAR translate(Character c) {
		return roxanne.types.CHAR.getInstance();
	}
	
	private ARRAY translate(Str s) {
		return new ARRAY(roxanne.types.CHAR.getInstance(), s.string.length());
	}
	
	private TYPE translate(Var v) throws Error {
		/*
		 * check defined and is not a func nor typedefname
		 * return it's type
		 */
		Entry entry = env.get(Symbol.symbol(v.name), Mode.USE);
		if (entry == null) throw new Error("variable "+v.name+" undefined");
		if (entry instanceof VarEntry) return ((VarEntry) entry).type;
		throw new Error(v.name + " can't be resolved to be a variable");
	}
	private boolean isLV(Expr e) {
		/*System.out.println(e);
		if (e instanceof ExprList) {
			for (ExprList el = (ExprList) e; el != null; el = el.tail) {
				System.out.println("elhead: "+el.head);
				System.out.println("elhead.e: "+((UExpr)el.head).e+" "+((UExpr)el.head).op);
			}
		}*/
		if (e instanceof Var) return true;
		if (e instanceof BExpr) {
			OpType op = ((BExpr) e).op;
			if ((op == OpType.ARRAY 
					|| op == OpType.DOT
					|| op == OpType.PTR) && !(e.type instanceof ARRAY))
				return true;
			return false;
		} else if (e instanceof UExpr && ((UExpr)e).op == OpType.GET) {
			//System.out.println(((UExpr)e).op);
			return true;
		}
		return false;
	}
	
	private TYPE isFieldOf(String s, roxanne.types.STRUCTUNION su) {
		return su.isField(s);
	}
	
	public static boolean toPointer(TYPE t) {
		if(t instanceof POINTER || t instanceof ARRAY) return true;
		return false;
	}
	
	private TYPE translate(UExpr ue) throws Error {
		TYPE t;
		try {
			switch (ue.op) {
			case POS:	if (!isLV(ue.e)) throw new Error("lvalue required as unary '&' operand");
						t = translate(ue.e);
						return new POINTER(t);
			case GET:	 t = translate(ue.e);
						if (!toPointer(t)) throw new Error("pointer required as unary '*' operand");
						return ((POINTER)t).eletype;
			case SIZEOFE:	t = translate(ue.e);
							return INT.getInstance();
			case SIZEOFT:	t = translate((Type)ue.e);
							return INT.getInstance();
			case LDEC: case LINC: case RINC: case RDEC:
				t = translate(ue.e);
				if (!isLV(ue.e)) throw new Error(" not left value");
				if (!toInt(t, IntMode.CONV)) throw new Error(" operands of assignment must be convertable to int");
				return t;
			case NOT:
				t = translate(ue.e);
				if (!toInt(t, IntMode.CONV)) throw new Error("invalid operand for !");
				return INT.getInstance();
			default:
				//BITNOT, UMINUS, UPLUS, 
				t = translate(ue.e);
				if (!toInt(t, IntMode.CALC)) throw new Error("invalid operand unary operator...");
				return INT.getInstance();
			}
		} catch (Error e) {
			throw e;
		}
	}
	private TYPE checkValid(Expr e) throws Error {
		try {
			TYPE t = translate(e);
			if (t.actual() instanceof WRONG) throw new Error("arithmetic on invalid type");
			return t;
		} catch (Error err) {throw err;}
	}
	
	private TYPE translate(BExpr be) throws Error {
		try {
			roxanne.types.TYPE l = null, r=null;
			switch (be.op) {
			case CAST:
				l = translate(be.left);
				r = translate(be.right);
				if (!convertable(r,l)) throw new Error("type not convertable in cast");
				return l;
			case ASSIGN: 		//warning when 2 pointer of different type
				l = translate(be.left); 
				r = translate(be.right);
				if (!isLV(be.left)) throw new Error("the expr before = has to be a leftvalue");
				if (l instanceof VOID  || l instanceof ARRAY) throw new Error("left operand of '=' is not changeble");
				if (!convertable(r, l)) throw new Error("type inconvertable: "+/*l.toString()*/" <- "+r.toString());
				return l;
			case ARRAY:
				l = checkValid(be.left);
				r = checkValid(be.right);
				if (!toPointer(l))	throw new Error("operand before [] is not a pointer && l must of valid completely defined type");
				if (!toInt(r, IntMode.CALC))	throw new Error("index of array not convertable to int");
				return ((POINTER) l).eletype;
			case MULASSIGN: case DIVASSIGN: case MODASSIGN:  
			case ANDASSIGN: case ORASSIGN: case XORASSIGN: case SHLASSIGN: case SHRASSIGN:
				l = checkValid(be.left);
				r = checkValid(be.right);
				if (!isLV(be.left)) throw new Error(" not left value");
				if (l instanceof VOID  || l instanceof ARRAY) throw new Error("left operand of '=' is not changeble");
				if (!toInt(l, IntMode.CALC) || !toInt(r, IntMode.CALC)) throw new Error(" operands of assignment must be int");
				return l;
			case ADDASSIGN:
				l = checkValid(be.left);
				r = checkValid(be.right);
				if (l instanceof VOID  || l instanceof ARRAY) throw new Error("left operand of '=' is not changeble");
				if (!isLV(be.left)) throw new Error(" not left value");
				if (!toInt(l, IntMode.CONV) || !toInt(r, IntMode.CALC)) throw new Error(" operands of assignment must be int");
				return l;	
			case SUBASSIGN:
				l = checkValid(be.left);
				r = checkValid(be.right);
				if (l instanceof VOID  || l instanceof ARRAY) throw new Error("left operand of '=' is not changeble");
				if (!isLV(be.left)) throw new Error(" not left value");
				if (!toInt(l, IntMode.CONV) || !toInt(r, IntMode.CONV)) throw new Error(" operands of assignment must be int");
				return l;				
			case EQ: case NE:	// warning when 2 pointer not equal
				l = translate(be.left);
				r = translate(be.right);
				if (!toInt(l, IntMode.CONV) || !toInt(r,IntMode.CONV)) throw new Error("operands of == or != must be convertablet to int");
				return roxanne.types.INT.getInstance();
			case DOT:
				l = translate(be.left);
				if (!isLV(be.left)) throw new Error("operand before and after . should be left value");
				if (!(l instanceof roxanne.types.STRUCTUNION)) throw new Error("operand before . must be of type struct or union ");
				if ((r=isFieldOf(((Var)be.right).name, (roxanne.types.STRUCTUNION)l))==null) throw new Error(((Var)be.right).name+" is not a field of"+(roxanne.types.STRUCTUNION)l);
				//if multi inner struct, it's like peeling out the innner struct
				return r;
			case PTR:
				l = checkValid(be.left);
				VarEntry v = new VarEntry(null);
				if (!isLV(be.left))	throw new Error("operand before and after . should be left value");
				if (!(toPointer(l) && ((roxanne.types.POINTER) l).eletype instanceof roxanne.types.STRUCTUNION))
					throw new Error("before -> not a structunion pointer");
				roxanne.types.STRUCTUNION su = (roxanne.types.STRUCTUNION)((roxanne.types.POINTER) l).eletype;
				if ((r=isFieldOf(((Var)be.right).name,su))==null) throw new Error(((Var)be.right).name+" is not a field of "+su.name.toString());
				return r;
			case PLUS:
				boolean pl, pr;
				l = checkValid(be.left); r = checkValid(be.right);
				if (toInt(l, IntMode.CALC) && toInt(r, IntMode.CALC)) return INT.getInstance();
				if ((pl = toPointer(l))^(pr = toPointer(r))) {
					if ((pl && (l.actual() instanceof WRONG)) || (pr && (r.actual() instanceof WRONG))) throw new Error("'+': pointer in use with incompelete type");
					return pl? l:r;
				}
				throw new Error("invalid type around +");
			case MINUS:
				l = checkValid(be.left); r = checkValid(be.right);
				if (toInt(l,IntMode.CALC) && toInt(r, IntMode.CALC)) return INT.getInstance();
				if (toPointer(l) && toPointer(r)) {
					if ((l.actual() instanceof WRONG) || (r.actual() instanceof WRONG) || !l.equals(r)) throw new Error("'-': pointer in use with incompelete type");
					return INT.getInstance();
				}
				if (toPointer(l) && toInt(r, IntMode.CALC) && !(l.actual() instanceof WRONG)) return l;
				throw new Error("invalid type arround -");
			default:
				l = translate(be.left);
				r = translate(be.right);
				if (!toInt(l, IntMode.CALC) || !toInt(r, IntMode.CALC)) throw new Error("not int");
				return roxanne.types.INT.getInstance();
			}
		} catch (Error e) {
			throw e;
		}
	}
	
	private boolean isDeclaredFunc(Symbol s, FUNC t) { // check if is declared function, if not defined, but it in the list in the undefined used functions
		Entry e = env.get(s, Mode.USE);
		if (e == null || !(e instanceof FuncEntry)) return false;
		if (((FuncEntry)e).block == null) 
			funcs.add(s);
		t.returntype = ((FuncEntry)e).type.returntype;
		t.paras = ((FuncEntry)e).type.paras;
		return true;
	}
	private TYPE translate(Func f) throws Error{
		// all FUNC in venv has paras, but it's paras.paras may be null or not
		try {
			Symbol s = Symbol.symbol(f.f.name);
			FUNC t = new FUNC(null, null);
			if (!isDeclaredFunc(s, t)) throw new Error(s.toString()+" is not a function or has not been defined");
			PARAS p = t.paras;
			if (p.paras == null) 
				if (p.vara == false) {
					if (f.exprlist != null) throw new Error("should be no para");
					else return t.returntype;
				} else return t.returntype;
			
			if (f.exprlist == null) throw new Error("para required");
			int len = p.paras.size();
			RECORD r = new RECORD();
			ExprList el = f.exprlist;
			TYPE tt;
			int cnt = 0;
			while (el!=null && cnt <len) {
				tt = translate(el.head);
				r.append(tt, null);
				++cnt;
				el = (ExprList) el.tail;
			}
			boolean elnull = el == null;
			while (el != null) {
				translate(el.head);
				el = el.tail;
			}
			if (cnt < len) throw new Error("#paras unmatched");
			if (p.vara == false && !elnull) throw new Error("#paras unmatched");
			if (!r.equals(p.paras)) throw new Error("para type of "+f.f.name+" unmatched");
			return t.returntype;
		} catch(Error e) {
			throw e;
		}
	}
	private TYPE translate(Expr e) throws Error {
		if (e instanceof BExpr) return e.type = translate((BExpr) e);
		if (e instanceof UExpr) return e.type =  translate((UExpr) e);
		if (e instanceof Var) 	return e.type = translate((Var) e);
		if (e instanceof Str)	return e.type = translate((Str) e);
		if (e instanceof Character) return e.type = translate((Character) e);
		if (e instanceof Func)	return e.type = translate((Func) e);
		if (e instanceof ExprList) return e.type = translate((ExprList)e);
		if (e instanceof Type) return e.type = translateTy((Type)e, TMode.USE);
		return e.type = translate((Num) e);
	}
	
	private TYPE translate(Stmt s) throws Error {
		if (s instanceof Block) return translate((Block) s, BMode.NEWSCOPE);
		if (s instanceof While) return translate((While) s);
		if (s instanceof For) 	return translate((For) s);
		if (s instanceof Break) return translate((Break) s);
		if (s instanceof Continue)	return translate((Continue) s);
		if (s instanceof Return)	return translate((Return) s);
		if (s instanceof If)	return translate((If) s);
		if (s instanceof Expr)	return translate((Expr) s);
		
		throw  new Error("stmt type incorrect");
	}
	
	private roxanne.types.POINTER translateTy(Pointer p) throws Error {
		try {
			roxanne.types.TYPE t = translateTy(p.eletype, TMode.USE);
			return new roxanne.types.POINTER(t);
		} catch (Error e) {
			throw e;
		}
	}
	
	private TYPE isDeclared(Symbol name) {
		Entry e = env.get(name, Mode.USE);
		if (e == null || !(e instanceof TypeDefEntry)) return null;
		return ((TypeDefEntry)e).type.getBind();
	}
	
	private TYPE translateTy(Name tn) throws Error {
		TYPE t = isDeclared(Symbol.symbol(tn.name));
		if (t == null) throw new Error(tn.name+" can not resolved to be a Type");
		return t;
	}
	
	private void translateTy(DecList dl, RECORD r) throws Error {
		if (dl == null) return;
		try {
			translate(dl.head,  r);
			translateTy(dl.tail, r);	//dl.tail!!
		} catch (Error e) { throw e;}
	}
	
	private STRUCTUNION translateTy(StructUnion s) throws Error {
		STRUCTUNION su = null;
		if (s instanceof Struct) su = new STRUCT();
		else if (s instanceof Union) su = new UNION();
		else throw new Error();				// not needed
		
		env.beginScope();
		//System.out.println("==================after beginscope()====================");
		if (s.name != null) {
			su.name = Symbol.symbol(s.name);
			env.put(su);
		}
		if (s.record != null) {
			RECORD record = new RECORD();
			translateTy(s.record, record);
			su.record = record;
		}
		env.endScope();
		//System.out.println("==================after endscope()====================");
		return su;		
	}
	
	private TYPE translateTy(Type t, TMode m) throws Error {
		//return the structunion in tenv except for the one with no name
		//return the declared name in venv
		try  {
			if (t instanceof Int) return INT.getInstance();
			if (t instanceof Char) return roxanne.types.CHAR.getInstance();
			if (t instanceof Void) return roxanne.types.VOID.getInstance();
			if (t instanceof Struct || t instanceof Union) {
				STRUCTUNION su = (STRUCTUNION)translateTy((StructUnion) t);
				if (su.record == null)  {
					if (m == TMode.DEC) {
						STRUCTUNION s = env.get(su, Mode.DEF);
						if (s == null) {
							env.put(su);
							return su;
						} else {
							if ((s instanceof STRUCT) ^ (su instanceof STRUCT)) throw new Error(su.name+" defined as a wrong kind of tag");
							return s;
						}
					}
					STRUCTUNION s = env.get(su, Mode.USE);
					if (s == null) throw new Error("struct (or union) "+su.name+" undeclared");
					if ((s instanceof STRUCT) ^ (su instanceof STRUCT)) throw new Error(su.name+"has been declared as another type");
					return s;
				}
				if (su.name == null) return su;
				else {
					STRUCTUNION s = env.get(su, Mode.DEF);
					if (s!=null && s.record!=null) throw new Error("Struct or union redefined");
					else {
						if (s!= null) {
							if ((s instanceof STRUCT)^(su instanceof STRUCT)) throw new Error("a struct and a union share the same name "+su.name);
							env.addRecord(su);
						} else env.put(su);
						return env.get(su, Mode.DEF);
					}
				}
			}
			if (t instanceof Name) return translateTy((Name) t);		// check declared
			if (t instanceof Pointer) 	return translateTy((Pointer) t);			// check declared
			throw new Error();
		} catch (Error e) {
			throw e;
		}
	}
	
	private boolean checkSU(roxanne.types.STRUCTUNION t, Dec d) throws Error {
		// to make sure the type passed to initDecList not redefined in the current scope
		// and handling the type def and dec condition, when t not exist in the current scope
		// but if it's defining a variable, it can still not exist
		boolean exist;
		roxanne.types.STRUCTUNION tt = null;
		if (t.name == null) exist = false;
		else {
			tt = env.get((roxanne.types.STRUCTUNION)t, Mode.DEF);
			exist = (tt!=null);
		}
		if (exist) {
			if (t.record != null){
				//System.out.println("in checkSU\n" + t.toString());
				if (tt.record != null)	throw new Error("redefinition");
				else {env.addRecord(t);	return true;}			//don't forget
			}
			else return true;									// this omission of return can be detected, so magical
		} else {
			// if type definition or type declaration
			if (t.record != null || d.list == null)  {
				if (t.name == null) return true;
				env.put((roxanne.types.STRUCTUNION) t);
				return true;
			} 
			//if defining variable
			return true;
			//throw new Error("type undefined nor declared");
		}
		//throw new Error("error in checkSU");
	}
		
	private void translate(Dec d, RECORD r) throws Error {//	put check redefinition or  undefined 
		// to make sure the type passed to initDecList exist and not redefined
		//System.out.println("in dec");
			TYPE t = null;
			if (d.list == null) {
				t = translateTy(d.type, TMode.DEC);
				if (t instanceof STRUCTUNION)
					d.su = (STRUCTUNION) t;
				return;
			}
			else t = translateTy(d.type, TMode.USE);
			
			if (t instanceof STRUCTUNION)
				d.su = (STRUCTUNION) t;
			
			translateInDec(t, d.list, r);
	}
	boolean isPointer(roxanne.types.TYPE t) throws Error {
		if (t instanceof roxanne.types.POINTER && !(t instanceof ARRAY))  return true;
		else return false;
	}
	private DMode checkDefinable(Pack p, DMode m) throws Error {
		if (m == DMode.FUNC) {
			Entry e = env.get(p.sym, Mode.USE);		//since only the outest scope can define a function
			if (e == null) return DMode.NULL;
			if (!(e instanceof FuncEntry))	throw new Error("name " + p.sym + " has been defined as another type");
			if (!p.idtype.equals(((FuncEntry)e).type)) throw new Error("function's return type of para type unmatched");
			if (((FuncEntry) e).block == null) {
				return DMode.FUNCDEC;
			} else return DMode.FUNCDEF;
		} else if (m == DMode.VAR) {
			env.get(p.sym, Mode.DEF);
			Entry v = env.get(p.sym, Mode.DEF);		// VarEntry get(venv, Symbol);		Type get(tenv, Symbol)	=>Object get(Table,Symbol); void put(Table, Object)
			if (v != null) throw new Error("variable "+p.sym+" redefinition");
			if (isPointer(p.idtype)) {
				return DMode.NOTHING;
			} else {
				roxanne.types.TYPE a = p.idtype.actual();
				if (a instanceof roxanne.types.WRONG) throw new Error("Type incompelete");
				p.idtype = a;
				return DMode.NOTHING;
			}
		} else if (m == DMode.TYPEDEFNAME) {
			Entry v = env.get(p.sym, Mode.DEF);		// VarEntry get(venv, Symbol);		Type get(tenv, Symbol)	=>Object get(Table,Symbol); void put(Table, Object)
			if (v==null) return DMode.NULL;
			else return DMode.DEFED;
		}
		throw new Error("in checkDefinable");
	}
	
	public static boolean convertable(TYPE from, TYPE to) {
		if (from == null && to == null) return true;
		if ((from == null)^(to == null)) return false;
		if (to instanceof VOID) return true;
		if (toInt(from, IntMode.CONV) && toInt(to, IntMode.CONV)) return true;
		if ((to instanceof POINTER && !(to instanceof ARRAY)) && toPointer(from)) return true;
		if (from.equals(to)) return true;
		return false;
	}
	private Pack checkConvertable(Pack p) throws Error {
		if (! convertable(p.inittype, p.idtype))	// convert inittype to idtype
			throw new Error();
		else 
			return p;
	}
	
	private void translateInDec(TYPE t, InitDecList init, roxanne.types.RECORD r) throws Error {	// check actual except pointer && not redefined&& expression accordance
		if (init == null) return;
		try {
			Pack p= translateInDec(t, init.head);
			
			if (p.idtype instanceof FUNC)  {
				if (p.inittype != null)
					throw new Error("function declaration cannot be initialized");
				DMode m = checkDefinable(p, DMode.FUNC);
				if (m == DMode.NULL) {
					FuncEntry fe = new FuncEntry((FUNC)p.idtype);
					env.put(p.sym,fe);
				} 
			} else if (p.idtype instanceof NAME){
				if (checkDefinable(p, DMode.TYPEDEFNAME) != DMode.NULL) 
						throw new Error("typedef name "+ p.sym+" was defined in this scope");
				else env.put(p);
			} else {
				checkDefinable(p, DMode.VAR);
				if (p.inittype != null)
					p=checkConvertable(p);
				env.put(p);
				if (r != null) r.append(p.idtype, p.sym); 
			}
			
			if (init.head.values != null) {
				if (init.head.values instanceof InitList)
					translateInitList((InitList)init.head.values);
				else translate(init.head.values);
			}
			
			translateInDec(t, init.tail, r);
		} catch(Error e) {
			throw e;
		}
	}
	
	// test version
	private TYPE translate(ExprList el) throws Error {
		//System.out.println("haha:"+el.head);
		TYPE t = translate(el.head);
		if (el.tail == null) return t;
		return translate((ExprList) el.tail);
	}
	
	void translateInitExprList(ExprList el) throws Error {
		Expr head = null;
		while (el != null) {
			head = el.head;
			if (head instanceof InitList) translateInitList((InitList)head);
			else translate(head);	
			el = el.tail;
		}
	}
	void translateInitList(InitList il) throws Error {
		translateInitExprList(il.list);
	}
	
	private Pack translateInDec(TYPE t, InitDector d) throws Error {
		Pack p = translateInDec(t, d.name);
		d.pack = p;

		return p;
	}
	private Pack translateInDec(roxanne.types.TYPE t, Dector d) throws Error {
		Pack p = null;
		if (d instanceof FuncDec) {
			p=translateInDec(t, (FuncDec)d);
		}
		else if (d instanceof Array) { 
			translate(((Array)d).indexlist);
			p=translateInDec(t, (Array) d);
		}
		else if (d instanceof PlainDector) {
			//System.out.println("plaindector");
			p = translateInDec (t, (PlainDector) d);
		}
		else throw new Error("in InitDector");
		d.type = p.idtype;
		return p;
	}
	
	private int bool2bool(Expr.OpType op, int l, int r){
		boolean bl = (l==0)? false:true, br = (r==0)? false:true;
		switch(op) {
		case AND: return (bl&&br == true)? 1:0;
		case OR : return (bl||br == true)? 1:0;
		case NOT: return (!bl == true)? 1:0;
		default: //System.out.println("op error in bool2bool");
			return -1;
		}
	}
	
	private int int2bool(Expr.OpType op, int l, int r) {
		switch(op) {
		case EQ: return (l==r)? 1:0;
		case NE: return (l!=r)? 1:0;
		case GT: return (l>r)? 1:0;
		case LT: return (l<r)? 1:0;
		case GE: return (l>=r)? 1:0;
		case LE: return (l<=r)? 1:0;
		default: /*System.out.println("op error in int2bool"); */return -1;
		}
	}
	
	private boolean isConstantExpr(Expr e, Num size) {
		if (e instanceof Num) {
			size.value = ((Num)e).value;
			//System.out.println(size.value);
			return true;
		}
		if (e instanceof Character) {
			size.value = (int) ((Character) e).c;
			return true;
		}
		Num l = new Num(-1), r = new Num(-1);
		if (e instanceof BExpr) {
			if (!isConstantExpr(((BExpr)e).left,l) || !isConstantExpr(((BExpr)e).right,r)) return false;
			int ll = l.intValue(), rr = r.intValue();
			switch(((BExpr) e).op) {
			// case cast unconsidered
			case BITOR:	 size.value = ll|rr; break;
			case BITAND: size.value = ll&rr; break;
			case BITXOR: size.value = ll^rr; break;
			case SHL: 	 size.value = ll<<rr; break;
			case SHR: 	 size.value = ll>>rr; break;
			case PLUS:	 size.value = ll+rr; break;
			case MINUS:  size.value = ll-rr; break;
			case TIMES:  size.value = ll*rr; break;
			case DIVIDE: size.value = ll/rr; break;
			case MOD:	 size.value = ll%rr; break;
			case AND: case OR: size.value = bool2bool(((BExpr)e).op,ll, rr); break;
			case EQ: case NE: case LT: case GT: case LE: case GE:
				size.value = int2bool(((BExpr)e).op, ll, rr); break;
			default: 
					return false;
			}
			return true;
		} else if(e instanceof UExpr) {
			if (!isConstantExpr(((UExpr)e).e, l)) return false;
			int n = l.intValue();
			switch(((UExpr) e).op) {
			//sizeofE sizeofT unconsidered
			case UMINUS: size.value = -n; break;
			case NOT: 	 size.value = bool2bool(((BExpr)e).op,n,0); break;
			case BITNOT: size.value = ~n; break;
			case UPLUS:	 size.value = n; break;
			default:
				return false;
			}
			return true;
		}
		return false;		
	}
	
	private ARRAY createArray(TYPE ele, ExprList indexes) {
		if (indexes.tail == null)
			return new ARRAY(ele, indexes.head);
		ARRAY a = createArray(ele, indexes.tail);
		return new ARRAY(a, indexes.head);
	}
	
	private Pack translateInDec(TYPE t, Array d) throws Error {
			// array of pointer , func and so on
			Pack p = translateInDec(t, d.name);
			//Num size = new Num(-1);
		//	if (!isConstantExpr(d.size, size) || size.intValue() < 0)
			//	throw new Error("size of Array < 0 or the size is not a constant expression");
			p.idtype = createArray(p.idtype, d.indexlist);
			return p;
	}

	private void translateInDec(roxanne.types.PARAS paras, ParaList pl) throws Error {
		if (pl == null) return;
		Argument head = pl.head;
		if (head instanceof Ellipsis) paras.vara = true;
		else {
			Para para = (Para)head;

			Dec d = new Dec(para.type, new InitDecList(new InitDector(para.name,null),null));
			translate(d, paras.paras);
		}
		translateInDec(paras, pl.tail);	
	}
	
	private Pack translateInDec(roxanne.types.TYPE t, FuncDec d) throws Error {
			Pack p = translateInDec(t, d.f);
			PARAS paras = createParas(d.paras);
			roxanne.types.FUNC fp = new roxanne.types.FUNC(p.idtype, paras);
			return new Pack(fp, p.sym, null);
	}
	
	private Pack translateInDec(roxanne.types.TYPE t, PlainDector d) throws Error {
		if (d instanceof DecName)
			return translateInDec(t, (DecName) d);
		else if (d instanceof DecPtr) 
			return translateInDec(t, (DecPtr)d);
		else if (d instanceof TypeDefName)
			return translateInDec(t, (TypeDefName) d);
		else throw new Error();
	}
	

	private Pack translateInDec(TYPE t, TypeDefName d) throws Error {
		return new Pack(new NAME(d.name, t),Symbol.symbol(d.name), null);
	}
	
	private Pack translateInDec(roxanne.types.TYPE t, DecName d) throws Error{
		Symbol sym = Symbol.symbol(d.name);
		
		// change for test
		Pack  p = new Pack(t, sym, null);
		return p;
	}
	private Pack translateInDec(roxanne.types.TYPE t, DecPtr d) throws Error {
		Pack p = translateInDec(t, d.eledector);
		p.idtype = new roxanne.types.POINTER(p.idtype);
		return p;
	}
}
