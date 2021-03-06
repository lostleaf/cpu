package roxanne.quad;

import roxanne.addr.*;
import roxanne.asm.Asm;
import roxanne.symbol.Symbol;
import roxanne.translate.Level;

import java.util.*;

public class CallFunc extends CallProc {
	public Temp ret;
	
	public CallFunc(LABEL lbl, Level lvl, Temp t, LinkedList<Addr> addrs) {
		super(lbl, lvl, addrs);
		ret = t;
	}
	
	public String toString() {
		return ret+" = "+ super.toString();
	}
	
	public LinkedHashSet<Temp> def() {
		LinkedHashSet<Temp> set = new LinkedHashSet<Temp>();
		addDef(set, ret);
		return set;
	}
	
	public LinkedHashSet<Temp> use() {
		LinkedHashSet<Temp> set = super.use();
		addDefToUse(set, ret);
		return set;
	}
	public boolean isJump() { 
		if (label.label.name == Symbol.symbol("malloc"))
			return false;
		return true; 
	}
	
	public LinkedList<String> genMalloc() {
		LinkedList<String> strings = new LinkedList<String>();
		
		Addr num = paras.getFirst();
		
		if (num instanceof Const) {
			if (outOfBound(((Const)num).value, ConstMode.LI))
				genBeforeUseConst(strings, (Const)num, a0, ConstMode.LI);
			else
				strings.add("\tli\t$a0, "+ ((Const)num).value);
		} else {
			if (! ((Temp)num).spilled())
				strings.add("\tmove\t$a0, "+num.gen());	// so far??
			else {
				genBeforeUse(strings, (Temp)num, a0, k0);
			}
		}
		
		strings.add("\tli\t$v0, 9");
		strings.add("\tsyscall");
		
		if (ret.mustBeSpilled() || ret.getInterval() != null)
			if (!ret.spilled()) {
				strings.add("\tmove\t"+ret.gen()+", $v0");
			} else {
				Move.genWhenSpillDst(strings, ret.addr, ret.index, "$v0");
			}		
		return strings;	
	}
	
	public LinkedList<Asm> gen() {
		if (label.label.name == Symbol.symbol("malloc"))
			return genMalloc();
		
		LinkedList<Asm> strings = super.gen();
		if (ret.mustBeSpilled() || ret.getInterval() != null)
			if (!ret.spilled()) {
				strings.add("\tmove\t"+ret.gen()+", $v0");
			} else {
				Move.genWhenSpillDst(strings, ret.addr, ret.index, "$v0");
			}
		return strings;
	}
	
	
	//for DefReach
	public boolean isDef() {
		return true;
	}
}
