package roxanne.quad;

import roxanne.addr.*;
import roxanne.asm.Asm;
import roxanne.asm.Asm.Op;
import roxanne.ast.Expr.OpType;
import roxanne.error.Error;
import roxanne.quad.Quad.ConstMode;
import roxanne.symbol.Symbol;
import roxanne.translate.*;
import java.util.*;

public class Enter extends Quad {
	public Label label;
	public Level level;				// callee's level
	public LinkedList<Temp> paras;	// the temp bind to paras in function label
	public boolean vara = false;

	public Enter(Label lb, Level lv, LinkedList<Temp> temps) {
		label = lb;
		level = lv;
		paras = temps;
	}
	public Enter(Label lb, Level lv, LinkedList<Temp> temps, boolean b) {
		label = lb;
		level = lv;
		paras = temps;
		vara = b;
	}

	public String toString() {
		return "Enter "+ label.toString()+ 
				"(para size = " + ((paras!=null)?paras.size(): 0)+ ((vara)? " vara":" fixed")+" )"
				+super.toString();
	}
	
	public LinkedHashSet<Temp> def() {
		return new LinkedHashSet<Temp>(paras);
	}

	// for asmgen
	public LinkedList<Asm> gen() throws Error {
		LinkedList<Asm> asms = new LinkedList<Asm>();

		if (label.name != Symbol.symbol("main"))
			if (outOfBound(level.paraSize()+level.size(), ConstMode.ALU)) {
				Temp s1 = (Temp)genBeforeUseConst(asms, new Const(level.paraSize()+level.size()), level, ConstMode.ALU);
				asms.add(new Asm(getOp(OpType.MINUS), Temp.sp, Temp.sp, s1));
			} else 
				asms.add(new Asm(getOpI(OpType.MINUS), Temp.sp, Temp.sp, new Const((level.paraSize()+level.size()))));

		/*if (label.name != Symbol.symbol("main")) {
			int size2 = level.size()-wordSize;
			if (outOfBound(size2, ConstMode.PCOFFSET)){
				genBeforeUseConst(asms, new Const(size2), level, ConstMode.PCOFFSET);
				//asms.add("\tadd\t"+regNames[k0]+", $sp, "+regNames[k0]);
				asms.add("\tsw\t$fp, "+"$sp($k0)");
			} else
				asms.add("\tsw\t$fp, "+size2+"($sp)");
		}*/
		
		// when label.name == main
		/*Addr s3 = genBeforeUseConst(asms, new Const(level.size()), level, ConstMode.ALU);
		if (s3 instanceof Temp) {
			asms.add(new Asm(getOp(OpType.PLUS), Temp.fp, Temp.sp, s3));
		} else
			asms.add(new Asm(getOpI(OpType.PLUS), Temp.fp, Temp.sp, s3));
	*/
		if (label.name != Symbol.symbol("main"))
			asms.add(new Asm(Op.sw, Temp.ra, Temp.fp, new Const(0)));
		// callee saved registers according to the regalloc
		// the const here will not overflow
		/*if (label.name != Symbol.symbol("main") && level.getUsedRegisters() != null)
			for (Integer i: level.getUsedRegisters()) {
				asms.add("\tsw\t"+regNames[i]+", "+genAddress(asms, new Const(((2+i-baseOfSavedRegisters)*wordSize)), "$fp", k0));			
			}

		for (Temp para: paras) {
			if (!para.spilled() && out.contains(para))
				asms.add("\tlw\t"+para.gen()+", "+genAddress(asms, (Const)para.index, ((Temp)para.addr).gen(), k0));
		}*/

		return asms;
	}

	//for DefReach
	public boolean isDef() {
		return true;
	}
}
