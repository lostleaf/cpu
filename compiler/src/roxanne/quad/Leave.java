package roxanne.quad;

import java.util.LinkedList;

import roxanne.addr.Const;
import roxanne.addr.Label;
import roxanne.addr.Temp;
import roxanne.asm.Asm;
import roxanne.asm.Asm.Op;
import roxanne.ast.Expr.OpType;
import roxanne.error.Error;
import roxanne.symbol.Symbol;
import roxanne.translate.Level;

public class Leave extends Quad {
	public Level level;			// the calleed's level and label
	public Label label;

	public Leave(Level lvl, Label lbl) {
		level = lvl;
		label = lbl;
	}

	public String toString() {
		return "Leave " + label+super.toString();
	}

	public LinkedList<Asm> gen() throws Error {
		LinkedList<Asm> asms = new LinkedList<Asm>();
		/*
		 * restore callee saved register (ra?)
		 * restore sp
		 * jr
		 */
		if (level.savera)
			asms.add(new Asm(Op.lw, Temp.ra, Temp.fp, new Const(0)));
		
		// index here will not overflow
		/*if (label.name != Symbol.symbol("main") && level.getUsedRegisters() != null)
			for (Integer i: level.getUsedRegisters()) {
				asms.add("\tlw\t"+regNames[i]+", "+Quad.genAddress(asms, new Const((2+i-baseOfSavedRegisters)*wordSize), "$fp", k0));			
			}
		if (label.name != Symbol.symbol("main"))  {
			asms.add("\tlw\t$fp, "+Quad.genAddress(asms, new Const(wordSize), "$fp", k0));
			int size = level.paraSize()+level.size();
			if (minInt <= size && size <= maxInt)
				asms.add("\t"+getOpI(OpType.PLUS)+"\t$sp, $sp, "+size);
			else {
				String s = genBeforeUseConst(asms, new Const(size), k0, ConstMode.ALU);
				asms.add("\t"+getOp(OpType.PLUS)+"\t$sp, $sp, "+s);
			}
			asms.add("\tjr\t$ra");
		}*/
		
		return asms;
	}

	public boolean isJump() { return true; }

	//for DefReach
	public boolean isDef() {
		return false;
	}
}
