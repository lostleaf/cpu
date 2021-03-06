package roxanne.quad;

import java.util.LinkedHashSet;
import java.util.LinkedList;

import roxanne.addr.Addr;
import roxanne.addr.Const;
import roxanne.addr.Label;
import roxanne.addr.Temp;
import roxanne.asm.Asm;
import roxanne.error.Error;

public class MoveA extends Quad {
	public Temp dst; 	
	public Label src;
	
	public MoveA(Temp d, Label s) {
		dst = d;
		src = s;
	}
	public String toString() {
		return "MoveA\t"+ dst+", "+src+super.toString();
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
	
	static void genBeforeLoadLabel(LinkedList<String> strings, Label num, int reg) {
		strings.add("\tla\t"+regNames[reg]+", "+ num.gen());
	}
	
	/*static void genWhenSpillDst(LinkedList<String> strings, Addr dstAddr, Addr index, Label src) {
		genBeforeLoadLabel(strings, src, k0);
		if (dstAddr instanceof Temp) {
			String dstAddrName = genBeforeUse(strings, (Temp)dstAddr, k1, a1);
			if (index instanceof Const)
				strings.add("\tsw\t"+regNames[k0]+", "+genAddress(strings, (Const)index, dstAddrName, k1));
			else {
				String indexName = genBeforeUse(strings, (Temp)dstAddr, a1, a0);
				strings.add("\tswrr\t"+regNames[k0]+", "+genAddress(dstAddrName,indexName));
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
		LinkedList<Asm> strings = new LinkedList<Asm>();

		/*if (!dst.spilled())
			strings.add("\tla\t"+dst.gen()+", "+src.gen());
		else {	// dst.spilled, src must be loaded before sw
			genWhenSpillDst(strings, dst.addr, dst.index, src);
		}*/
		
		return strings;
	}

}
