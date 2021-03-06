package roxanne.translate;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;

import roxanne.env.*;
import roxanne.env.Env.Mode;
import roxanne.error.Error;
import roxanne.addr.*;
import roxanne.analysis.LiveInterval;
import roxanne.ast.*;
import roxanne.ast.Character;
import roxanne.ast.Expr.OpType;
import roxanne.quad.*;
import roxanne.semantic.Semantic;
import roxanne.symbol.Symbol;
import roxanne.types.*;
import roxanne.types.RECORD.RECORDFIELD;
import roxanne.util.Constants;

public class Translator implements Constants{
	private Level level = null;
	private Label label = null;
	private Env env = null;
	public 	LinkedList<Quad> quads = new LinkedList<Quad>();
	public 	LinkedList<CompilationUnit> subUnits = new LinkedList<CompilationUnit>();
	public 	LinkedList<DataFrag> datafrags = new LinkedList<DataFrag>();
	private Stack<LABEL> breaklabels = new Stack<LABEL>();
	private Stack<LABEL> continuelabels = new Stack<LABEL>();
	
	
	public enum EMode {LV, RV};
	
	public Translator(Level l) { env = new Env(); level = l; }
	
	public Translator(Env e, Level l, Label lbl) { env = e; level = l; label = lbl;}
	
	public void translate(DefList dl)  throws Error  {
		transDefList(dl);
		
		//env.print();
		//if (quads.size() != 0)
			//printQ();
		//if (subUnits.size() != 0)
			//printU();
	}
	
	private void transDefList(DefList dl)  throws Error {
		if (dl == null) return;
		transDef(dl.head);
		transDefList(dl.tail);		
	}
	
	private void transDef(Def d) throws Error {
		if (d instanceof Dec) {
			transDec((Dec) d);
		} else if (d instanceof TypeDef) {
			transTypeDef((TypeDef) d);
		} else if (d instanceof FuncDef) {
			transFuncDef((FuncDef) d);
		}
	}
	
	private LinkedList<Temp> createParas(RECORD paras, Level lvl) throws Error {
		/*
		 * create temps for paras and put them in env on the way
		 * but not yet bind to structunion nor to array
		 */
		if (paras == null) return new LinkedList<Temp>();
		
		LinkedList<Temp> temps = new LinkedList<Temp>();
		Iterator<RECORDFIELD> iter = paras.fields.iterator();
		
		RECORDFIELD r = null;
		Const offset = null;
		VarEntry ve = null;
		while (iter.hasNext()) {
			r = iter.next();
			offset = lvl.newPara(r.type.getWidth());
			Temp t = lvl.newTemp(r.type.getWidth());
			ve = new VarEntry(r.type, r.name,  offset, Temp.fp, t);
			t.entry = ve; t.addr = fpgp(false); t.index = offset;
			env.put(r.name, ve);
			temps.add(t);
		}
		return temps;
	}
	
	private void transFuncDef(FuncDef fd) throws Error {
		/*
		 * before: add label, enter, put func and paras, calc parasize
		 * after: add leave
		 */
		
		FuncEntry fe = (FuncEntry) env.get(fd.pack.sym, Mode.DEF);
		Label lbl = null;
		LABEL LBL = null;
		Level lvl = null;
		
		if (fe == null) {
			lbl = new Label(fd.pack.sym);
			LBL = new LABEL(lbl);
			lvl = new Level(level);
			env.put(fd.pack.sym, new FuncEntry((FUNC)fd.pack.idtype, lvl, LBL));
		} else {
			lvl = fe.level;
			LBL = fe.label;
		}
		Translator translator = new Translator(env, lvl, lbl);
		
		translator.quads.add(LBL);
		
		env.beginScope();
			LinkedList<Temp> paras = createParas(((FUNC)fd.pack.idtype).paras.paras, lvl);	// create temps and put them in env
			translator.quads.add(new Enter(lbl, lvl, paras, ((FUNC)fd.pack.idtype).paras.vara));
			if (fd.block.decs != null)
				translator.transDefList(fd.block.decs);
			if (fd.block.stmts != null)
				translator.transStmtList(fd.block.stmts);
		env.endScope();
		
		translator.quads.add(new Leave(lvl, lbl));
		addUnits(translator.getUnits());
		datafrags.addAll(translator.datafrags);
	}
		
	private void transTypeDef(TypeDef d) throws Error {
		/*
		 * typedef struct a ({...}) b;
		 */
		if (d.type instanceof StructUnion && SUWithRecordAndName(d.type))
			addStructUnion((STRUCTUNION)d.su);
		transInitDecList(d.list);
	}
	
	private boolean SUWithRecordAndName(Type t) throws Error {
		if (!(t instanceof StructUnion)) return false;
		if (((StructUnion)t).record == null || ((StructUnion)t).name == null) return false;
		return true;
	}
	private void transDec(Dec d) throws Error {
		/*
		 * if struct a; or struct a {...}; add this structunion
		 * otherwise put or add record when struct a {...} b;
		 */
		if (d.list == null)
			if (d.su==null) return;
			else { 
				LinkedList<Quad> list = d.su.createWidthAndIndex(level); 
				if (list != null)
					quads.addAll(list);
				env.put(d.su);
				
				return; 
			}

		if (d.type instanceof StructUnion && SUWithRecordAndName(d.type))
			addStructUnion((STRUCTUNION)d.su);
		transInitDecList(d.list);
	}
	
	private void transInitDecList(InitDecList idl) throws Error {
		if (idl == null) return;
		transInitDector(idl.head);
		transInitDecList(idl.tail);
	}
	
