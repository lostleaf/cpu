package roxanne.translate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Stack;

import roxanne.addr.*;
import roxanne.ast.Expr.OpType;
import roxanne.error.Error;
import roxanne.util.Constants;

public class Level implements Constants{
	public Level parent = null;
	public Const offset = new Const(0);	// also the final size
	public Const paraoffset = new Const(0);	// vara para not supported yet
	
	private HashSet<Integer> usedregs = null;
	public boolean savera = false;
	
	private Stack<Const> stack = new Stack<Const> ();
	
	//public Integer offset = 0; //of temp
	// 0($gp), 4($gp), 8($gp) saved for $t0, $t1, $t3
	public Level() { /*offset = new Const(numOfSavedRegisters*wordSize);*/}
	public Level(Level l) {
		parent = l;
		/*if (parent == null)
			offset = new Const(numOfSavedRegisters*wordSize);*/
		/*if (parent != null)
			offset = new Const(saveSize);*/
	}
	
	public Temp newTemp() {
		return new Temp(new Const(wordSize), this);
	}
	
	public Temp newTemp(Addr width) {
		return new Temp(width, this);
	}
	public Const newLocal(Addr width) throws Error {
		Const ans = offset;
		offset = (Const)Addr.biop(offset, OpType.PLUS, width, this);
		return ans;
	}
	
	public Const newPara(Addr width) throws Error {
		paraoffset = (Const)Addr.biop(paraoffset, OpType.MINUS, width, this);
		return paraoffset;
	}
	
	public void beginScope() {
		stack.push(offset);
	}
	
	public void endScope() {
		offset = stack.pop();
	}
	
	public int size() { return offset.value; }
	
	public int paraSize() { return -paraoffset.value; }
	
	// for regAlloc
	public void setUsedRegisters(HashSet<Integer> regs) {
		usedregs = regs;
	}
	
	public HashSet<Integer> getUsedRegisters() {
		assert(usedregs != null);
		return usedregs;
	}
	public void addUsedRegister(Integer regNum) {
		if (usedregs == null)
			usedregs = new HashSet<Integer>();
		usedregs.add(regNum);		
	}
}
