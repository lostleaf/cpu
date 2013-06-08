package roxanne.ast;

import roxanne.addr.*;
import roxanne.types.TYPE;


public abstract class Expr extends Stmt {
	// for normal variable whether it's an lv or not makes no difference
	// only for case a[b](not of type ARRAY), and for case (a.b, a->b,b )(all of type structunion) does lv makes differences
	// lv means the addr is the address of the variable instead of it's value
	public TYPE type = null;
	public Addr addr = null;	// can be fp, gp, calculated pointer result (temp), pointer's initial value (const);
	public Addr index = null; 
	//public Const index = null;
	public static enum OpType {
		PLUS, MINUS, TIMES, DIVIDE, MOD, EQ, NE, LT, GT, LE, GE,
		ASSIGN, POS, DOT, PTR, BITOR, BITXOR, BITAND, BITNOT, NOT, OR, AND,
		MULASSIGN, DIVASSIGN, MODASSIGN, ADDASSIGN, SUBASSIGN, ANDASSIGN, ORASSIGN, XORASSIGN, SHLASSIGN, SHRASSIGN,
		LDEC, LINC, RINC, RDEC, ARRAY, SHR, SHL,
		CAST, GET, UMINUS, UPLUS, SIZEOFE, SIZEOFT,
		NULL
	}
}