	private void addStructUnion(STRUCTUNION t) throws Error{
		LinkedList<Quad> list = t.createWidthAndIndex(level);
		if (list != null)	quads.addAll(list);
		STRUCTUNION su = env.get(t, Mode.DEF);
		if (su == null) env.put(t);
		else env.addRecord(t);
	}
	
	public boolean isGlobal() {
		if (level.parent == null) return true;
		return false;
	}
	
	private ExprList transInitExprList(TYPE t, Temp addr, Integer index, ExprList el) throws Error {
		/*
		 * use the front part of el to initialize addr[index] of type t
		 * if t is a scalar call transIniter to initialize using the head
		 * if t is an array, use el to initialize it's elements
		 * if el is too long return the tail as the el to initialize the next part
		 * if el is too short, add zero automatically
		 */
		
		if (!(t instanceof ARRAY)) {
			Temp temp = level.newTemp(t.getWidth()); temp.addr = addr; temp.index = new Const(index);
			transIniter(t, temp, el.head);
			return el.tail;
		}
		
		
		TYPE eletype = ((ARRAY)t).eletype;
		Integer capacity = ((Const)((ARRAY)t).capacity.addr).value;
		Integer size = ((Const)((eletype instanceof ARRAY)? ((ARRAY)eletype).size:eletype.getWidth())).value;
		Integer cnt = 0, nextindex = index;

		while (el != null && cnt < capacity) {
			if (el.head instanceof InitList) {
				Temp temp = level.newTemp(t.getWidth()); temp.addr = addr; temp.index = new Const(nextindex);
				transIniter(eletype, temp, el.head);
				el = el.tail;
			} else {
				el = transInitExprList(eletype, addr, nextindex, el);
			}

			nextindex += size;
			++cnt;
		}


		/*if (cnt < capacity) {
			Integer totalsize = ((Const)((ARRAY)t).size).value;
			for (; nextindex-index < totalsize; nextindex+=4) {
				Temp temp = level.newTemp(t.getWidth()); temp.addr = addr; temp.index = new Const(nextindex);
				// makeMove(addr, new Const(nextindex), INT.getInstance(), new Const(0), INT.getInstance());
				// no cast should be needed
				emit(Quad.makeMove(temp, new Const(0)));
			}
		}*/
		
		return el;
	}
	
	private boolean isCharArrayOrPointer(TYPE t) {
		if (t instanceof ARRAY && ((ARRAY)t).eletype instanceof CHAR) return true;
		if (t instanceof POINTER && ((POINTER)t).eletype instanceof CHAR) return true;
		return false;
	}
	
	private void transIniter(TYPE t, Temp temp, Expr e) throws Error {
		/*
		 * a initer can be a number or a initlist i.e. 2 or {2,3}
		 * for {2,3} check if more elements present and if it's initializing a scalar
		 * for 2 check if initializing an array, convertable and makemove
		 */
	
		if (e instanceof InitList) {
			if (!(t instanceof ARRAY)) throw new Error("initialize a scalar with {}");
			ExprList el = transInitExprList(t, (Temp)temp.addr, ((Const)temp.index).value, ((InitList)e).list); // so far temp.addr should be temp ??
			if (el != null) System.out.println("more elements than needed are initializing an array");
		} else if (e instanceof Str) {
			if (!isCharArrayOrPointer(e.type)) throw new Error("assign a string to type: "+e.type);
			transStr((Str)e, EMode.RV, null, null);
			emit(Quad.makeMove(temp, e.addr));
		} else {
			if (t instanceof ARRAY) throw new Error("invalid initializer");
			if (!Semantic.convertable(e.type, t)) 
				throw new Error("initializer type "+e.type+" is not convertable to "+t);
			transExpr(e, EMode.RV, null, null);
			emit(Quad.makeMove(temp, e.addr));
		}
	}
	
	boolean isStr(Expr e) {
		if (e == null) return false;
		return e instanceof Str;
	}
	
	private void transInitDector(InitDector d) throws Error {
		Pack p = d.pack;
		
		if (p.idtype instanceof FUNC) {
			FuncEntry fe = (FuncEntry)env.get(d.pack.sym, Mode.DEF);
			if (fe == null) 
				env.put(d.pack.sym, new FuncEntry((FUNC)d.pack.idtype, 
						new Level(level), new LABEL(new Label(d.pack.sym))));
		} else if (p.idtype instanceof NAME) {
			NAME name = (NAME) p.idtype;
			env.put(name.name, new TypeDefEntry(name));
		} else {
			Temp t = level.newTemp(p.idtype.getWidth());
			VarEntry ve = null;
			Const offset = null;
			if (p.idtype instanceof ARRAY) {
				LinkedList<Quad> list = ((ARRAY) p.idtype).calcSize(level);
				if (list != null)	quads.addAll(list);
				
				if (isStr(d.values)) {
					transIniter(p.idtype, t, d.values);
					ve = new VarEntry(p.idtype, p.sym, null, t, t);
					t.entry = ve;
					env.put(p.sym, ve);
					if (isGlobal())
						t.setInterval(new LiveInterval(t, 0, inf));
					return;
				} else {
					offset = level.newLocal(((ARRAY)p.idtype).size);
					Temp arrayAddress = level.newTemp(new Const(wordSize));
					makeBiop(arrayAddress, fpgp(isGlobal()),OpType.PLUS, offset);
					ve = new VarEntry(p.idtype, p.sym, offset, fpgp(isGlobal()), arrayAddress);
					t.addr = fpgp(isGlobal()); t.index = offset; t.entry = ve;
					if (isGlobal()) 
						arrayAddress.setInterval(new LiveInterval(arrayAddress, 0, inf));
					env.put(p.sym, ve);
					if (d.values != null) 
						transIniter(p.idtype, t, d.values);
				}
			} else {
				offset =level.newLocal(p.idtype.getWidth());

				ve = new VarEntry(p.idtype, p.sym, offset, fpgp(isGlobal()), t);
				t.addr = fpgp(isGlobal()); t.index = offset; t.entry = ve;
				if (isGlobal())
					t.setInterval(new LiveInterval(t, 0, inf));
				env.put(p.sym, ve);

				if (d.values != null) {
					transIniter(p.idtype, t, d.values);
					if (d.values instanceof Str && p.idtype.equals(new POINTER(CHAR.getInstance())))
						ve.str = (Str)d.values;
				} else if (isGlobal())
					emit(Quad.makeMove(t, new Const(0)));
			}
		}
	}
	
