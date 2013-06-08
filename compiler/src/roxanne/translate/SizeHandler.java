package roxanne.translate;

import java.util.LinkedList;

import roxanne.addr.Const;
import roxanne.ast.BExpr;
import roxanne.ast.Character;
import roxanne.ast.Expr;
import roxanne.ast.Expr.OpType;
import roxanne.ast.Num;
import roxanne.ast.UExpr;
import roxanne.quad.Quad;

public class SizeHandler {
	private static int bool2bool(Expr.OpType op, int l, int r){
		boolean bl = (l==0)? false:true, br = (r==0)? false:true;
		switch(op) {
		case AND: return (bl&&br == true)? 1:0;
		case OR : return (bl||br == true)? 1:0;
		case NOT: return (!bl == true)? 1:0;
		default: //System.out.println("op error in bool2bool");
			return -1;
		}
	}
	
	private static int int2bool(Expr.OpType op, int l, int r) {
		switch(op) {
		case EQ: return (l==r)? 1:0;
		case NE: return (l!=r)? 1:0;
		case GT: return (l>r)? 1:0;
		case LT: return (l<r)? 1:0;
		case GE: return (l>=r)? 1:0;
		case LE: return (l<=r)? 1:0;
		default: return -1;
		}
	}
	
	public static Const calcB(Const l, OpType op, Const r) {
		Integer i = null;
		Integer ll = l.value, rr = r.value;
		switch(op) {
		// case cast unconsidered
		case BITOR:	 i = ll|rr; break;
		case BITAND: i = ll&rr; break;
		case BITXOR: i = ll^rr; break;
		case SHL: 	 i = ll<<rr; break;
		case SHR: 	 i = ll>>rr; break;
		case PLUS:	 i = ll+rr; break;
		case MINUS:  i = ll-rr; break;
		case TIMES:  i = ll*rr; break;
		case DIVIDE: i = ll/rr; break;
		case MOD:	 i = ll%rr; break;
		case AND: case OR: i = bool2bool(op,ll, rr); break;
		case EQ: case NE: case LT: case GT: case LE: case GE:
			i = int2bool(op, ll, rr); break;
		}
		return new Const(i);
	}
	

	public static Const calcU(OpType op, Const c) {
		Integer i = null, n = c.value;
		switch(op) {
		//sizeofE sizeofT unconsidered
		case UMINUS: i = -n; break;
		case NOT: 	 i = bool2bool(op,n,0); break;
		case BITNOT: i = ~n; break;
		case UPLUS:	 i = n; break;
		}
		return new Const(i);
	}
	
	private static void calcIfConstExpr(Expr e) {
		//incompelete version
		if (e instanceof Num)
			e.addr = new Const(((Num)e).value);
		else if (e instanceof Character)
			e.addr = new Const((int) ((Character)e).c);
		else if (e instanceof BExpr) {
			Expr l = ((BExpr)e).left, r = ((BExpr)e).right;
			calcIfConstExpr(l);
			calcIfConstExpr(r);
			if ((l.addr != null) && (r.addr != null))
				e.addr = calcB((Const)l.addr, ((BExpr)e).op, (Const)r.addr);
		} else if (e instanceof UExpr) {
			Expr u = ((UExpr)e).e;
			calcIfConstExpr(u);
			if (u.addr != null)
				e.addr = calcU(((UExpr)e).op, (Const)u.addr);
		} else
			e.addr = null;			
	}
	
	public static LinkedList<Quad> transExpr(Expr e, Level lvl) throws Error {
		// incompelete version??
		//calculate when is constant expr
		calcIfConstExpr(e);
		if (e.addr == null)
			throw new Error("e is not constant (in transExpr(Expr, Level))");
		return null;
	}
}
