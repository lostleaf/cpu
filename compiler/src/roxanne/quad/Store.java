package roxanne.quad;

import java.util.LinkedHashSet;

import roxanne.addr.*;
import roxanne.symbol.Symbol;

public class Store extends Quad {
	public Temp dst, src;
	public Const index;
	// optional, only for print
	public Symbol name;
	//	width is the dst's width
	/*
	 * index(dst) = src
	 */
	
	public Store(Temp d, Const c, Temp s) {
		dst = d;
		index = c;
		src = s;
	}
	public String toString() {
		return "st\t"+ index+"("+dst+"), "+src;
	}
	
	public LinkedHashSet<Temp> use() {
		LinkedHashSet<Temp> set = new LinkedHashSet<Temp>();
		add(set, src);
		add(set, dst);
		return set;
	}
	// so far not used;
}
