package roxanne.types;

import roxanne.symbol.Symbol;

public final class STRUCT extends STRUCTUNION {
	public STRUCT() { super(); }
	public STRUCT(String s, RECORD r) { super(s, r); }
	public STRUCT(Symbol n, RECORD r) { super(n,r); }
}
