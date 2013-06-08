package roxanne.quad;

import java.util.LinkedHashSet;
import java.util.LinkedList;

import roxanne.addr.*;
import roxanne.error.Error;

public class Iftrue extends Quad {
	// bnez Rsrc, label => need to li if cond is const
	public Temp cond;
	public LABEL label;

	public Iftrue(Temp t, LABEL lbl) {
		cond = t;
		label = lbl;
	}

	public String toString() {
		return "bneqz "+cond+", "+label+super.toString();
	}

	public LABEL jumpLABEL() {
		return label;
	}
	public boolean isJump() { return true; }

	public LinkedHashSet<Temp> use() {
		LinkedHashSet<Temp> set = new LinkedHashSet<Temp>();
		if (cond instanceof Temp)
			addUse(set, (Temp) cond);
		return set;
	}

	@Override
	public LinkedList<String> gen() throws Error {
		LinkedList<String> strings = new LinkedList<String>();

		String c = genBeforeUse(strings, cond, k0, k1);
		strings.add("\tbnez\t"+c+", "+label.label.gen());

		return strings;
	}


	//for DefReach
	public boolean isDef() {
		return false;
	}
}
