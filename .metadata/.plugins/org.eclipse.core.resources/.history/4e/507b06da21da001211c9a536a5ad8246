package roxanne.quad;

import java.util.LinkedHashSet;
import java.util.LinkedList;

import roxanne.addr.*;
import roxanne.asm.Asm;
import roxanne.asm.Asm.Op;
import roxanne.error.Error;

public class Iffalse extends Quad {
	// bez, Rsrc, label  => need to li if cond is const
	public Temp cond;
	public LABEL label;

	public Iffalse(Temp t, LABEL lbl) {
		cond = t;
		label = lbl;
	}

	public String toString() {
		return "beqz "+cond+", "+label+super.toString();
	}

	public boolean isJump() { return true; }

	public LABEL jumpLABEL() {
		return label;
	}


	public LinkedHashSet<Temp> use() {
		LinkedHashSet<Temp> set = new LinkedHashSet<Temp>();
		if (cond instanceof Temp)
			addUse(set, (Temp)cond);
		return set;
	}

	@Override
	public LinkedList<Asm> gen() throws Error {
		LinkedList<Asm> asms = new LinkedList<Asm>();
		Temp c = genBeforeUse(asms, cond);
		asms.add(new Asm(Op.beqz, c, label.label, null));
		return asms;
	}

	//for DefReach
	public boolean isDef() {
		return false;
	}
}
