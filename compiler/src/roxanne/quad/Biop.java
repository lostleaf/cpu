package roxanne.quad;

import java.util.LinkedHashSet;
import java.util.LinkedList;

import roxanne.addr.*;
import roxanne.asm.Asm;
import roxanne.ast.Expr.OpType;
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
	public LinkedList<Asm> gen() throws Error {
		LinkedList<Asm> asms = new LinkedList<Asm>();
		
		Temp r = genBeforeUse(asms, right);
		Temp l = genBeforeUse(asms, left);
		Temp d = genBeforeDef(dst);
		
		asms.add(new Asm(getOp(op), d, l, r));
		
		genAfterDef(asms, dst, d);
		return asms;
	}	
	
	// DefReach
	public void replace(Temp from, Temp to) {
		
	}
}