	private void transStmtList(StmtList stmts) throws Error {
		if (stmts == null)
			return;
		transStmt(stmts.head);
		transStmtList(stmts.tail);
	}
	
	private void transStmt(Stmt stmt) throws Error {
		if (stmt instanceof Expr)
			transExpr((Expr) stmt, EMode.RV, null, null);
		else if (stmt instanceof Block)
			transBlock((Block) stmt);
		else if (stmt instanceof While)
			transWhile((While) stmt);
		else if (stmt instanceof For)
			transFor((For) stmt);
		else if (stmt instanceof Break)
			transBreak((Break) stmt);
		else if (stmt instanceof Continue)
			transContinue((Continue) stmt);
		else if (stmt instanceof Return)
			transReturn((Return) stmt);
		else //s instanceof If
			transIf((If) stmt);
	}
	
	private void transReturn(Return ret) throws Error {
		if (ret.values == null) {
			emit(new Ret(null, level, label));
		}
		else {
			transExprList(ret.values, EMode.RV, null, null);
			emit(new Ret(ret.values.addr, level, label));
		}
	}
	
	private void transBlock(Block b) throws Error {
		env.beginScope();
		level.beginScope();
			transDefList(b.decs);
			transStmtList(b.stmts);
		level.endScope();
		env.endScope();
	}
	
	private OpType oppositeOp(OpType op) throws Error {
		switch(op) {
		case EQ: return OpType.NE;
		case NE: return OpType.EQ;
		case LT: return OpType.GE;
		case GT: return OpType.LE;
		case LE: return OpType.GT;
		case GE: return OpType.LT;
		}
		throw new Error("invalid op in oppositeOp!!");
	}
	
	private void emitBranch(OpType op, Addr l, Addr r, LABEL yes, LABEL no) throws Error {
		if (r != null) {
			Addr ans = Addr.biop(l, op, r, level);
			if (ans instanceof Const) { l = ans; op = null; }
			else if (l instanceof Const) { Addr t = l; l = r; r = t;}
		}
		
		if (yes != null && no !=null)  {
			if (op == null) {
				if (l instanceof Temp)
					emit(new Iftrue((Temp)l, yes));
				else {
					assert(l instanceof Const);
					if (((Const)l).value != 0)
						emit(new Goto(yes));
				}
			}
			else emit(new Branch((Temp)l, op, r, yes));
			emit(new Goto(no));
		} else if (yes != null)
			if (op == null)
				if (l instanceof Temp)
					emit(new Iftrue((Temp)l, yes));
				else {
					assert(l instanceof Const);
					if (((Const)l).value != 0)
						emit(new Goto(yes));
				}
			else emit(new Branch((Temp)l, op, r, yes));
		else if (no != null)
			if (op == null) {
				if (l instanceof Temp)
					emit(new Iffalse((Temp)l, no));
				else {
					assert(l instanceof Const);
					if (((Const)l).value == 0)
						emit(new Goto(no));
				}
			}
			else emit(new Branch((Temp)l, oppositeOp(op), r, no));
	}
	
	private void transIf(If s) throws Error {
		if (s.no == null) {
			LABEL next = new LABEL(new Label());
			transExpr(s.cond, EMode.RV, null, next);
			
			env.beginScope();
			level.beginScope();
				transStmt(s.yes);
			env.endScope();
			level.endScope();
			
			emit(next);
		} else {
			LABEL next = new LABEL(), no = new LABEL();
			transExpr(s.cond, EMode.RV, null,no);
			
			env.beginScope();
			level.beginScope();
				transStmt(s.yes);
			env.endScope();
			level.endScope();
			
			emit(new Goto(next));
			emit(no);
			
			env.beginScope();
			level.beginScope();
				transStmt(s.no);
			env.endScope();
			level.endScope();
			
			emit(next);
		}
	}
	
	private void transBreak(Break b) {
		emit(new Goto(breaklabels.peek()));
	}
	
	private void transContinue(Continue cont) throws Error {
		emit(new Goto(continuelabels.peek()));
	}
	
	private void transFor(For s) throws Error {
		LABEL no = new LABEL(), begin = new LABEL(), cont = (s.e3 == null)? begin:new LABEL();
		if (s.e1 != null)
			transExpr(s.e1, EMode.RV, null, null);
		emit(begin);
		if (s.e2 != null)
			transExpr(s.e2, EMode.RV, null, no);
		
		breaklabels.push(no);
		continuelabels.push(cont);
		
		if (s.yes != null) {
			env.beginScope();
			level.beginScope();
			transStmt(s.yes);
			env.endScope();
			level.endScope();
		}
		
		if (s.e3 != null) {
			emit(cont);
			transExpr(s.e3, EMode.RV, null, null);
		}
		emit(new Goto(begin));
		emit(no);
		breaklabels.pop();
		continuelabels.pop();
	}
	
