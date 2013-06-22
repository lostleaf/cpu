package roxanne.quad;

import java.util.LinkedHashSet;
import java.util.LinkedList;

import roxanne.addr.Temp;
import roxanne.ast.Expr.OpType;
import roxanne.error.Error;

public class Uop extends Quad {
	public Temp dst, src;
	public OpType op;
	
	public Uop(Temp d, OpType o, Temp s) {
		dst = d;
		op = o;
		src = s;
	}
	
	public String toString() {
		return dst+" = "+op+" "+src+super.toString();
	}
	
	public LinkedHashSet<Temp> use() {
		LinkedHashSet<Temp> set = new LinkedHashSet<Temp>();
		addUse(set, src);
		addDefToUse(set, dst);
		return set;
	}
	public LinkedHashSet<Temp> def() {
		LinkedHashSet<Temp> set = new LinkedHashSet<Temp>();
		addDef(set, dst);
		return set;
	}

	@Override
	/*
	 * if both spilled, op k0, k1;		k1 also used for dst.addr
	 */
	public LinkedList<String> gen() throws Error {
		LinkedList<String> strings = new LinkedList<String>();
	
		String s = null, d = null;
		s = genBeforeUse(strings, src, k1, k0);
		d = genBeforeDef(dst, k0);
		
		strings.add("\t"+getOp(op)+"\t"+d+", "+s);
		
		genAfterDef(strings, dst, k0, k1, a1);
		return strings;
	}
}
