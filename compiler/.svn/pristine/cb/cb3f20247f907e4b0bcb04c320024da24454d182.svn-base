package roxanne.symbol;

public class Symbol {
	private String name;		// why warning?
	private static java.util.Dictionary<String, Symbol> dict = new java.util.Hashtable<String, Symbol>();

	
	public Symbol(String s) {
		name = s;
	}

	public static Symbol symbol(String n) {
		String u = n.intern();
		Symbol s = dict.get(u);
		if (s == null) {
			s = new Symbol(u);
			dict.put(u, s);
		}
		return s;
	}
	
	public String toString() {
		return name;
	}
}
