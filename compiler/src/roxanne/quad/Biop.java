package roxanne.quad;

import java.util.LinkedHashSet;
import java.util.LinkedList;

import roxanne.addr.*;
import roxanne.ast.Expr.OpType;
import roxanne.error.Error;
import roxanne.error.Error;

public class Biop extends Quad {
	public Temp dst, left, right;
	public OpType op;
	
	public Biop(Temp t1, Temp t2, OpType o, Temp t3) {
		dst = t1;
		left = t2;
		right = t3;
		op = o;
	}
	
	public String toString() {
		return dst.toString()+" = "+left.toString()+" "+op.toString()+" "+right.toString()+super.toString();
	}
	
	public LinkedHashSet<Temp> use() { 
		LinkedHashSet<Temp> set = new LinkedHashSet<Temp>();
		addUse(set, left);
		addUse(set, right);
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
	 * add dst, left, right
	 * if both spilled: k0(k1,a1,a0) = k0(a1, a0)+k1(k0,a0)
	 */
	public LinkedList<String> gen() throws Error {
		LinkedList<String> strings = new LinkedList<String>();
	
		if (!dst.spilled() && !left.spilled() && !right.spilled()) {
			strings.add("\t"+getOp(op)+"\t"+dst.gen()+", "+left.gen()+", "+right.gen());
			return strings;
		}
		
		String l = null, r = null, d = genBeforeDef(dst, k0);
		r = genBeforeUse(strings, right,k1, k0);
		l = genBeforeUse(strings, left,k0, a1);
		
		
		strings.add("\t"+getOp(op)+"\t"+d+", "+l+", "+r);
		
		genAfterDef(strings, dst, k0, k1, a1);
		return strings;
	}	
	
	// DefReach
	public void replace(Temp from, Temp to) {
		
	}
}
