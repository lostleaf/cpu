package roxanne.codegen;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.LinkedList;

import roxanne.ast.Expr.OpType;
import roxanne.error.Error;
import roxanne.quad.*;
import roxanne.symbol.Symbol;
import roxanne.translate.CompilationUnit;
import roxanne.util.Constants;
public class Codegen implements Constants {
	public LinkedList<String> lines = new LinkedList<String>();
	
	//public gen(linkedList<CompilationUnit> )
	// private the below one
	public void gen(LinkedList<CompilationUnit> units) throws Error {
		Iterator<CompilationUnit> iter = units.iterator();
		// deal with global and main;
		assert(iter.hasNext());
			CompilationUnit global = iter.next();
		assert(iter.hasNext());
			CompilationUnit main = iter.next();	
		lines.add("\t.text");
		lines.add("\t.globl main");
		
		Iterator<Quad> quadIter = main.quads.iterator();
		assert(quadIter.hasNext());
		lines.addAll(quadIter.next().gen());
		if (global.level.size()!=0) {
			int size = (global.level.size()+wordSize);				// - wordSize??
			if (size > maxInt) {
				lines.add("\t"+Quad.getOpI(OpType.PLUS)+"\t$gp, $gp, "+maxInt);
				size -=maxInt;
			}
			lines.add("\taddiu\t$gp, $gp, "+size);
		}
		for (Quad quad:global.quads) 
			lines.addAll(quad.gen());
		while (quadIter.hasNext())
			lines.addAll(quadIter.next().gen());
		lines.add("\n");
		
		CompilationUnit unit = null;
		while (iter.hasNext()) {
			unit = iter.next();
			for(Quad quad:unit.quads) {
				lines.addAll(quad.gen());
			}
			lines.add("\n");	
		}
	}
	
	
	public void genAll(LinkedList<String> list) {
		lines.addAll(list);
	}
	
	public void gen(String line) {
		lines.add(line);
	}
	
	public void flush() {
		for(String line: lines) {
			System.out.println(line);
		}
	}
	
}


