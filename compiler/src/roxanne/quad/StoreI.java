package roxanne.quad;

import java.util.LinkedHashSet;

import roxanne.addr.Const;
import roxanne.addr.Temp;
import roxanne.symbol.Symbol;

public class StoreI extends Quad {
	public Temp dst;
	public Const src;
	public Const index;
	// optional, only for print
	public Symbol name;
	//	width is the dst's width
	/*
	 * 	a[3] = 2;
	 *  index(dst) = src
	 */
	public StoreI(Temp d, Const c, Const s) {
		dst = d;
		index = c;
		src = s;
	}
	
	public String toString() {
		return "stI\t"+ index+"("+dst+"), "+src;
	}
	
	public LinkedHashSet<Temp> use() {
		LinkedHashSet<Temp> set = new LinkedHashSet<Temp>();
		add(set,dst);
		return set;
	}
	
	// so far not used
}
