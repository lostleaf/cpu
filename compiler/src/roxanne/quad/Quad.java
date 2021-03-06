package roxanne.quad;

import roxanne.addr.Temp;


import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

import roxanne.asm.*;
import roxanne.asm.Asm.Op;
import roxanne.addr.*;
import roxanne.ast.Expr.OpType;
import roxanne.error.Error;
import roxanne.translate.Level;
import roxanne.util.Constants;

public abstract class Quad implements Constants {
	public LinkedHashSet<Temp> in = new LinkedHashSet<Temp> ();
	public LinkedHashSet<Temp> out = new LinkedHashSet<Temp>();
	public boolean isLeader = false;
	
	//for DefReach
	public int DRcount = 0;
	public LinkedHashSet<Quad> DRin = new LinkedHashSet<Quad>();
	public LinkedHashSet<Quad> DRout = new LinkedHashSet<Quad>();
	
	public enum ConstMode {ALU, BR, LI, PCOFFSET};
	
	public static Quad makeBiop(Temp dest, Addr l, OpType op, Addr r) {
		if (l instanceof Temp && r instanceof Temp)
			return new Biop(dest, (Temp)l, op, (Temp)r);
		else if (op == OpType.PLUS || op == OpType.PLUS || op == OpType.BITAND
				|| op == OpType.BITOR || op == OpType.BITXOR){
			if (l instanceof Const)
				return new BioprI(dest, (Temp) r, op, (Const)l);
			else return new BioprI(dest, (Temp)l, op, (Const)r);
		} else {
			if (l instanceof Const)
				return new BioplI(dest, (Const)l, op, (Temp)r);
			else return new BioprI(dest, (Temp)l, op, (Const)r);
		}
	}
	
	public static Quad makeMove(Temp dst, Addr src) {
		if (src instanceof Const)
			return new MoveI(dst, (Const)src);
		else if (src instanceof Label)
			return new MoveA(dst, (Label)src);
		else return new Move(dst, (Temp)src);
	}
	
	public static Quad makeStore(Addr dst, Const index, Addr src) {
		/*
		 * if addr of dst to store is const (unconsidered) although it wouldn't happen in my design now??
		 */
		if (index == null) index = new Const(0);
		if (src instanceof Const)
			return new StoreI((Temp)dst, index, (Const) src);
		else return new Store((Temp)dst, index, (Temp)src);
	}
	
	public static Quad makeUop(Temp dst, OpType op, Addr src) {
		return new Uop(dst, op, (Temp)src);
	}
	
	// temporary??
	public abstract LinkedList<Asm> gen() throws Error;
	
	public boolean isJump() { return false; }
	
	public LABEL jumpLABEL() { return null; }
	
	public LinkedHashSet<Temp> use() {
		LinkedHashSet<Temp> set = new LinkedHashSet<Temp>();
		return set;
	}
	public LinkedHashSet<Temp> def() {
		LinkedHashSet<Temp> set = new LinkedHashSet<Temp>();
		return set;
	}
	
	//for DefReach
	public boolean isDef() {
		return true;
	}
	
	
	public static void findAddr(Temp t, LinkedHashSet<Temp> set) {
		/*
		 * for each temp find the register that temp used,
		 * each temp can use only one register, even if its addr is a temp other than fp, gp
		 * since if a temp is not spilled we find the register, whatever its addr is used, since we will not load from its addr once it's not spilled
		 * if it's spilled, the same applies to its register
		 */
		/*
		 * if not must be spilled, the address must be directly calculated before
		 * <=> if the address not calculated before, it must be spilled,since it must be a calculated temp of type pointer
		 */
		if (t!=null && t.addr instanceof Temp && t.addr != Temp.fp && t.addr != Temp.gp)
			if (!((Temp)t.addr).mustBeSpilled())
				set.add((Temp)t.addr);
			else findAddr((Temp)t.addr, set);
		if (t!=null && t.index instanceof Temp && t.index != Temp.fp && t.index != Temp.gp)
			if (!((Temp)t.index).mustBeSpilled())
				set.add((Temp)t.index);
			else findAddr((Temp)t.index, set);
	}
	
	protected void addDef(LinkedHashSet<Temp> set, Addr t) {
		if (t == null || t instanceof Const) return;
		if (((Temp)t).mustBeSpilled() || t == Temp.fp || t == Temp.gp)
			return;
		set.add((Temp)t);
	}
	
	protected void addDefToUse(LinkedHashSet<Temp> set, Addr t) {
		if (t == null || t instanceof Const) return;
		if (((Temp)t).mustBeSpilled()) {
			findAddr((Temp)t, set);
		}
	}
	
