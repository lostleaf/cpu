package roxanne.types;

import roxanne.symbol.Symbol;

public class UNION extends STRUCTUNION {
	public UNION() {super();}
	public UNION(String s, RECORD r) { super(s, r); }
	public UNION(Symbol n, RECORD r) { super(n,r); }
}
