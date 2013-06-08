package roxanne.translate;

import roxanne.addr.*;
import roxanne.quad.*;
import java.util.*;

public class CompilationUnit {
	public LinkedList<Quad> quads = null;
	public Label label = null;
	public Level level = null;
	
	public CompilationUnit(LinkedList<Quad> q, Label lbl, Level lvl) {
		label = lbl;
		level = lvl;
		quads = q;
	}
	public CompilationUnit() {
		quads = new LinkedList<Quad>();
	}
	
	public String toString() {
		StringBuffer str = new StringBuffer();
		if (label != null)
			str.append("======"+label+"======\n");
		else 
			str.append("======null======\n");
		Iterator<Quad> iter = quads.iterator();
		while (iter.hasNext()) {
			str.append("\t" + iter.next().toString()+"\n");
		}
		if (label!=null)
			str.append("=====end of "+label.toString()+"=====");
		else 
			str.append("=====end of null=====");
		return str.toString();
	}
}
