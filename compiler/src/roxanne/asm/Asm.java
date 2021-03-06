package roxanne.asm;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import roxanne.util.Constants;

import roxanne.addr.*;
import roxanne.error.Error;
import roxanne.quad.*;
import roxanne.translate.CompilationUnit;

public class Asm implements Constants{
	public Op op;
	public Addr dst, src1, src2;
	
	public LinkedHashSet<Temp> in = new LinkedHashSet<Temp> ();
	public LinkedHashSet<Temp> out = new LinkedHashSet<Temp>();
	public boolean isLeader = false;
	
	public Node node = new Node(this, null, null, null);
	public enum Op {add, sub, mul, lwrr, swrr, addi, subi, muli, lw, sw, li, j, jr, bge, label, move};
	public LinkedHashSet<Asm> defOut = new LinkedHashSet<Asm>();
	// note label and j only 1 arg.
	public Asm(Op o, Addr d, Addr s1, Addr s2) {
		op = o;
		dst = d;
		src1 = s1;
		src2 = s2;
	}
	public LinkedHashSet<Temp> use() { 
		LinkedHashSet<Temp> set = new LinkedHashSet<Temp>();
		switch(op) {
		case add: case sub: case mul: case lwrr:
			set.add((Temp)src1);
			set.add((Temp)src2);
			break;
		case addi: case subi: case muli: case lw:
			add(set,(Temp)src1);
			break;
		case swrr: 
			add(set, (Temp)src1);
			add(set, (Temp)src2);
			add(set, (Temp)dst);
			break;
		case sw:
			add(set, (Temp)src1);
			add(set, (Temp)dst);
			break;
		case li: case label: case j:
			break;
		case jr:
			add(set, (Temp)dst);
			break;
		case bge:
			add(set, (Temp)dst);
			if (src2 instanceof Temp)
				add(set, (Temp)src1);
			break;
		case move:
			add(set, (Temp)src1);
			break;
		}
		return set;
	}
	
	private void add(LinkedHashSet<Temp> set, Temp t) {
		if (t == Temp.fp || t == Temp.gp || t == Temp.ra || t == Temp.sp) 
			return;
		set.add(t);
	}
	public LinkedHashSet<Temp> def() {
		LinkedHashSet<Temp> set = new LinkedHashSet<Temp>();
		switch (op) {
		case add: case sub: case mul: case addi: case subi: case muli: case lw: case lwrr: case li: case move:
			add(set,(Temp)dst);
			break;
		case label: case j: case sw: case swrr: case jr: case bge:
			break;
		}
		return set;
	}
	
	public static LinkedList<Asm> gen(LinkedList<CompilationUnit> units) throws Error {
		LinkedList<Asm> asms = new LinkedList<Asm>();
		Iterator<CompilationUnit> iter = units.iterator();
		// deal with global and main;
		assert(iter.hasNext());
			CompilationUnit global = iter.next();
		assert(iter.hasNext());
			CompilationUnit main = iter.next();	
			
		Iterator<Quad> quadIter = main.quads.iterator();
		assert(quadIter.hasNext());
		asms.addAll(quadIter.next().gen());
		if (global.level.size()!=0) {
			int size = (global.level.size()+wordSize);				// - wordSize??
			if (size > maxInt) {
				asms.add(new Asm(Op.addi, Temp.gp, Temp.gp, new Const(maxInt)));
				size -=maxInt;
			}
			asms.add(new Asm(Op.addi, Temp.gp, Temp.gp, new Const(size)));
		}
		for (Quad quad:global.quads) 
			asms.addAll(quad.gen());
		while (quadIter.hasNext()) {			
				asms.addAll(quadIter.next().gen());
		}

		
		CompilationUnit unit = null;
		while (iter.hasNext()) {
			unit = iter.next();
			for(Quad quad:unit.quads) {
				asms.addAll(quad.gen());
			}
		}
		
		//System.out.println(asms);
		
		return asms;
	}
	
	public String toString() {
		if (op == Op.label)
			return ((Label)dst).gen()+"\n";
		else {
			String postfix = null;
			switch (op) {
			case add: case sub: case mul: case addi: case subi: case muli: 
			case lw: case lwrr: case sw: case swrr:
			case bge:
				postfix = op.toString()+"\t"+dst.toString()+", "+src1.toString()+", "+src2.toString();
				break;
			case li: case move:
				postfix = op.toString()+"\t"+dst.toString()+", "+ src1.toString();
				break;
			case j: case jr:
				postfix = op.toString()+"\t"+dst.toString();
				break;
			}
			return "\t"+postfix+"\n";
		}
	}
	
	
	public boolean isJump() {
		if (op == Op.j || op == Op.jr || op == op.bge) return true;
		else return false;
	}
	
	public Label jumpLabel() {
		if (!isJump()) return null;
		if (op == op.bge) return (Label)src2;
		return (Label)dst;
	}
	
	public boolean isDef() {
		switch(op) {
		case add: case sub: case mul: case addi: case subi: case muli: 
		case lw: case lwrr: case li: case move:
			return true;
		default: 
			//case j: case jr: case sw: case swrr: case bge:
			return false;
		}
	}
	public String codeGen() {
		// must not be spilled
		if (op == Op.label)
			return ((Label)dst).gen()+":";
		else {
			String postfix = null;
			switch (op) {
			case add: case sub: case mul: case addi: case subi: case muli: 
			case lw: case lwrr: case sw: case swrr:
			case bge:
				postfix = op.toString()+"\t"+dst.gen()+", "+src1.gen()+", "+src2.gen();
				break;
			case li: case move:
				postfix = op.toString()+"\t"+dst.gen()+", "+ src1.gen();
				break;
			case j: case jr:
				postfix = op.toString()+"\t"+dst.gen();
				break;
			}
			return "\t"+postfix;
		}
	}
	
	public static int getStall(Op op) throws Error{
		switch(op) {
		case add: case sub: case addi: case subi: 
			return 0;
		case mul: case muli:
			return 3;
		case lwrr: case lw:
			return 99;
		case swrr: case sw:
			return 0;
		case li: 
			return 0;
		default:
			//case label: case j:	case jr: case bge: case move:
			throw new Error("getStall from "+op);
		}
	}
}
