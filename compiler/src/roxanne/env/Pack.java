package roxanne.env;

import roxanne.symbol.Symbol;
import roxanne.types.TYPE;

public class Pack{
	public roxanne.types.TYPE idtype;
	public Symbol sym;
	public roxanne.types.TYPE inittype;
	public Pack(TYPE t, Symbol s, roxanne.types.TYPE t2) { idtype = t; sym = s; inittype = t2;}
	public Pack() { idtype = null; sym = null; inittype = null; }
	public void print() {
		//System.out.println("in print");
		//System.out.println(sym.toString()+", idtype: "+idtype.toString()+ ",inittype: "+inittype.toString());
		String s = new String("");
		if (sym!=null) s = s+sym.toString()+", ";
		else s = s+"sym==null, ";
		if (idtype != null) s = s+idtype.toString()+", ";
		else s=s+"idtype == null, ";
		if (inittype!=null) s = s+inittype.toString()+", ";
		else s = s+"inittype == null";
		System.out.println(s);
	}
}