	private void transWhile(While s) throws Error {
		LABEL no = new LABEL(), cont = new LABEL();
		
		emit(cont);
		transExpr(s.cond, EMode.RV, null, no);
		
		breaklabels.push(no);
		continuelabels.push(cont);
		
		env.beginScope();
		level.beginScope();
			transStmt(s.yes);
		env.endScope();
		level.endScope();
		
		emit(new Goto(cont));
		emit(no);
		breaklabels.pop();
		continuelabels.pop();
	}
	
	private void transExpr(Expr e, EMode m, LABEL yes, LABEL no) throws Error {
		if (e instanceof ExprList)
			transExprList((ExprList) e, m, yes, no);
		else if (e instanceof Num)
			transNum((Num) e, m, yes, no);
		else if (e instanceof Character)
			transCharacter((Character)e, m, yes, no);
		else if (e instanceof Str)
			transStr((Str)e, m, yes, no);
		else if (e instanceof Var)
			transVar((Var)e, m, yes, no);
		else if (e instanceof UExpr)
			transUExpr((UExpr)e, m, yes, no);
		else if (e instanceof BExpr)
			transBExpr((BExpr)e, m, yes, no);
		else if (e instanceof Type)
			transTypeExpr((Type)e, m);
		else if (e instanceof Func) 
			transFunc((Func) e, m, yes, no);
		else {
			System.out.println("unconsiderd expr");
			System.exit(1);
		}
	}
	
	private void transPrint_int(Addr addr) {
		LinkedList<Addr> paras = new LinkedList<Addr>();
		paras.add(addr);
		emit(new CallProc(new LABEL(new Label(new String("print_int"))), null, paras));
	}
	
	private void transPrint_str(Addr label) {
		LinkedList<Addr> paras = new LinkedList<Addr>();
		paras.add(label);
		emit(new CallProc(new LABEL(new Label(new String("print_str"))), null, paras));
	}
	
	private void transPrint_char(Addr addr) {
		// only const considered
		/*if (addr instanceof Const) {
			char c[] = {(char) ((Const)addr).value.intValue()};
			Label lbl= new Label();
			String str = new String(c);
			datafrags.add(new DataFrag(lbl, str));
			transPrint_str(lbl);
		}*/
		
	}
	
	
	private void emitPrintCopyAndWidth(char copy, int width, Addr addr) throws Error {
		if (addr instanceof Temp) {
			LABEL cond = new LABEL(), next = new LABEL();
			Temp t = level.newTemp(new Const(wordSize));
			int init = 1;
			for (int i = 1; i < width; ++i) 
				init *=10;
			emit(Quad.makeMove(t, new Const(init)));
			emit(cond);
			emit(new Branch(t, OpType.LE, new Const(1), next));
			emit(new Branch((Temp)addr, OpType.GE, t, next));
			//add char to datafrag
			Character c = new Character(copy);
			transCharacter(c, EMode.LV, null, null);
			transPrint_str(c.addr);
			makeBiop(t, t, OpType.DIVIDE, new Const(10));
			emit(new Goto(cond));
			emit(next);
		} else {
			int num = ((Const)addr).value;
			int digit = 0;
			while (num > 0) {
				++digit;
				num/=10;
			}
			StringBuffer strbuffer = new StringBuffer();
			for (int i = 0; i < width-digit; ++i)
				strbuffer.append(copy);
			Str str = new Str(strbuffer.toString());
			transStr(str, EMode.LV, null, null);
			transPrint_str(str.addr);
		}
		
	}
	
	private void transFunc(Func f, EMode m, LABEL yes, LABEL no) throws Error {
		level.savera = true;
		// only m == RV is valid
		LinkedList<Addr> paras = null;
		
		// deal with stdio, scanf not yet considered
		Symbol fname = Symbol.symbol(f.f.name);
		if ((fname == Symbol.symbol("printf")) || (fname == Symbol.symbol("scanf"))) {
			Str str = null;
			if (f.exprlist.head instanceof Str) 
				str = (Str) f.exprlist.head;
			else {
				VarEntry ve = (VarEntry) env.get(Symbol.symbol(((Var)f.exprlist.head).name), Mode.USE);
				str = ve.str;
			}
			//System.out.println("string: "+str.string);
			LinkedList<String> strings = str.cut();
			ExprList el = f.exprlist.tail;
			for (String string: strings) {
				if (string.charAt(0) == '%' && string.length() == 4){
					char copy = string.charAt(1);
					int width = string.charAt(2)-'0';
					if (el == null) throw new Error("number of parameters is less than needed");
					transExpr(el.head, EMode.RV, null, null);
					emitPrintCopyAndWidth(copy, width, el.head.addr);
					transPrint_int(el.head.addr);
					el = el.tail;
				} else if (string.charAt(0) == '%' && string.charAt(1) != '%' ) {
					if (el == null) throw new Error("number of parameters is less than needed");
					switch(string.charAt(1)) {
					case 'd':	
						transExpr(el.head, EMode.RV, null, null);
						transPrint_int(el.head.addr);
						break;
					case 's':
						transExpr(el.head, EMode.RV, null, null);	// el.head must be of type POINTER (ARRAY belongs to POINTER)
						transPrint_str(el.head.addr);
						break;
					case 'c':
						transExpr(el.head, EMode.LV, null, null);
						transPrint_str(makeBiop(null, el.head.addr, OpType.MINUS, el.head.index));
						break;
					}
					el = el.tail;
				} else if (string.charAt(0) == '%' && string.charAt(1) == '%') {
					transPrint_char(new Const((int)'%'));
				} else {
					Label strlbl = new Label();
					datafrags.add(new DataFrag(strlbl, string));
					transPrint_str(strlbl);
				}
				
			}
			if (el != null)
				throw new Error("number of parameters is more than needed");
			return;
		}
	
		
		if (f.exprlist != null) {
			paras = new LinkedList<Addr>();
			for (ExprList el = f.exprlist; el != null; el = el.tail) {
				transExpr(el.head, EMode.RV, null, null);
				paras.add(el.head.addr);
			}
		}
		
		FuncEntry fe = (FuncEntry)env.get(Symbol.symbol(f.f.name), Mode.USE);
		if (fe.type.returntype instanceof VOID) 
			emit(new CallProc(fe.label, level, paras));
		else {
			if (f.addr == null)
				f.addr = level.newTemp(fe.type.returntype.getWidth());
			emit(new CallFunc((fname == Symbol.symbol("malloc"))? new LABEL(new Label("malloc")):fe.label,  level, (Temp)f.addr, paras));
			if (fname == Symbol.symbol("malloc"))  {
				Addr size = paras.getFirst();
				size = makeBiop(null, size, OpType.MINUS, new Const(wordSize));
				makeBiop(f.addr, f.addr, OpType.PLUS, size);
			}
			emitBranch(null, f.addr, null, yes, no);
		}
		
	}
	private void transExprList(ExprList el, EMode m, LABEL yes, LABEL no) throws Error {
		if (el.tail != null) {
			transExpr(el.head,m, null, null);
			transExprList(el.tail, m, yes, no);
		} else {
			el.head.addr = el.addr;
			transExpr(el.head, m, yes, no);
			el.addr = el.head.addr;
			el.index = el.head.index;
		}
	}
	
