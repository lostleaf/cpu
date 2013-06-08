package roxanne.ast;

public class ExprList extends Expr {
	public Expr head;
	public ExprList tail;
	public ExprList(Expr h, ExprList i2) {
		head = h;
		tail = i2;
	}
}
