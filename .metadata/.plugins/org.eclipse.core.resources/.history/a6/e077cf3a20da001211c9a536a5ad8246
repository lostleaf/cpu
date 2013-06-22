package roxanne.quad;

import java.util.LinkedList;

import roxanne.addr.Const;
import roxanne.addr.Label;
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

	public LinkedList<String> gen() throws Error {
		LinkedList<String> strings = new LinkedList<String>();
		/*
		 * restore callee saved register (ra?)
		 * restore sp
		 * jr
		 */
		if (level.savera)
			strings.add("\tlw\t$ra, "+"($fp)");
		
		// index here will not overflow
		if (label.name != Symbol.symbol("main") && level.getUsedRegisters() != null)
			for (Integer i: level.getUsedRegisters()) {
				strings.add("\tlw\t"+regNames[i]+", "+Quad.genAddress(strings, new Const((2+i-baseOfSavedRegisters)*wordSize), "$fp", k0));			
			}
		if (label.name != Symbol.symbol("main"))  {
			strings.add("\tlw\t$fp, "+Quad.genAddress(strings, new Const(wordSize), "$fp", k0));
			int size = level.paraSize()+level.size();
			if (minInt <= size && size <= maxInt)
				strings.add("\t"+getOpI(OpType.PLUS)+"\t$sp, $sp, "+size);
			else {
				String s = genBeforeUseConst(strings, new Const(size), k0, ConstMode.ALU);
				strings.add("\t"+getOp(OpType.PLUS)+"\t$sp, $sp, "+s);
			}
			strings.add("\tjr\t$ra");
		}
		
		return strings;
	}

	public boolean isJump() { return true; }

	//for DefReach
	public boolean isDef() {
		return false;
	}
}
