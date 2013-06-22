package roxanne.quad;

import java.util.LinkedHashSet;
import java.util.LinkedList;

import roxanne.addr.*;
import roxanne.error.Error;
import roxanne.symbol.Symbol;

public class Load extends Quad {
	public Temp dst,src;
	public Const index;
	// optional, only for print
	public Symbol name;
	//	width is the dst's width
	
	/*
	 * dst = index(src)
	 */
	
	public Load(Temp d, Temp s, Const c) {
		dst = d;
		src = s;
		index = c;
	}
	
	public String toString() {
		return "ld "+dst+", "+index+"("+src+")";
	}
	
	public LinkedHashSet<Temp> def() {
		LinkedHashSet<Temp> set = new LinkedHashSet<Temp>();
		set.add(dst);
		return set;
	}
	
	public LinkedHashSet<Temp> use() {
		LinkedHashSet<Temp> set = new LinkedHashSet<Temp>();
		if (src != Temp.fp && src != Temp.gp)
			add(set,src);
		return set;
	}

	// so far no such quad
	
}
