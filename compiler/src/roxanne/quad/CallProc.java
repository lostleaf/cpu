package roxanne.quad;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import roxanne.addr.*;
import roxanne.asm.Asm;
import roxanne.symbol.Symbol;
import roxanne.translate.Level;
import roxanne.translate.Translator;

public class CallProc extends Quad {
	public LABEL label;
	public Level level;		// level of the caller
	public LinkedList<Addr> paras;	// could be temp, also can be const
	
	public CallProc(LABEL lbl, Level lvl,  LinkedList<Addr> addrs) {
		label = lbl;
		level = lvl;
		paras = addrs;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		if (paras != null) {
			Iterator<Addr> iter = paras.iterator();
			sb.append(iter.next().toString());
			while (iter.hasNext()) {
				sb.append(", "+iter.next().toString());
			}
		}
		
		return label+" ("+sb.toString()+")"+super.toString();
	}
	
	
	public boolean isJump() { 
		if (label.label.name == Symbol.symbol("print_int") || label.label.name == Symbol.symbol("print_str"))
			return false;
		return true; 
	}
	
	public LABEL jumpLABEL() {
		/*if (label == null)
			System.out.println("in CALLPROC jumpLABEL: "+label);*/
		return label;
	}
	
	/*
	 * (non-Javadoc)
	 * @see roxanne.quad.Quad#gen()
	 * 
	 * if (para temp and spilled())
	 * 		int regs = find(para);
	 * 		// both are spilled
				String srcName = genBeforeUse(strings, src, regs[0]);
				if (dst.addr instanceof Temp) {
					strings.add("\tsw\t"+srcName+", "+offset+(addr));
				genAfterUse(strings, src, regs[0]);
		else  { para is a const
			int regs[] = getFirst2NotUsed(null, null, null);
			
			genBeforeLoadConst(strings, src, regs[0]);
			if (dst.addr instanceof Temp) {
				strings.add("\tsw\t"+regNames[regs[0]]+", "+genAddress(dst.index, dstAddrName));
			}
			genAfterLoadConst(strings, regs[0]);
		
		}
	 */
	public LinkedList<Asm> gen() {
		if (label.label.name == Symbol.symbol("print_int"))
			return genPrint_int();
		if (label.label.name == Symbol.symbol("print_str"))
			return genPrint_str();
		
		LinkedList<Asm> strings = new LinkedList<Asm> ();
		// put paras
		int offset = level.offset.value;
		Temp addr  = Translator.fpgp(level.parent == null);
		Addr para = null;
		if (paras != null) {
			Iterator<Addr> iter = paras.descendingIterator();
			while (iter.hasNext()) {
				para = iter.next();

				if (para instanceof Temp) {
					Move.genWhenSpillDst(strings, addr, new Const(offset),(Temp)para);
				} else if (para instanceof Label)
					MoveA.genWhenSpillDst(strings, addr, new Const(offset), (Label)para);
				else
					MoveI.genWhenSpillDst(strings, addr, new Const(offset), (Const)para);
				

				if (para instanceof Temp) offset+=((Const)((Temp)para).width).value;
				else offset+=wordSize;
			}
		}
		strings.add("\tjal\t"+label.label.gen());
		return strings;
	}
	
	private LinkedList<String> genPrint_int() {
		LinkedList<String> strings = new LinkedList<String>();
		
		Addr num = paras.getFirst();
		if (num instanceof Const) {
			if (outOfBound(((Const)num).value, ConstMode.LI))
				genBeforeUseConst(strings, (Const)num, a0, ConstMode.LI);
			else
				strings.add("\tli\t$a0, "+ ((Const)num).value);
		} else {
			assert(num instanceof Temp);
			if (! ((Temp)num).spilled())
				strings.add("\tmove\t$a0, "+num.gen());	// so far??
			else {
				genBeforeUse(strings, (Temp)num, a0, a1);
			}
		}
		strings.add("\tli\t$v0, 1");
		strings.add("\tsyscall");
		
		return strings;		
	}
	
	private LinkedList<String> genPrint_str() {
		LinkedList<String> strings = new LinkedList<String>();
		
		Addr addr = paras.getFirst();
		if (addr instanceof Label)
			strings.add("\tla\t$a0, "+addr);
		else {
			if (!((Temp)addr).spilled())
				strings.add("\tmove\t$a0, "+addr.gen());
			else
				genBeforeUse(strings, (Temp)addr, a0, a1);	
		}
		strings.add("\tli\t$v0, 4");
		strings.add("\tsyscall");
		
		return strings;		
	}
	
	public LinkedHashSet<Temp> use() {
		LinkedHashSet<Temp> set = new LinkedHashSet<Temp>();
		if (paras != null)
			for (Addr addr: paras) {
				if (addr instanceof Temp)
					addUse(set,(Temp) addr);
			}
		return set;
	}
	
	//for DefReach
	public boolean isDef() {
		return false;
	}
}
