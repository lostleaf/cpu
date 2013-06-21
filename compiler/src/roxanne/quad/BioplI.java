package roxanne.quad;

import java.util.LinkedHashSet;
import java.util.LinkedList;

import roxanne.addr.*;
import roxanne.asm.Asm;
import roxanne.asm.Asm.Op;
import roxanne.ast.Expr.OpType;
import roxanne.error.Error;

public class BioplI extends Quad {
	public Temp dst, right;
	public Const left;
	public OpType op;
	
	public BioplI(Temp t1, Const t2, OpType o, Temp t3) {
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
		addUse(set, right);
		addDefToUse(set, dst);
		return set;
	}
	
	public LinkedHashSet<Temp> def() {
		LinkedHashSet<Temp> set = new LinkedHashSet<Temp>();
		addDef(set,dst);
		return set;
	}
	
	
	/*
	 * lw k0, left
	 * k0 = k0 op k1;  k1 used for k0's addr if need
	 */
	public LinkedList<Asm> gen() throws Error {
		LinkedList<Asm> asms = new LinkedList<Asm>();
		
		Temp r = genBeforeUse(asms, right);
		Temp l = null;
		if (outOfBound(left.value, ConstMode.LI))
			l = (Temp)genBeforeUseConst(asms, left, dst.level, ConstMode.ALU);
		else  {
			l = dst.level.newTemp();
			asms.add(new Asm(Op.li, l, left, null));
		}
		Temp d = genBeforeDef(dst);
		
		asms.add(new Asm(getOp(op), d, l, r));
		
		genAfterDef(asms, dst, d);
		return asms;
	}	
}
