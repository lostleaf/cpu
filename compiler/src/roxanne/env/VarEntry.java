package roxanne.env;

import roxanne.addr.*;
import roxanne.ast.Str;
import roxanne.symbol.Symbol;
import roxanne.types.*;

public class VarEntry extends Entry {
	/*
	 * each varentry is bound with a temp
	 */
	public TYPE type;
	public Const offset = null;
	public Temp	temp = null;
	public Temp base = null;
	
	//for print
	public Symbol name = null;
	public Str str = null;
	
	public VarEntry(TYPE t) { type = t; }	
	public VarEntry(TYPE t, Const te) { type = t; offset = te; }
	
	public VarEntry(TYPE t, Symbol sym, Const c, Temp tt, Temp tp) {
		type = t; offset = c; base = tt;
		name = sym;
		temp = tp;
	}
	
	public String toString() {
		if (type == null) return "type == null";
		return type.toString();
	}
}
