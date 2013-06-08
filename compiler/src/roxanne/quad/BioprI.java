package roxanne.quad;

import java.util.LinkedHashSet;
import java.util.LinkedList;

import roxanne.addr.*;
import roxanne.ast.Expr.OpType;
import roxanne.error.Error;

public class BioprI extends Quad {
	public Temp dst, left;
	public Const right;
	public OpType op;
	
	public BioprI(Temp t1, Temp t2, OpType o, Const t3) {
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
		addUse(set,left);
		addDefToUse(set, dst);
		return set;
	}
	
	public LinkedHashSet<Temp> def() {
		LinkedHashSet<Temp> set = new LinkedHashSet<Temp>();
		addDef(set,dst);
		return set;
	}
	
	
	/*
	 * if both spilled k0 = k0+imm(k1);  k1 used for k0's addr if need
	 */
	public LinkedList<String> gen() throws Error {
		LinkedList<String> strings = new LinkedList<String>();
		
		String l = genBeforeUse(strings, left, k0,a1);
		String d = genBeforeDef(dst, k0);
		String r = genBeforeUseConst(strings, right, k1, ConstMode.ALU);
		strings.add("\t"+getOpI(op)+"\t"+d+", "+l+", "+r);
		genAfterDef(strings, dst, k0, k1, a1);
		return strings;
	}
	
}
