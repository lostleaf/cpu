package roxanne.addr;

import roxanne.translate.Level;
import roxanne.translate.SizeHandler;
import roxanne.quad.*;
import roxanne.ast.Expr.OpType;
import roxanne.error.Error;

public abstract class Addr {
	public static Addr biop(Addr l, OpType op, Addr r, Level level) throws Error {
		// quad is appended outside the function
		if (r == null)
			if (op == OpType.PLUS)
				return l;
			else {
				throw new Error("r == null && op != '+'(in Addr.biop)");
			}
		if (l instanceof Const && r instanceof Const) {
			return SizeHandler.calcB((Const)l, op, (Const)r);
		} else {
			return level.newTemp(new Const(4));
		}
	}
	
	public static Addr uop(OpType op, Addr e, Level level) {
		if (e instanceof Const)
			return SizeHandler.calcU(op, (Const)e);
		else return level.newTemp(new Const(4));
	}
	
	public abstract String gen();
	
}
