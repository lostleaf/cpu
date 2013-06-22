package roxanne.quad;

import java.util.LinkedHashSet;
import java.util.LinkedList;

import roxanne.addr.*;
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
	public LinkedList<String> gen() throws Error {
		LinkedList<String> strings = new LinkedList<String>();
		
		String d = genBeforeDef(dst, k0);
		String r = genBeforeUse(strings, right, k1, k0);
		
		if (outOfBound(left.value, ConstMode.LI))
			genBeforeUseConst(strings, left, k0, ConstMode.LI);
		else strings.add("\tli\t$k0, "+left.gen());
		
		String l = regNames[k0];
		
		strings.add("\t"+getOp(op)+"\t"+d+", "+l+", "+r);
		genAfterDef(strings, dst, k0, k1, a1);
		return strings;
	}

}
