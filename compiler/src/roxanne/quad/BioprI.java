package roxanne.quad;

import java.util.LinkedHashSet;
import java.util.LinkedList;

import roxanne.addr.*;
import roxanne.asm.Asm;
import roxanne.asm.Asm.Op;
import roxanne.ast.Expr.OpType;
import roxanne.error.Error;
import roxanne.quad.Quad.ConstMode;

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
	public LinkedList<Asm> gen() throws Error {
		LinkedList<Asm> asms = new LinkedList<Asm>();
		
		Temp l = genBeforeUse(asms, left);
		Addr r = genBeforeUseConst(asms, right, dst.level, ConstMode.ALU);
		Temp d = genBeforeDef(dst);
		
		asms.add(new Asm(getOpI(op), d, l, r));
		
		genAfterDef(asms, dst, d);
		return asms;
	}	
	
}
