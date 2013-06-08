package roxanne.types;

import roxanne.addr.*;

public class POINTER extends TYPE {
	public TYPE eletype;
	public POINTER() { eletype = null; width = new Const(4);}
	public POINTER(TYPE t) { eletype = t; width = new Const(4);}
	public TYPE actual() {
		if (eletype.actual() instanceof WRONG) return new WRONG();
		return this;
	}
	public boolean equals(POINTER p) {
		if (this == p) return true;
		if (eletype.equals(p.eletype)) return true;
		return false;
	}
	public String toString() {
		if (eletype instanceof STRUCT) return "struct *"+((STRUCTUNION)eletype).name.toString();
		if (eletype instanceof UNION) return "UNION *"+((STRUCTUNION)eletype).name.toString();
		return "*"+eletype.toString();
	}
	public Const getWidth() {return (Const)width;}
}