	protected void addUse(LinkedHashSet<Temp> set, Addr t) {
		if (t==null || t instanceof Const) return;
		if (((Temp)t).mustBeSpilled()) {
			findAddr((Temp)t ,set);
		} else if (t != Temp.fp && t != Temp.gp)
			set.add((Temp)t);
	}
	
	/*protected int[] getMax(OpType op) {
		switch (op) {
		case MINUS: case PLUS: case TIMES: case ASSIGN:
		}
	}*/
	// for codegen
	protected static int[] getMinMax(ConstMode m) {
		int min = 0, max = 0;
		switch (m) {
		case ALU: case PCOFFSET:
			min = minInt;
			max = maxInt;
			break;
		case LI:
			min = minLiInt;
			max = maxLiInt;
			break;
		case BR:
			min = minBrInt;
			max = maxBrInt;
			break;
		}
		int ans[] = {min, max};
		return ans;
	}
	protected static boolean outOfBound(int n, ConstMode m) {
		int a[] = getMinMax(m);
		int min = a[0], max = a[1];
		
		if (min <= n && n <= max)
			return false;
		return true;
	}
	
	public static Temp genBeforeUse(LinkedList<Asm> strings, Temp t) {
		if (!t.mustBeSpilled()) 
			return t;
		
		return t.genBeforeUse(strings);
	}
	public static Temp genBeforeDef(Temp t) {
		if (!t.mustBeSpilled())
			return t;
		
		return t.level.newTemp(t.width);
	}
	
	public static Addr genBeforeUseConst(LinkedList<Asm> strings, Const num, Level lvl, ConstMode m) {
		int a[] = getMinMax(m);
		int min = a[0], max = a[1];
		
		if (min <= num.value && num.value <= max)
			return num;
		int rest = 0;
		
		Temp ans = lvl.newTemp();
		if (num.value < min) {
			rest = num.value - min;
			strings.add(new Asm(Op.li, ans, new Const(min), null));
		} else {
			strings.add(new Asm(Op.li, ans, new Const(max), null));
			rest = num.value - max;
		}
		strings.add(new Asm(Op.addi, ans, ans, new Const(rest)));
		return ans;
	}
	
	
	protected static void genAfterDef(LinkedList<Asm> strings, Temp t, Temp storeDst) {
		if (!t.mustBeSpilled())
			return;
		strings.addAll(t.genAfterDef(storeDst));
	}
	
	protected static Asm.Op getOp(OpType op) throws Error {
		switch(op) {
		case PLUS: return Op.add;
		case MINUS: return Op.sub;
		case TIMES: return Op.mul;
		default: throw new Error("get illegal op: "+op);
		}
	}
	
	public static Op getOpI(OpType op) throws Error{
		switch(op) {
		case PLUS:	return Op.addi;	
		case MINUS:	return Op.subi;
		case TIMES: return Op.muli;
		default: 
			throw new Error("get illegal op: "+op);
		}
	}
	
	protected static Op getBranchOp(OpType op) throws Error{
		switch(op) {
		case GE: return Op.bge;
		default: throw new Error("get illegal branch op: "+op);
		}
	}
	
	
	/*protected static int[] getFirst2NotUsed(Temp t0, Temp t1, Temp t2) {
		Integer reg0 = find(t0), reg1 = find(t1), reg2 = find(t2);
		//System.out.println(((reg0==null)?null:regNames[reg0])+", "+((reg1 == null)? null:regNames[reg1])+", "+((reg2 == null)? reg2:regNames[reg2]));
		int ans[] = new int[2], cnt = 0;
		for (int i = baseOfSavedRegisters+numOfSavedRegisters-1; i>=baseOfSavedRegisters && cnt<2; --i) {
			if ((reg0!=null && reg0 == i) || (reg1 != null && i==reg1) || (reg2 != null && i == reg2))
					continue;
			else {
				ans[cnt++] = i;
			}
		}
		//System.out.println("regs: "+regNames[ans[0]]+", "+regNames[ans[1]]);
		return ans;
	}*/

	public static String genAddress(String addr, String index) {
		return addr+", "+index;
	}
	/*public static String genAddress(LinkedList<String> strings, Const index, String addrName, int reg) {
		if (index == null)
			return addrName+", "+0;
		
		String constName = genBeforeUseConst(strings, new Const(-index.value), reg, ConstMode.PCOFFSET);
		return addrName+", "+constName;
	}*/
	
	public String toString() {
		//StringBuffer str = new StringBuffer("\n");
		/*str.append("\t\tin: "+in+"\n");
		str.append("\t\tout: "+out+"\n");
		str.append("\t\tDRin: "+DRin+"\n");
		str.append("\t\tDRout: "+DRout+"\n");*/
		//return str.toString();
		return new String();
	}
}