	private String makeStr(char c) {
		if (c=='"')
			return new String("\\\"");
		else switch(c) {
		case '\n': return new String("\\n");
		case '\t': return new String("\\t");
		case '\r': return new String("\\r");
		case '\f': return new String("\\f");
		case '\\': return new String("\\\\");
		default:
			char ca[] = {c};
			return new String(ca);
		}
	}
	
	private void transNum(Num n, EMode m, LABEL yes, LABEL no) throws Error {
		if (m == EMode.RV) {
			if (n.addr == null)
				n.addr = new Const(n.value);
			else emit(Quad.makeMove((Temp)n.addr, new Const(n.value)));
			emitBranch(null, n.addr, null, yes, no);
		} else {
			
			Label lbl= new Label();
			String str = makeStr((char)n.value.intValue());
			datafrags.add(new DataFrag(lbl, str));
			n.addr = lbl;
		}
	}
	
	private void transStr(Str str, EMode m, LABEL yes, LABEL no) throws Error {
		// neglect m, yes, no
		// for str, lv means cut the string into parts
		// 		rv means simply put the str in datafrag
		
		Label l = new Label();
		datafrags.add(new DataFrag(l, str.string));
		if (str.addr != null)
			emit(Quad.makeMove((Temp)str.addr, l));
		str.addr = l;
	}
	
	private void transCharacter(Character c, EMode m, LABEL yes, LABEL no) throws Error {
		// not distinguished from Num ??
		// char is a const of width 2;
		if (m == EMode.RV) {
			if (c.addr == null)
				c.addr = new Const((int)c.c);
			else emit(Quad.makeMove((Temp)c.addr, new Const((int)c.c)));
			emitBranch(null, c.addr, null, yes, no);
		} else {
			char ca[] = {c.c};
			Label lbl= new Label();
			String str = new String(ca);
			datafrags.add(new DataFrag(lbl, str));
			c.addr = lbl;
		}
	}
	
	private void transTypeExpr(Type t, EMode m) {
		/*
		 * type has been generated in Semantic
		 * so do nothing here
		 */
	}
	
	private void transVar(Var v, EMode m, LABEL yes, LABEL no) throws Error {
		/*
		 * as to var
		 * only structunion's lv is the address, rv is loading the var;
		 * array and pointer'lv and rv is the same
		 */
		VarEntry ve = (VarEntry) env.get(Symbol.symbol(v.name), Mode.USE);
		// lv and rv of array is the same
		if (ve.type instanceof ARRAY) {
			v.addr = ve.temp;
			return;
		}
		if (m == EMode.LV) {
			v.addr = ve.base;
			v.index = ve.offset;
			ve.temp.setMustBeSpilled();
		} else {
			if (v.addr != null)
				emit(Quad.makeMove((Temp)v.addr, ve.temp));
			v.addr = ve.temp;
			//emit(new Load((Temp)v.addr, ve.global? Temp.gp:Temp.fp, (Const) ve.offset));
		}
		emitBranch(null, v.addr, null, yes, no);
	}
	
	private void setExprWithAddrIndex(Expr e, Addr addr, Addr index, EMode m, LABEL yes, LABEL no) throws Error {
		Addr setaddr = addr;
		if ((index instanceof Const && addr instanceof Const)) {
			setaddr = makeBiop(null, addr, OpType.PLUS, index);
			index = null;
		} else if (index instanceof Temp && addr instanceof Const) {
			Addr t = index; index = addr; addr = t;
		}
		if (m == EMode.LV) {
			e.addr = setaddr;
			e.index = index;
		} else {
			Temp result = level.newTemp(e.type.getWidth());
			result.addr = setaddr;
			result.index = index;
			result.setMustBeSpilled();
			if (e.addr == null)
				e.addr = result;
			else emit(Quad.makeMove((Temp)e.addr, result));
			emitBranch(null, e.addr, null, yes, no);
			//setMustBeSpilled
		}
		
	}
	
	private void transOpAssignExpr(BExpr e, OpType op, LABEL yes, LABEL no) throws Error {
		// RESULT = RV
		transExpr(e.right, EMode.RV, null, null); transExpr(e.left, EMode.RV, null,null);
		
		makeBiop(e.left.addr, e.left.addr, op, e.right.addr);
		
		if (e.addr != null) 
			emit(Quad.makeMove((Temp)e.addr, e.left.addr));
		e.addr = e.left.addr;
		
		emitBranch(null, e.addr, null, yes, no);
	}
	
