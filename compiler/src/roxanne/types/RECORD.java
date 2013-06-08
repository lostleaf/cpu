package roxanne.types;
import java.util.Iterator;
import roxanne.ast.*;
import roxanne.quad.Biop;
import java.util.LinkedList;

import roxanne.addr.Const;
import roxanne.addr.Temp;
import roxanne.quad.Quad;
import roxanne.symbol.*;
import roxanne.translate.Level;
import roxanne.translate.Translator;
import roxanne.addr.*;
import roxanne.ast.Expr.OpType;
import roxanne.error.Error;


public class RECORD extends TYPE {
	public class RECORDFIELD {
		/*
		 * every RECORD FIELD is bound to a temp when declared
		 */
		public TYPE type;
		public Symbol name;
		public Addr index = null;
		
		RECORDFIELD(TYPE t, String s) {
			type = t;
			name = Symbol.symbol(s);
		}
		RECORDFIELD() { type = null; name = null; }
		RECORDFIELD(TYPE t, Symbol s) {
			type = t; name = s;
		}
		public String toString() {
			StringBuffer str = new StringBuffer();
			if (type instanceof STRUCT) str.append("struct "+((STRUCTUNION)type).name.toString());
			else if (type instanceof STRUCT) str.append("union "+((STRUCTUNION)type).name.toString());
			else str.append(type.toString());
			str.append(" "+name.toString());
			return str.toString();
		}
	}
	
	
	public LinkedList<RECORDFIELD> fields;
	

	public RECORD() { fields = new LinkedList<RECORDFIELD>(); }
	public RECORD(LinkedList<RECORDFIELD> l) { fields = l; }
	public TYPE actual() {
		Iterator<RECORDFIELD> iter = fields.iterator();
		while(iter.hasNext()) {
			RECORDFIELD f = iter.next();
			TYPE t = f.type;
			if (t.actual() instanceof WRONG) return new WRONG();
		}
		return this;
	}
	public boolean equals(RECORD r) {
		if (this == r) return true;
		Iterator<RECORDFIELD> iter1 = fields.iterator(), iter2 = r.fields.iterator();
		if (fields.size() != r.fields.size()) return false;
		while(iter1.hasNext()) {
			TYPE t1 = iter1.next().type, t2 = iter2.next().type;
			if (!t1.equals(t2)) return false;
		}
		return true;
	}
	public void append(TYPE t, Symbol s) {
		RECORDFIELD rf = new RECORDFIELD(t,s);
		fields.add(rf);
	}

	public String toString() {
		StringBuffer m = new StringBuffer("{ ");
		Iterator<RECORDFIELD> iter = fields.iterator();
		while(iter.hasNext()) {
			m.append(iter.next().toString()+"; ");
		}
		m.append("}");
		return m.toString();
	}

	public TYPE isField(String str) {
		Symbol name = Symbol.symbol(str);
		Iterator<RECORDFIELD> iter = fields.iterator();
		while(iter.hasNext()) {
			RECORDFIELD r = iter.next();
			if (name == r.name) { 
				return r.type;
			}
		}
		return null;
	}
	
	Addr getIndex(String s) {
		Symbol name = Symbol.symbol(s);
		Iterator<RECORDFIELD> iter = fields.iterator();
		RECORDFIELD r = null;
		while (iter.hasNext()) {
			r = iter.next();
			if (r.name == name)
				return r.index;
		}
		return null;
	}
	public int size() {
		return fields.size();
	}
	public Addr getWidth() {
		return width;
	}

	LinkedList<Quad> createWidthAndIndex(Level l, boolean struct) throws Error {
		if (width != null) return null;
		width = new Const(0);
		Addr lastwidth = null;
		LinkedList<Quad> quads = new LinkedList<Quad> ();
		LinkedList<Quad> list = null;
		Iterator<RECORDFIELD> iter = fields.iterator();
		while (iter.hasNext()) {
			RECORDFIELD r = iter.next();
			r.index = lastwidth = width;
			Addr add = null;
			if (r.type instanceof STRUCTUNION || r.type instanceof ARRAY) {
				if (r.type instanceof STRUCTUNION) {
					list = ((STRUCTUNION) r.type).createWidthAndIndex(l);
					add = ((STRUCTUNION)r.type).getWidth();
				} else {
					list = ((ARRAY)r.type).calcSize(l);
					add = ((ARRAY)r.type).size;
				}
				/*add = Addr.biop(size, OpType.PLUS,psize, l);
				if (add instanceof Temp)
					list.add(Quad.makeBiop((Temp)add, size, OpType.PLUS, psize));*/
			} else {
				add = r.type.getWidth();
			}
			
			if (list != null)
				quads.addAll(list);
			
			if (struct) {
				width = Addr.biop(lastwidth, OpType.PLUS, add, l);
				if (width instanceof Temp)
					quads.add(Quad.makeBiop((Temp)width, add, OpType.PLUS, lastwidth));
			} else 
				if (((Const)width).value < ((Const)add).value)
					width = add;
		}
		if (quads.size() == 0) return null;
		else return quads;
	}
}
	// inner class (private | public ...) operator
	// inner class (private | public ...) operator