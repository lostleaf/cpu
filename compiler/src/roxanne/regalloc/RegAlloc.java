package roxanne.regalloc;

import java.util.LinkedList;

import roxanne.translate.CompilationUnit;
import roxanne.util.*;

import roxanne.addr.Temp;
import roxanne.asm.Asm;

public interface RegAlloc extends Constants{
	void bindRegister(LinkedList<Asm> asms) throws roxanne.error.Error;
}
