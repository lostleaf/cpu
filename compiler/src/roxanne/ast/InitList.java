package roxanne.ast;

public class InitList extends Expr {
	public ExprList list;
	public InitList(ExprList el) {
		list = el;
	}
}
