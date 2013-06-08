package roxanne.env;

import roxanne.types.*;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;

import roxanne.ast.Block;
import roxanne.symbol.*;
import roxanne.types.*;

public class Env {
	Table tenv;
	Table venv;
	private  Integer scopelevel;
	public Integer loop;
	public FUNC f;
	public TBinder returnty;

	public static enum Mode { USE, DEF;}
	
	class V {
		Entry e;
		Integer level;
		
		V(TYPE t, Integer l) { 
			level = l;
			if (t instanceof FUNC)
				e= new FuncEntry((FUNC)t);
			else if (t instanceof NAME) 
				e = new TypeDefEntry((NAME)t);
			else e = new VarEntry(t);
		}
		V(Entry et, Integer l) {e = et; level = l;}

		public String toString() {
			StringBuffer str = new StringBuffer(e.toString());
			if (e instanceof FuncEntry) {
				if (((FuncEntry)e).block == null)
					str.append("\n\tblock == null");
				else str.append("\n\t"+((FuncEntry)e).block.toString());
			}
			return str.toString();
		}
	}
	class T {
		STRUCTUNION type;
		Integer level;
		T(STRUCTUNION t, Integer i) {
			level = i;
			type = t;
		}
		public String toString() {
			return type.toString();
		}
	}
	public class TBinder {
		public TYPE type;
		public TBinder prev;
		public TBinder(TYPE t, TBinder p) {
			type  = t;
			prev = p;
		}
	}
	
	private void addFUNC() {
		RECORD r = new RECORD();
		r.append(new POINTER(CHAR.getInstance()), Symbol.symbol("format"));
		FUNC f = new FUNC(VOID.getInstance(), new PARAS(r,true));
		FuncEntry fe = new FuncEntry(f, new Block(null,null));
		put(Symbol.symbol("printf"), fe);
		put(Symbol.symbol("scanf"), fe);
		
		RECORD rr = new RECORD();
		rr.append(INT.getInstance(), Symbol.symbol("size"));
		FUNC ff = new FUNC(new POINTER(VOID.getInstance()), new PARAS(rr,false));
		FuncEntry fee = new FuncEntry(ff, new Block(null,null));
		put(Symbol.symbol("malloc"), fee);
	}
	public Env() {
		tenv = new Table();
		venv = new Table();
		scopelevel = 0;
		f=null;
		loop  = new Integer(0);
		returnty = null;
		
		addFUNC();	
	}
	
	//in tenv
	public STRUCTUNION get(STRUCTUNION t, Mode m) {
		Object o =tenv.get(t.name);
		T su = (T) tenv.get(t.name);
		if (m == Mode.USE)	{
			if (su == null) return null;
			return su.type;
		}
		else {
			if (su == null || su.level < scopelevel) return null;
			else return su.type;
		}
	}
	
	//venv include Name
	public Entry get (Symbol s, Mode m) {
		V v = (V) venv.get(s);
		if (m == Mode.USE)  {
			if (v == null) return null;
			return v.e;
		}
		else {
			if (v == null || v.level < scopelevel) return null;
			else return v.e;
		}
	}
	
	public void addRecord(STRUCTUNION su) {
		T t = (T) tenv.get(su.name);
		t.type.record = su.record;
		tenv.modify(su.name, t);
	}
	public void addBlock(Symbol key, Block b) {
		V v = (V) venv.get(key);
		((FuncEntry) v.e).block = b;
		venv.modify(key, v);
	}
	
	//venv
	public void put(Pack p) {
		venv.put(p.sym, new V(p.idtype, scopelevel));
	}
	public void put(Symbol s,Entry e) {
		venv.put(s, new V(e, scopelevel));
	}
	
	// tenv
	public void put(STRUCTUNION t) {
		tenv.put(t.name, new T(t, scopelevel));
	}
	public void beginScope() {
		tenv.beginScope();
		venv.beginScope();
		++scopelevel;
	}
	
	public void endScope() { 
		tenv.endScope();
		venv.endScope(); 
		--scopelevel;
	}
	
	public void pushReturn(TYPE t) {
		returnty = new TBinder(t, returnty);
	}
	public void popReturn() {
		returnty = returnty.prev;
	}
	// just for test
	private void print(Table t) {
		Symbol s = Symbol.symbol("");
		for (Enumeration<Symbol> e = t.keys(); e.hasMoreElements();) {
			s = e.nextElement();
			Object value = t.get(s);
		}
	}
	
	public void put(PARAS paras) {
		if (paras.paras == null) return;
		Pack p = new Pack();
		RECORD.RECORDFIELD r = null;
		LinkedList<RECORD.RECORDFIELD> l = paras.paras.fields;
		Iterator<RECORD.RECORDFIELD> iter = l.iterator();
		while(iter.hasNext()) {
			r = iter.next();
			p.sym = r.name;
			p.idtype = r.type;
			put(p);
		}
	}
	
	public void print() {
		System.out.println("------------env-----------");
		System.out.println("tenv");
		print(tenv);
		System.out.println("venv");
		print(venv);
		System.out.println("------------end of env-----------");
	}
	
}
