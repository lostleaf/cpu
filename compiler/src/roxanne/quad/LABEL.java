package roxanne.quad;

import java.util.LinkedList;

import roxanne.addr.*;
import roxanne.asm.Asm;
import roxanne.asm.Asm.Op;


public class LABEL extends Quad {
	public Label label;

	public LABEL(Label l){
		label = l;
	}

	public LABEL() {
		label = new Label();
	}

	public String toString() {
		return label.toString()+super.toString();
	}

	public LinkedList<Asm> gen() {
		LinkedList<Asm> asms = new LinkedList<Asm>();
		asms.add(new Asm(Op.label, label, null,null));
		return asms;
	}

	//for DefReach
	public boolean isDef() {
		return false;
	}
}
