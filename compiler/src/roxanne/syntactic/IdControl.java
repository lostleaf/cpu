package roxanne.syntactic;

import roxanne.symbol.Symbol;
import roxanne.symbol.Table;

public class IdControl {
	private	Table t;
	private Integer scopelevel;
	public	Boolean typedef;
	public 	Boolean infunc;
	
	class Entry {
		Integer level;
		Entry(Integer i) {level = i;;}
	}
	class VarEntry extends Entry{
		VarEntry(Integer i) {
			super(i);
		}
	}
	class TypeEntry extends Entry {
		TypeEntry(Integer i) {
			super(i);
		}
		
	}
	
	IdControl() {
		t = new Table();
		scopelevel = 0;
		typedef = false;
		infunc = false;
	}
	public void beginScope(){
		t.beginScope();
		++scopelevel;
	}
	public void endScope() {
		t.endScope();
		--scopelevel;
	}
	private Entry getCurrent(Symbol sym) {
		Entry e = (Entry) t.get(sym);
		if (e==null || e.level<scopelevel) return null;
		else return e;
	}
	public boolean isType(String s) {
		Entry e = (Entry) t.get(Symbol.symbol(s));
		if (e==null || e instanceof VarEntry) return false;
		return true;
	}
	public void addVar(String s) throws Error {
		Symbol sym = Symbol.symbol(s);
		t.put(sym, new VarEntry(scopelevel));
	}
	
	public void addType(String s) throws Error {
		Symbol sym = Symbol.symbol(s);
		t.put(sym, new TypeEntry(scopelevel));
	}
	
}
