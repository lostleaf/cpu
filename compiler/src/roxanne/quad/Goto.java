package roxanne.quad;

import java.util.LinkedList;

import roxanne.addr.Label;
import roxanne.asm.Asm;
import roxanne.asm.Asm.Op;
import roxanne.error.Error;

public class Goto extends Quad {
	public LABEL label;
	
	public Goto(LABEL l) {
		label = l;
	}
	
	public String toString() {
		return "Goto "+label+super.toString();
	}
	
	public boolean isJump() { return true; }
	
	public LABEL jumpLABEL() {
		return label;
	}

	@Override
	public LinkedList<Asm> gen() throws Error {
		LinkedList<Asm> asms = new LinkedList<Asm>();
		asms.add(new Asm(Op.j, label.label, null, null));
		return asms;
	}

	//for DefReach
	public boolean isDef() {
		return false;
	}
	
}
