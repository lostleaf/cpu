package roxanne.quad;

import roxanne.addr.*;
import roxanne.ast.Expr.OpType;
import roxanne.error.Error;
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

	// for codegen
	public LinkedList<String> gen() throws Error {
		LinkedList<String> strings = new LinkedList<String>();
		
		if (outOfBound(level.paraSize()+level.size(), ConstMode.ALU)) {
			String s1 = genBeforeUseConst(strings, new Const(level.paraSize()+level.size()), k0, ConstMode.ALU);
			strings.add("\t"+getOp(OpType.MINUS)+"\t$sp, $sp, "+s1);
		} else 
			strings.add("\t"+getOpI(OpType.MINUS)+"\t$sp, $sp, "+(level.paraSize()+level.size()));
		
		if (label.name != Symbol.symbol("main")) {
			int size2 = level.size()-wordSize;
			if (outOfBound(size2, ConstMode.PCOFFSET)){
				genBeforeUseConst(strings, new Const(size2), k0, ConstMode.PCOFFSET);
				//strings.add("\tadd\t"+regNames[k0]+", $sp, "+regNames[k0]);
				strings.add("\tsw\t$fp, "+"$sp($k0)");
			} else
				strings.add("\tsw\t$fp, "+size2+"($sp)");
		}
		
		if (outOfBound(level.size(), ConstMode.ALU)) {
			String s3 = genBeforeUseConst(strings, new Const(level.size()), k0, ConstMode.ALU);
			strings.add("\t"+getOp(OpType.PLUS)+"\t$fp, $sp, "+s3);
		} else
			strings.add("\t"+getOpI(OpType.PLUS)+"\t$fp, $sp, "+level.size());

		if (label.name != Symbol.symbol("main"))
			strings.add("\tsw\t$ra, ($fp)");
		// callee saved registers according to the regalloc
		// the const here will not overflow
		if (label.name != Symbol.symbol("main") && level.getUsedRegisters() != null)
			for (Integer i: level.getUsedRegisters()) {
				strings.add("\tsw\t"+regNames[i]+", "+genAddress(strings, new Const(((2+i-baseOfSavedRegisters)*wordSize)), "$fp", k0));			
			}

		for (Temp para: paras) {
			if (!para.spilled() && out.contains(para))
				strings.add("\tlw\t"+para.gen()+", "+genAddress(strings, (Const)para.index, ((Temp)para.addr).gen(), k0));
		}

		return strings;
	}

	//for DefReach
	public boolean isDef() {
		return true;
	}
}
