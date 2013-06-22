package roxanne.quad;

import java.util.LinkedHashSet;

import roxanne.addr.Const;
import roxanne.addr.Temp;
import roxanne.symbol.Symbol;

public class LoadD extends Quad {
	public Temp dst;
	public Const offset;
	// optional, only for print
	public Symbol name;
	//	width is the dst's width
	
	public LoadD(Temp d,Const c) {
		dst = d;
		offset = c;
	}
	
	public String toString() {
		return "ldD "+dst+", "+offset;
	}
	
	public LinkedHashSet<Temp> def() {
		LinkedHashSet<Temp> set = new LinkedHashSet<Temp>();
		add(set,dst);
		return set;
	}
}
