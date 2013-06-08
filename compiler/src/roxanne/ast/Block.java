package roxanne.ast;

public class Block extends Stmt {
	public DefList decs;
	public StmtList stmts;
	public Block(DefList dl, StmtList sl) {
		decs = dl;
		stmts = sl;
	}
}