	private Addr transINC(Addr dst, Expr e, OpType op, boolean right) throws Error {
		if (dst != null && dst instanceof Const) throw new Error("dst "+dst+" not a temp in transINC");
		// return rv
		transExpr(e, EMode.RV, null, null);
		if (!right) {
			Addr ans = makeBiop(e.addr, e.addr, op, new Const(1));
			if (dst != null)
				emit(Quad.makeMove((Temp)dst, ans));
			return ans;
		} else {
			if (dst == null)
				dst = level.newTemp(new Const(wordSize));
			emit(Quad.makeMove((Temp)dst, e.addr));
			makeBiop(e.addr, e.addr, op, new Const(1));
			return dst;
		}
	}
	// lv do not emit
	private void transUExpr(UExpr e, EMode m, LABEL yes, LABEL no) throws Error {
		switch(e.op) {
		case GET:	// can be lv or rv
			transExpr(e.e, EMode.RV, null, null);
			if (m == EMode.LV)
				e.addr = e.e.addr;
			else {
				Addr initialAddr = e.addr;
				e.addr = level.newTemp(e.type.getWidth());
				((Temp)e.addr).addr = e.e.addr;
				emitBranch(null, e.addr, null, yes, no);
				((Temp)e.addr).setMustBeSpilled();
				
				if (initialAddr != null)
					emit(Quad.makeMove((Temp)initialAddr, e.addr));
			}
			break;
		case POS: // return rv
			transExpr(e.e, EMode.LV, null, null);
			if (e.e.index != null)
				e.addr = makeBiop(e.addr, e.e.addr, OpType.MINUS, e.e.index);
			else {
				if (e.addr != null)
					emit(Quad.makeMove((Temp)e.addr, e.e.addr));
				e.addr = e.e.addr;
			}
			emitBranch(null, e.addr, null, yes, no);
			break;
		case UPLUS:	
			transExpr(e.e, EMode.RV, null, null);
			if (e.addr != null)
				emit(Quad.makeMove((Temp)e.addr, e.e.addr));
			e.addr = e.e.addr;
			emitBranch(null, e.addr, null, yes, no);
			break;
		case UMINUS: 	case BITNOT:  
			transExpr(e.e, EMode.RV, null, null);
			e.addr = makeUop(e.addr, e.op, e.e.addr);
			emitBranch(null, e.addr, null, yes, no);
			break;
		case NOT:
			transExpr(e.e, EMode.RV, no, yes);
			
			if (yes == null && no == null) {
				if (e.e.addr instanceof Const) {
					e.addr = makeUop(e.addr, e.op, e.e.addr);
					break;
				}
				
				no = new LABEL();
				LABEL next = new LABEL();
				emit(new Iftrue((Temp)e.e.addr, no));
				if (e.addr == null)
					e.addr = level.newTemp(new Const(4));
				emit(Quad.makeMove((Temp)e.addr, new Const(1)));
				emit(new Goto(next));
				emit(no);
				emit(Quad.makeMove((Temp)e.addr, new Const(0)));
				emit(next);
			}
			break;
			
		case SIZEOFE: case SIZEOFT:
			transExpr(e.e, EMode.RV, null, null);
			Addr ans = null;
			if (e.e.type instanceof ARRAY) ans = ((ARRAY)e.e.type).size;
			else if (e.e.type instanceof CHAR) ans = new Const(2);
			else ans = e.e.type.getWidth();
			emitBranch(null, ans, null, yes, no);
			
			if (e.addr != null)
				emit(Quad.makeMove((Temp)e.addr, ans));
			if (e.addr == null || ans instanceof Const)  e.addr = ans;
			break;
		case LDEC:	// left is false
			e.addr = transINC(e.addr, e.e, OpType.MINUS, false);	// include inc and dec
			emitBranch(null, e.addr, null, yes, no);
			break;
		case LINC:
			e.addr = transINC(e.addr, e.e, OpType.PLUS, false);
			emitBranch(null, e.addr, null, yes, no);
			break;
		case RINC:
			e.addr = transINC(e.addr, e.e, OpType.PLUS, true);
			emitBranch(null, e.addr, null, yes, no);
			break;
		case RDEC:
			e.addr = transINC(e.addr, e.e, OpType.MINUS, true);
			emitBranch(null, e.addr, null, yes, no);
			break;
		}
	}
	
	private boolean isZero(Addr a) {
		if (a == null) return true;
		if (a instanceof Const && ((Const)a).value == 0)
			return true;
		return false;
	}
	
	private boolean isN(Addr a, int n){
		if (a instanceof Const && ((Const)a).value == n)
			return true;
		return false;
	}
	
	// reduce biop complexity
	private Addr makeBiop(Addr dst, Addr l, OpType op, Addr r) throws Error {
		// simplify if can
		if (dst != null && !(dst instanceof Temp)) throw new Error("dst not temp in makeBiop in translator");
		
		Addr optimizedAns = null;
		/*if (op == OpType.PLUS && isZero(l)||isZero(r)) {
			optimizedAns = (isZero(l))? r:l;
		} else*/ if (op == OpType.TIMES) {
			if ((isN(l,1) || isN(r, 1)))
				optimizedAns = (isN(l, 1))? r:l;
			else if (isN(l, 2) || isN(r, 2))
				optimizedAns = isN(l,2)?makeBiop(dst, r, OpType.PLUS, r): makeBiop(dst, l, OpType.PLUS, l);
		} else if (op == OpType.DIVIDE && isN(r, 1))
			optimizedAns = l;
		if (optimizedAns != null) {
			if (dst != null)
				emit(Quad.makeMove((Temp)dst, optimizedAns));
			return optimizedAns;
		}
			
		
		Addr ans = Addr.biop(l, op, r, level);
		if (dst == null) {
			if (ans instanceof Temp)
				emit(Quad.makeBiop((Temp)ans, l, op, r));
		} else {
			if (ans instanceof Temp)
				emit(Quad.makeBiop((Temp)dst, l, op, r));
			else emit(Quad.makeMove((Temp)dst,ans));
		}
		return (dst != null && ans instanceof Temp)? dst:ans;
	}
	
