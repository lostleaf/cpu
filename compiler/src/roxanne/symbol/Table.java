package roxanne.symbol;

class Binder {
	Object value;
	Symbol prevtop;
	Binder tail;
	
	Binder(Object v, Symbol p, Binder b) {
		value = v;
		prevtop = p;
		tail = b;
	}
}

public class Table {
	private java.util.Dictionary<Symbol, Binder> dict;
	private Symbol top;
	private Binder marks;
	
	public Table() {
		dict = new java.util.Hashtable<Symbol, Binder>();
		top = null;
		marks = null;
	}
	
	public Object get(Symbol key) {
		Binder b = dict.get(key);
		if (b == null) return null;
		return b.value;
	}
	
	public void put(Symbol key, Object value) {
		dict.put(key, new Binder(value, top, dict.get(key)));
		top = key;
	}
	
	public void beginScope() {
		marks = new Binder(null, top, marks);
		top = null;
	}
	
	public void endScope() {
		while (top != null) {
			Binder b = dict.get(top);
			if (b.tail != null) 
				dict.put(top, b.tail);
			else dict.remove(top);
			top = b.prevtop;
		}
		top = marks.prevtop;
		marks = marks.tail;
	}
	
	public void modify(Symbol key, Object value) {
		Binder b = dict.get(key);
		b.value = value;
		dict.put(key, b);
	}
	
	public java.util.Enumeration<Symbol> keys() {
		return dict.keys();
	}
}

