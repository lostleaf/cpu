package roxanne.quad;

import java.util.LinkedHashSet;
import java.util.LinkedList;

import roxanne.addr.*;
import roxanne.asm.Asm;
import roxanne.asm.Asm.Op;
import roxanne.error.Error;

public class MoveI extends Quad {
	public Temp dst; 	
	public Const src;
	
	public MoveI(Temp d, Const s) {
		dst = d;
		src = s;
	}
	public String toString() {
		return "MoveI\t"+ dst+", "+src+super.toString();
	}
	
	public LinkedHashSet<Temp> def() {
		LinkedHashSet<Temp> set = new LinkedHashSet<Temp>();
		addDef(set,dst);
		return set;
	}
	
	public LinkedHashSet<Temp> use() {
		LinkedHashSet<Temp> set = new LinkedHashSet<Temp>();
		addDefToUse(set, dst);
		return set;
	}
	
	/*static void genBeforeLoadConst(LinkedList<String> strings, Const num, int reg) {
		if (outOfBound(num.value, ConstMode.LI))
			genBeforeUseConst(strings, num, reg, ConstMode.LI);
		else
			strings.add("\tli\t"+regNames[reg]+", "+ num.gen());
	}*/
	/*
	 * k1(addr) = k0
	 */
	/*static void genWhenSpillDst(LinkedList<Asm> asms, Addr dstAddr, Addr index, Const src) {
		genBeforeLoadConst(strings, src, k0);
		if (dstAddr instanceof Temp) {
			String dstAddrName = genBeforeUse(strings, (Temp)dstAddr, k1, a1);
			if (index instanceof Const)
				strings.add("\tsw\t"+regNames[k0]+", "+genAddress(strings, (Const)index, dstAddrName, k1));
			else {
				String indexName = genBeforeUse(strings, (Temp)dstAddr, a1, a0);
				strings.add("\tswrr\t"+regNames[k0]+", "+genAddress(dstAddrName, indexName));
			}
		} else {
			assert(index == null);
			strings.add("\tsw\t"+regNames[k0]+", "+dstAddr.gen());
		}
	}*/
	
	@Override
	/*
	 * li	dst, src;
	 * if dst.spilled
	 * 	li	k0, src;
	 * 	sw	k0,	dst.index(dst.addr(k1))
	 * 
	 */
	public LinkedList<Asm> gen() throws Error {
		LinkedList<Asm> asms = new LinkedList<Asm>();

		if (!dst.mustBeSpilled())
			asms.add(new Asm(Op.li, dst, src, null));
		/*else {	// dst.spilled, src must be loaded before sw
			genWhenSpillDst(asms, dst.addr, dst.index, src);
		}*/
		
		return asms;
	}
	
}
