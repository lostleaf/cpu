package roxanne.util;

public interface Constants {

	static final String[] opStr = {
		"+", "-", "*", "/", "==", "!=", "<", "<=", ">", ">="
	};
	
	static final boolean[] isInvertable = {
		true, false, true, false, true, true, true, true, true, true
	};
	
	/*static final int[] opInverted = {
		OpExp.PLUS, -1, OpExp.TIMES, -1, OpExp.EQ, OpExp.NEQ, OpExp.GT, OpExp.GE, OpExp.LT, OpExp.LE
	};*/
	
	static final String[] ifFalseCmp = {
		"", "", "", "", "ne", "eq", "ge", "gt", "le", "lt"
	};
	
	static final String[] assemStr = {
		"addu", "subu", "mul", "divu", "seq", "sne", "slt", "sle", "sgt", "sge"
	};
	
	static final String[] assemStrI = {
		"addiu", "subu", "mul", "divu", "seq", "sne", "slt", "sle", "sgt", "sge"
	};
	
	static final int wordSize = 1;

	static int baseOfSavedRegisters = 4;	// start from $a0
	static int numOfSavedRegisters = 22;	// number of saved registers, including $a0, $a1, $a2, $a3
	static int ra = 31;
	static int fp = 30;
	static int sp = 29;
	static int gp = 28;
	static int k0 = 26;						// index of $k0, for spilled temp
	static int k1 = 27;						// index of $k1, for spilled temp
	static int v0 = 2;						// index of $v0, for return value
	static int a0 = 4;
	static int a1 = 5;
	static int saveSize = (numOfSavedRegisters+2)*wordSize;
	
	static final int paramRegBase = 4;	// start from $a0
	static final int paramRegNum  = 4;	// $a0-$a3
	
	static final String[] regNames = {
		"$zero", "$at",
		"$v0", "$v1",
		"$a0", "$a1", "$a2", "$a3",
		"$t0", "$t1", "$t2", "$t3", "$t4", "$t5", "$t6", "$t7",
		"$s0", "$s1", "$s2", "$s3", "$s4", "$s5", "$s6", "$s7",
		"$t8", "$t9",
		"$k0", "$k1",
		"$gp", "$sp", "$fp", "$ra",
		"spill"	// special register
	};
	
	static final int spillReg = regNames.length - 1;
	
	static final int rewriteLimit = 100;

	static final String[] libFunctions = {
		"print", "printi", "flush", "getchar", "ord", "chr",
		"size", "substring", "concat", "not", "exit"
	};
	
	static final int minInt = -262143;	// 18b'
	static final int maxInt = 262144;
	static final int minBrInt = -8192, maxBrInt = 8191;			// 13b'
	static final int minLiInt = -8388608, maxLiInt = 8388607;	//23b'
	
	static final String[] peepholeRegNames = {
		"t0", "t1", "t2", "t3", "t4", "t5", "t6", "t7",
		"s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7",
		"t8", "t9",
	};
	
	static final int inf = Integer.MAX_VALUE;
}