	private Addr makeUop(Addr dst, OpType op, Addr e) throws Error {
		if (dst != null && dst instanceof Const)
			throw new Error("dst: "+dst+" is not a temp in makeUop");
		
		Addr ans = Addr.uop(op, e, level);
		if (dst == null) {
			if (ans instanceof Temp)
				emit(new Uop((Temp)ans, op,(Temp) e));
		} else {
			if (ans instanceof Const)
				emit(Quad.makeMove((Temp)dst, ans));
			else emit(new Uop((Temp)dst, op,(Temp) e));
			ans = (ans instanceof Const)? ans:dst;
		}
		return ans;
	}
	
	private void transBExpr(BExpr e, EMode m, LABEL yes, LABEL no)throws Error  {
		LABEL ltrue = null, lfalse = null;
		boolean calcvalue = false;
		switch (e.op) {
		case PLUS: case MINUS: case TIMES: case DIVIDE: case MOD:
		case BITOR: case BITXOR: case BITAND: case BITNOT: 
		case SHR: case SHL:	// return RV
			transExpr(e.left, EMode.RV, null, null);
			transExpr(e.right, EMode.RV, null, null);
			e.addr = makeBiop(e.addr, e.left.addr, e.op, e.right.addr);
			emitBranch(null, e.addr, null,yes, no);
			break;
			
		case EQ: case NE: case LT: case GT: case LE: case GE:
			transExpr(e.left, EMode.RV, null, null);
			transExpr(e.right, EMode.RV, null, null);
			if (yes == null && no == null)
				e.addr = makeBiop(e.addr, e.left.addr, e.op, e.right.addr);
			else emitBranch(e.op, e.left.addr, e.right.addr, yes, no);
			break;
			
		case OR: // return RV
			if (yes == null && no == null) {
				calcvalue = true;
				yes = new LABEL(); no = new LABEL();
			}
			
			ltrue = (yes == null)? new LABEL(): yes;
			transExpr(e.left, EMode.RV, ltrue, null);
			transExpr(e.right, EMode.RV, yes, no);
			if (yes == null)
				emit(ltrue);
			
			if (calcvalue) {
				LABEL next = new LABEL();
				if (e.addr == null)
					e.addr = level.newTemp(new Const(4));
				emit(yes);
				emit(Quad.makeMove((Temp)e.addr, new Const(1)));
				emit(new Goto(next));
				emit(no);
				emit(Quad.makeMove((Temp)e.addr, new Const(0)));
				emit(next);
			}
			break;

		case AND:
			if (yes == null && no == null) {
				calcvalue = true;
				yes = new LABEL(); no = new LABEL();
			}
			
			lfalse = (no == null)? new LABEL(): no;
			transExpr(e.left,EMode.RV, null, lfalse);
			transExpr(e.right,EMode.RV, yes, no);
			if (no == null)
				emit(lfalse);
			
			if (calcvalue) {
				LABEL next = new LABEL();
				if (e.addr == null)
					e.addr = level.newTemp(new Const(4));
				emit(yes);
				emit(Quad.makeMove((Temp)e.addr, new Const(1)));
				emit(new Goto(next));
				emit(no);
				emit(Quad.makeMove((Temp)e.addr, new Const(0)));
				emit(next);
			}
			break;
		
		case CAST: 			// return RV
			throw new Error("has cast !!!!");
			/*transExpr(e.left, EMode.RV, null, null);
			transExpr(e.right, EMode.RV, null, null);
			
			e.addr = castIfNeed(e.left.type, e.right.type, e.right.addr);
			emitBranch(null, e.addr, null, yes, no);
			break;			*/
			
		case ASSIGN:	//return RV
			transExpr(e.left, EMode.RV, null, null);
			e.right.addr = e.left.addr;	//new
			transExpr(e.right, EMode.RV, null, null);
			
			if (e.addr != null)
				emit(Quad.makeMove((Temp)e.addr, e.right.addr));
			e.addr = e.right.addr;
			//makeMove(e.left, EMode.RV, e.right, EMode.RV);
			emitBranch(null, e.addr, null, yes, no);
			break;			
			
		case ARRAY:
			/*
			 * if array calc the pos (RV)
			 * else
			 * 		if LV, set addr, global, otherwise load it;
			 */
			transExpr(e.left, EMode.RV, null, null);
			transExpr(e.right, EMode.RV, null, null);
			Addr r = castIfNeed(INT.getInstance(), e.right.type, e.right.addr);
			Addr index = null, width = null;
			if (e.type instanceof ARRAY)
				width =  ((ARRAY)e.type).size;
			else width = e.type.getWidth();
				index = makeBiop(null, r, OpType.TIMES, width);
			
			if (e.type instanceof ARRAY) {
				e.addr = makeBiop(e.addr, e.left.addr, OpType.PLUS, index);
				emitBranch(null, e.addr, null, yes, no);
			} else 
				setExprWithAddrIndex(e, e.left.addr, index, m, yes, no);	//??
			break;
			
		case DOT:
			/*
			 * addr = l.addr
			 * idx = l.index + getIndex(r);
			 * if RV then load
			 */
			transExpr(e.left, EMode.LV, null, null);
			
			Addr addr = e.left.addr;
			Addr rindex = ((STRUCTUNION)e.left.type).getIndex(((Var)e.right).name);
			Addr idx = null;
			if (e.left.index != null)
				idx = makeBiop(null, rindex, OpType.PLUS, e.left.index);
			else idx = rindex;
			
			setExprWithAddrIndex(e, addr, idx, m, yes, no);
			break;
			
		case PTR:	// a->b
			transExpr(e.left, EMode.RV, null, null);
			STRUCTUNION su = (STRUCTUNION) ((POINTER)e.left.type).eletype;
			setExprWithAddrIndex(e, e.left.addr,su.getIndex(((Var)e.right).name),m, yes, no);
			break;
		case MULASSIGN:
			transOpAssignExpr(e, OpType.TIMES, yes, no); break;
		case DIVASSIGN:
			transOpAssignExpr(e, OpType.DIVIDE, yes, no); break;
		case MODASSIGN:
			transOpAssignExpr(e, OpType.MOD, yes, no); 	break;
		case ADDASSIGN:
			transOpAssignExpr(e, OpType.PLUS, yes, no);	break;
		case SUBASSIGN:
			transOpAssignExpr(e, OpType.MINUS, yes, no); break;
		case ANDASSIGN:
			transOpAssignExpr(e, OpType.AND, yes, no); 	break;
		case ORASSIGN:
			transOpAssignExpr(e, OpType.OR, yes, no);	break;
		case XORASSIGN:
			transOpAssignExpr(e, OpType.BITXOR, yes, no);break;
		case SHLASSIGN:
			transOpAssignExpr(e, OpType.SHL, yes, no);	break;
		case SHRASSIGN:
			transOpAssignExpr(e, OpType.SHR, yes, no);	break;
		}
	}
	
