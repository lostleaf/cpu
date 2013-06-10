package roxanne.quad;

import java.util.LinkedHashSet;
import java.util.LinkedList;

import roxanne.addr.*;
import roxanne.ast.Expr.OpType;

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
	public LinkedList<String> gen() throws roxanne.error.Error {
		LinkedList<String> strings = new LinkedList<String>();
		
		String l = null, r = null;
		
		if (right instanceof Temp)
			r = genBeforeUse(strings, (Temp)right, k1, k0);
		else r = genBeforeUseConst(strings, (Const)right, k1, ConstMode.BR);
		l = genBeforeUse(strings, left,k0, a1);
		
		strings.add("\tb"+getBranchOp(op)+"\t"+l+", "+r+", "+label.label.gen());
		return strings;
	}
}