package roxanne.quad;

import java.util.LinkedList;

import roxanne.addr.Label;
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
	public LinkedList<String> gen() throws Error {
		LinkedList<String> strings = new LinkedList<String>();
		strings.add("\tj\t"+label.label.gen());
		return strings;
	}

	//for DefReach
	public boolean isDef() {
		return false;
	}
	
}
