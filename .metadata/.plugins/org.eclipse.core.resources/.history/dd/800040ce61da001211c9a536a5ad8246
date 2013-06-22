package roxanne.codegen;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.LinkedList;

import roxanne.asm.Asm;
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
	public void gen(LinkedList<Asm> asms) throws Error {
		lines.add("\t.text");
		lines.add("\t.globl main");
		
		for (Asm a: asms) {
			lines.add(a.codeGen());
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


