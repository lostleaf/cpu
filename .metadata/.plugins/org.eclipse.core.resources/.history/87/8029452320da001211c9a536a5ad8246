package roxanne.quad;

import java.util.LinkedList;

import roxanne.addr.*;;


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

	public LinkedList<String> gen() {
		LinkedList<String> strings = new LinkedList<String>();
		strings.add(label+":");
		return strings;
	}

	//for DefReach
	public boolean isDef() {
		return false;
	}
}
