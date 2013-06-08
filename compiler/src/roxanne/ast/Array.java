package roxanne.ast;

public class Array extends Dector {
	public Dector name;
	public ExprList indexlist;
	public Array(Dector d, ExprList e) {
		name = d;
		indexlist = e;
	}
}
