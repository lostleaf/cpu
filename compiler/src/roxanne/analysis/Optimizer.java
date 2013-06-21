package roxanne.analysis;

import java.util.ArrayList;
import java.util.LinkedList;

import roxanne.asm.Asm;
import roxanne.quad.Quad;
import roxanne.translate.CompilationUnit;

public class Optimizer {
	private static CompilationUnit unit;
	private static ArrayList<BasicBlock> blocks;
	
	public static void optimize(LinkedList<Asm> asmList) {
		blocks = BasicBlockBuilder.createBBGraph(asmList);
		boolean changed = false;
		do {
			changed = copy.propagate();
		} while (changed);
		print();
	}
	
	public static void print() {
		System.out.println("==============="+unit.label+"==============");
		for (Quad quad: unit.quads) {
			/*if (quad instanceof Branch) {
					print(quad.in);
				}*/
			System.out.println("\tin of "+quad+": "+quad.in);
		}	
		System.out.println("===========end of "+unit.label+"===========");
	}
}
