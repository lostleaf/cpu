package roxanne.translate;

import java.util.LinkedList;

import roxanne.addr.Label;
import roxanne.quad.LABEL;

public class DataFrag {
	public Label label;
	public String string;
	
	public DataFrag(Label lbl, String str) {
		label = lbl;
		string = str;
	}
	
	public LinkedList<String> gen() {
		LinkedList<String> strings = new LinkedList<String>();
		
		LABEL LBL = new LABEL(label);
		strings.addAll(LBL.gen());
		strings.add("\t.asciiz\t\""+string+"\"");
		
		return strings;
	}
}
