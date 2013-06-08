package roxanne.ast;


public class UExpr extends Expr {
	public Expr e;
	public OpType op;
	public UExpr(OpType opp, Expr r) {
		e = r;
		op = opp;
	}
}
