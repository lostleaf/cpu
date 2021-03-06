package roxanne.types;

import roxanne.addr.Addr;

import roxanne.addr.Temp;
import roxanne.quad.*;
import roxanne.addr.Const;
import roxanne.ast.Expr;
import roxanne.ast.Expr.OpType;
import roxanne.ast.Num;
import roxanne.error.Error;
import roxanne.translate.*;
import java.util.*;

public final class ARRAY extends POINTER{
	public Expr capacity;
	public Addr size = null;
	public ARRAY() { super(); capacity = null; width = new Const(wordSize);}
	public ARRAY(TYPE t, Expr i) { super(t); capacity = i; width = new Const(wordSize);}
	public ARRAY(TYPE t, Integer i) {
		super(t);
		capacity = new Num(i);
	}
	public TYPE actual() {
		if (capacity!=null && !(super.actual() instanceof WRONG)) return this;
		return new WRONG();
	}
	public boolean equals(ARRAY a) {
		return super.equals(a);
	}
	
	public String toString() {
		return "("+eletype.toString()+"<-"+capacity.toString()+" ( size = "+size+" ))";
	}
	public Const getWidth() {return (Const) width;}
	public LinkedList<Quad> calcSize(Level l) throws Error {
		if (size != null) return null;
		LinkedList<Quad> quads = new LinkedList<Quad> ();
		LinkedList<Quad> list = null;
		Addr elewidth = null;
		
		list = SizeHandler.transExpr(capacity, l);
		
		if (list != null)
			quads.addAll(list);
		
		if (eletype instanceof ARRAY) {
			list = ((ARRAY) eletype).calcSize(l);
			if (list != null)
				quads.addAll(list);
			elewidth = ((ARRAY)eletype).size;
		} else {
			if (eletype instanceof STRUCTUNION) {
				list = ((STRUCTUNION)eletype).createWidthAndIndex(l);
				if (list != null)
					quads.addAll(list);
			}
			elewidth = eletype.getWidth();
		}
		
		size = Addr.biop(elewidth, OpType.TIMES, capacity.addr, l);
		if (!(size instanceof Const))
			quads.add(Quad.makeBiop((Temp)size, elewidth, OpType.TIMES, capacity.addr));
		
		return (quads.size()==0)? null: quads;
	}
}
