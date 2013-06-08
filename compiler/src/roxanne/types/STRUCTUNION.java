package roxanne.types;

import java.util.LinkedList;

import roxanne.quad.Quad;
import roxanne.symbol.Symbol;
import roxanne.translate.Level;
import roxanne.addr.*;
import roxanne.error.Error;

public class STRUCTUNION extends TYPE {
	public RECORD record;
	public Symbol name;
	//public static Const psize = new Const(4);
	
	public STRUCTUNION() { record = null; name = null; }
	public STRUCTUNION(String n, RECORD r) { name = Symbol.symbol(n); record = r; }
	public STRUCTUNION(Symbol n, RECORD r) { name = n; record = r; }
	public TYPE actual() {
		if (record == null) return new WRONG();
		return this;
	}
	// equals means refer to the same instance;
	public boolean equals(STRUCTUNION su) {
		return this == su;
	}

	public TYPE isField(String s) {return record.isField(s); }
	
	public Addr getIndex(String name) {
		if (this instanceof UNION) return new Const(0); 
		return record.getIndex(name);
	}
	
	public String toString() {
		StringBuffer str = new StringBuffer();
		if (this instanceof STRUCT) str.append("\n\t--Struct--\n\t");
		else str.append("\n\t--Union--\n\t");
		if (name == null) str.append("name == null:");
		//else str.append(name.toString()+":\n\t");
		if (record == null) str.append("\n\trecord == null");
		else str.append("\n\t"+record.toString());
		if (width != null)
			str.append("\n\t width = "+ width);
		str.append("\n\t--end of StructUion--");
		return str.toString();
	}
	public Addr getWidth() {
		return width;
	}
	public LinkedList<Quad> createWidthAndIndex(Level l) throws Error {
		if (record == null) return null;
		boolean struct = (this instanceof STRUCT)? true: false;
		LinkedList<Quad> list = record.createWidthAndIndex(l, struct);
		width = record.getWidth();
		return list;
	}
}
