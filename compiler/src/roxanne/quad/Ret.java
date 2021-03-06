package roxanne.quad;

import java.util.LinkedHashSet;
import java.util.LinkedList;

import roxanne.addr.*;
import roxanne.asm.Asm;
import roxanne.error.Error;
import roxanne.symbol.Symbol;
import roxanne.translate.Level;

public class Ret extends Quad {
	public Addr value;
	private Level level;
	private Label label;

	public Ret(Addr t, Level lvl, Label lbl) {
		value = t;
		level = lvl;
		label = lbl;
	}

	public String toString() {
		return "Ret "+value+super.toString(); 
	}

	public boolean isJump() { return true; }

	public LinkedHashSet<Temp> use() {
		LinkedHashSet<Temp> set = new LinkedHashSet<Temp>();
		if (value instanceof Temp)
			addUse(set, (Temp)value);
		return set;
	}

	@Override
	public LinkedList<Asm> gen() throws Error {
		LinkedList<Asm> asms = new LinkedList<Asm>();
		if (value == null|| label.name == Symbol.symbol("main")) return asms;
		
		throw new Error("label is not main");
		/*if (value instanceof Const) {
			if (outOfBound(((Const)value).value, ConstMode.LI))
				genBeforeUseConst(asms, (Const)value, level, ConstMode.LI);
			else
				asm.add(new Asm(Op.li, Temp.v0, value));
				strings.add("\tli\t$v0, "+value.gen());
			
			return strings;
		}

		// value is a temp
		if (((Temp)value).spilled())
			genBeforeUse(strings, (Temp)value, v0, k0);
		else
			strings.add("\tmove\t$v0, "+value.gen());

		Leave leave = new Leave(level, label);
		strings.addAll(leave.gen());

		return strings;*/
	}

	//for DefReach
	public boolean isDef() {
		return false;
	}	

}