	private Addr castIfNeed(TYPE to, TYPE from, Addr src) {
		if (needCast(to, from)) {
			Temp t = level.newTemp(to.getWidth());
			emit(new Cast(t, to, src));
			return t;
		} else return src;
	}
	
	private boolean needCast(TYPE to, TYPE from) {
		// bool not considered so far
		if ((from instanceof CHAR || from instanceof INT)^(to instanceof CHAR || to instanceof INT)) return true;
		if ((from instanceof POINTER || from instanceof INT) && (to instanceof POINTER|| to instanceof INT))
			// POINTER including ARRAY
			return false;
		if (from.equals(to)) return false;
		System.out.println("cast condition unconsidered (in need cast)");
		return true;
	}
	
	public static Temp fpgp(boolean global) {
		return global? Temp.gp:Temp.fp;
	}
	
	private Addr getWidth(TYPE t) {
		return (t instanceof ARRAY)? ((ARRAY)t).size: t.getWidth();
	}
	
	private Addr makeMove(Addr dstaddr, Const dstindex, TYPE dsttype, Addr src, TYPE srctype) {
		// dstaddr[dstindex] = src: addr
		Addr s = null;
		if (needCast(dsttype, srctype)) {
			s = level.newTemp(getWidth(dsttype));
			emit(new Cast((Temp)s, dsttype, src));
		} else s = src;
		emit(Quad.makeStore(dstaddr, dstindex, s));
		return s;
	}
	
	/*private Addr makeMove(Expr dst, EMode dm, Addr src, TYPE srctype) {
		// dst:expr = src: addr
		if (dm == EMode.RV) {
			if (needCast(dst.type,srctype))
				emit(new Cast((Temp)dst.addr, dst.type, src));
			else emit(Quad.makeMove((Temp)dst.addr, src));
			return dst.addr;
		} else { //dm==lv
			return makeMove(dst.addr, dst.index, dst.type, src, srctype);
		} 
	}
	
	private Addr makeMove(Expr dst, EMode dm, Expr src, EMode sm) throws Error {
		// dst:expr = src:expr
		// return addr of the RV
		if (sm == EMode.RV)
			return makeMove(dst, dm, src.addr, src.type);
		else if (dm == EMode.RV) {
			if (!needCast(dst.type, src.type))
				emit(Quad.makeLoad((Temp)dst.addr, src.addr, (Const)src.index));
			else {
				Temp t = level.newTemp(getWidth(dst.type));
				emit(Quad.makeLoad(t,src.addr, src.index));
				emit(new Cast((Temp)dst.addr, dst.type, t));
			}
			return dst.addr;
		} else { // dm==lv && sm==lv
			Temp t1 = level.newTemp(getWidth(src.type));
			emit(Quad.makeLoad(t1, src.addr, src.index));
			
			return makeMove(dst.addr, dst.index, dst.type, t1, src.type);
		}
	}
	*/
	private void emit(Quad q) {
		quads.add(q);
	}
	
	private void addUnits(LinkedList<CompilationUnit> units) {
		for (CompilationUnit unit: units) {
			if (unit.label.name != null && unit.label.name == Symbol.symbol("main")) {
				subUnits.addFirst(unit);
			} else subUnits.add(unit);
		}
	}
	
	public LinkedList<CompilationUnit> getUnits() {
		CompilationUnit unit = new CompilationUnit(quads, label, level);
		subUnits.addFirst(unit);
		return subUnits;
	}
	
	
	private void printQ() {
		Iterator<Quad> iter = quads.iterator();
		while (iter.hasNext())
			System.out.println(iter.next());
	}
	
	private void printU() {
		Iterator<CompilationUnit> iter = subUnits.iterator();
		while (iter.hasNext())
			System.out.println(iter.next());
	}
	
}