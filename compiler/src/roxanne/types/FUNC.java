package roxanne.types;

import roxanne.addr.*;

public class FUNC extends TYPE {
	public PARAS paras;
	public TYPE returntype;
	public FUNC(TYPE t, PARAS p) { paras = p; returntype = t; }
	
	public TYPE actual() {
		if (returntype.actual() instanceof WRONG) return new WRONG();
		if (paras != null && paras.actual() instanceof WRONG) return new WRONG();
		return this;
	}
	
	public boolean equals(FUNC fp){
		if (this == fp) return true;
		return returntype.equals(fp.returntype) && paras.equals(fp.paras);
	}
	public String toString() {
		StringBuffer s = new StringBuffer("return: "+returntype.toString()+"\nparas:\n\t");
		if (paras == null) s.append("null");
		else s.append(paras.toString());
		return s.toString();
	}
	public Addr getWidth() {return null;}
}
