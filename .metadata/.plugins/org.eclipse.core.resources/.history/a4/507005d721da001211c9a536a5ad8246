package roxanne.quad;

import java.util.LinkedHashSet;

import roxanne.addr.Addr;
import roxanne.addr.Temp;
import roxanne.types.TYPE;

public class Cast extends Quad {
	Temp dst;
	Addr src;
	TYPE type;
	// not use SizeHandler to handle cast;
	
	
	public Cast(Temp d, TYPE t, Addr s) {
		dst= d;
		type = t;
		src = s;
	}
	
	public String toString() {
		return dst+" = ("+type+")"+src;
	}
	
	public LinkedHashSet<Temp> use() {
		LinkedHashSet<Temp> set = new LinkedHashSet<Temp>();
		if (src instanceof Temp)
			add(set,(Temp) src);
		return set;
	}
	
	public LinkedHashSet<Temp> def() {
		LinkedHashSet<Temp> set = new LinkedHashSet<Temp>();
		add(set, dst);
		return set;
	}
	
	// so far assume no cast is used
}
