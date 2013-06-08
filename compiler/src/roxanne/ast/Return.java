package roxanne.ast;

public class Return extends Stmt {
	public ExprList values;
	public Return(ExprList e) {
		values = e;
	}
}
