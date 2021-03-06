package roxanne.quad;

import java.util.LinkedHashSet;
import java.util.LinkedList;

import roxanne.addr.Addr;
import roxanne.addr.Const;
import roxanne.addr.Temp;
import roxanne.asm.Asm;
import roxanne.asm.Asm.Op;
import roxanne.error.Error;

public class Move extends Quad {
	public Temp dst, src;
	
	public Move(Temp d, Temp s) {
		dst = d;
		src = s;
	}
	public String toString() {
		return "Move\t"+dst+", "+src+super.toString();
	}
	
	public LinkedHashSet<Temp> use() {
		LinkedHashSet<Temp> set = new LinkedHashSet<Temp>();
		addUse(set, src);
		addDefToUse(set, dst);
		return set;
	}
	
	public LinkedHashSet<Temp> def() {
		LinkedHashSet<Temp> set = new LinkedHashSet<Temp>();
		addDef(set, dst);
		return set;
	}
	
	/*
	 * a1(k1) = src
	 */
	/*static void genWhenSpillDst(LinkedList<String> strings, Addr dstAddr, Addr index, String src) {
		if (dstAddr instanceof Temp) {
			String addrName = genBeforeUse(strings, (Temp)dstAddr, k1, a1);
			if (index instanceof Const) {
				strings.add("\tsw\t"+src+", "+genAddress(strings, (Const)index, addrName, k1));
			} else {
				String indexName = genBeforeUse(strings, (Temp)index, a1, a0);
				strings.add("\tswrr\t"+src+", "+genAddress(addrName, indexName));
			}
		} else {
			assert(index == null);
			strings.add("\tsw\t"+src+", "+dstAddr.gen());
		}
	}*/
	
	// call when dst is spilled
	// k0 = a1(k0)
	// (k1) or a1(k1) = k0
	/*static void genWhenSpillDst(LinkedList<String> strings, Addr dstAddr, Addr index, Temp src) {
		if (!src.spilled()) {
			genWhenSpillDst(strings, dstAddr, index, src.gen());
		} else {
			// both are spilled
			String srcName = genBeforeUse(strings, src, k0, a1);
			if (dstAddr instanceof Temp) {
				String dstAddrName = genBeforeUse(strings, (Temp)dstAddr, k1, a1);
				if (index instanceof Const)
					strings.add("\tsw\t"+srcName+", "+genAddress(strings, (Const)index, dstAddrName, k1));
				else {
					String indexName = genBeforeUse(strings, (Temp)index, a1,a0);
					strings.add("\tswrr\t"+srcName+", "+genAddress(dstAddrName, indexName));
				}
			} else {
				assert(index == null);
				strings.add("\tsw\t"+srcName+", "+dstAddr.gen());
			}
		}
	}*/
	@Override
	/*
	 * if (both not spilled)
	 * 		move dst, src
	 * else if(!dst.spilled && src.spilled)
	 * 			lw	dst.gen(), src.index(src.addr);		// Temp.genLoadIfNeed(strings, src, dst.getRegister());
	 * else {
	 * 		regs[] = findNotUsed(src, dst. null);
	 * 
	 * 		if (dst.spilled && !src.spilled) {
	 * 			sw	src, dst.index(dst.addr(k0));	// Temp.genLoadIfNeed(strings, dst.addr, regs[0]), then add this
	 * 		} else if (dst.spilled && src.spilled) {
	 * 			lw	src(k0), src.index(src.addr(k0));	// Temp.genLoadIfNeed(strings, src, regs[0]); then add this
	 * 			sw	src(k0), dst.index(dst.addr(k1));	// Temp.genLoadIfNeed(strings, dst.addr, regs[]); then add this
	 * 		}
	 * }
	 */
	public LinkedList<Asm> gen() throws Error {
		LinkedList<Asm> asms = new LinkedList<Asm>();

		if (!dst.mustBeSpilled() && !src.mustBeSpilled())
			asms.add(new Asm(Op.move, dst, src, null));
		/*else if (!dst.spilled() && src.spilled()) 
			Temp.genLoadIfNeed(asms, src, dst.getRegister(), k0);
		else {
			genWhenSpillDst(asms, dst.addr, dst.index, src);
		}
		*/
		return asms;
	}
	
	
	
}
