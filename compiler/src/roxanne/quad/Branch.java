package roxanne.quad;

import java.util.LinkedHashSet;
import java.util.LinkedList;

import roxanne.addr.*;
import roxanne.asm.Asm;
import roxanne.ast.Expr.OpType;
import roxanne.error.Error;
import roxanne.quad.Quad.ConstMode;

public class Branch extends Quad {
	public Temp left;
	public Addr right;
	public LABEL label;
	public OpType op;
	
	/*
	 * although by my optimization which calculate the result if both l and r are Const,
	 * at most one of l and r can be Const
	 * don't forget to li them to a register
	 */	
	public Branch(Temp l, OpType o, Addr r, LABEL lbl) {
		left = l;
		right = r;
		op = o;
		label = lbl;
	}
	
	public String toString() {
		return "b"+""+op+" "+left+" "+right+" goto "+label+super.toString();
	}
	
	public boolean isJump() { return true; }
	
	public LABEL jumpLABEL() {
		return label;
	}
	
	public LinkedHashSet<Temp> use() {
		LinkedHashSet<Temp> set = new LinkedHashSet<Temp>();
		addUse(set, (Temp)left);
		if (right instanceof Temp)
			addUse(set, (Temp)right);
		return set;
	}
	
	//for DefReach
	public boolean isDef() {
		return false;
	}
	
	/*
	 * if both spilled or right is const
	 * if bop k0, k1, label
	 */
	public LinkedList<Asm> gen() throws Error {
		LinkedList<Asm> asms = new LinkedList<Asm>();
		
		Temp l = genBeforeUse(asms, left);
		Addr r = null;
		if (right instanceof Temp)
			r = genBeforeUse(asms, (Temp)right);
		else
			r = genBeforeUseConst(asms, (Const)right, left.level, ConstMode.BR);
		
		asms.add(new Asm(getBranchOp(op), l, r, label.label));
		return asms;
	}	
}
