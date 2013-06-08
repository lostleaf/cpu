package roxanne.types;

import roxanne.symbol.Symbol;
import roxanne.addr.*;

public final class PARAS extends TYPE {
	public boolean vara;
	public RECORD paras;
	public PARAS() { paras = null; vara = false; }
	public PARAS(RECORD r, boolean b) { paras = r; vara = b; }
	public PARAS(RECORD r) { paras = r; vara = false; }
	public TYPE actual() {
		if (paras.actual() instanceof WRONG) return new WRONG();
		return this;
	}
	public boolean equals(PARAS p) {
		if (this == p) return true;
		if (vara != p.vara) return false;
		if ((paras==null)^(p.paras==null)) return false;
		if (paras==null && p.paras==null) return true;
		return paras.equals(p.paras);
	}
	
	public void append(TYPE t, Symbol s) {
		paras.append(t,s);
	}
	public String toString() {
		StringBuffer s = new StringBuffer();
		if (vara) s.append("vara\n\t");
		else s.append("fixed\n\t");
		if (paras == null) s.append("paras == null");
		else s.append(paras.toString());
		return s.toString();
	}
	
	public Addr getWidth() {return null;}
}
