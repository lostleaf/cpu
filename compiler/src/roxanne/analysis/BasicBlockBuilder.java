package roxanne.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

import roxanne.addr.Label;
import roxanne.addr.Temp;
import roxanne.asm.Asm;
import roxanne.asm.Asm.Op;
import roxanne.quad.Goto;
import roxanne.quad.LABEL;
import roxanne.quad.Leave;
import roxanne.quad.Quad;
import roxanne.quad.Ret;
import roxanne.translate.CompilationUnit;

public class BasicBlockBuilder {
	private static LinkedList<Asm> asms;
	private static ArrayList<BasicBlock> blocks = new ArrayList<BasicBlock>();
	private static Hashtable<Asm, BasicBlock> leaders = new Hashtable<Asm, BasicBlock>();
	
	
	private static void findLeader() {
		boolean isLeader = true;
		for (Asm asm: asms) {
			if (isLeader) {
				asm.isLeader = true;
				isLeader = false;
			}
			if (asm.isJump()) {
				Label label = asm.jumpLabel();
				if (label != null) {
					Asm labelAsm = find(label);
					labelAsm.isLeader = true;
				}
				isLeader = true;
			}
		}
	}
	
	private static void buildBlocks() {
		BasicBlock exit = new BasicBlock(), bb = null;
		blocks.add(exit);
		
		for (Asm asm: asms) {
			if (asm.isLeader) {
				bb = new BasicBlock();
				blocks.add(bb);
				bb.add(asm);
				leaders.put(asm, bb);
			} else 
				bb.add(asm);
		}
	}
	
	private static void addEdge(BasicBlock from, BasicBlock to) {
		from.addNext(to);
		to.addPre(from);
	}
	
	private void printBlocks() {
		int len = blocks.size();
		for (int i = 0 ;i  < len; ++i) {
			System.out.println("bb["+i+"]:"+blocks.get(i));
			for (Asm quad: blocks.get(i).asms) {
				System.out.println("\t" + quad);
			}
			System.out.println("pre: "+blocks.get(i).pre);
			System.out.println("next: "+blocks.get(i).next);
			System.out.println("in: "+blocks.get(i).in);
			System.out.println("out: "+blocks.get(i).out);
			System.out.println(" ");
		}
	}
	
	// BB for basic block
	public static ArrayList<BasicBlock> createBBGraph(LinkedList<Asm> asmList) {
		blocks.clear();
		leaders.clear();
		
		asms = asmList;
		
		findLeader();
		buildBlocks();
		
		BasicBlock exit = blocks.get(0), bb = null;
		
		int len = blocks.size();
		for (int i = 1; i < len; ++i) {
			bb = blocks.get(i);
			Asm last = bb.getLast();
			if (i == len-1 || last.op == Op.jr)
				addEdge(bb, exit);
			else if (last.op == Op.j) 
				addEdge(bb, leaders.get(find(last.jumpLabel())));
			else if (last.isJump()) {
				addEdge(bb, blocks.get(i+1));
				addEdge(bb, leaders.get(find(last.jumpLabel())));
			} else {
				// all jump is last, but last may not be a jump
				if (i+1 < len)
					addEdge(bb, blocks.get(i+1));
			}
				
		}
		
		/*boolean changed = false;
		boolean BBChanged = false;
		int len = blocks.size();
		BasicBlock now = null;
		do {
			changed = false;
			for (int i = 1; i < len; ++i) {
				now = blocks.get(i);
				for (BasicBlock next: now.next) {
					now.out.addAll(next.in);
				}
				LinkedHashSet<Temp> newin = (LinkedHashSet<Temp>) now.out.clone();
				Killer.kill(newin, now.def());
				newin.addAll(now.use());
				
				if (!newin.equals(now.in)) {
					now.in = newin;
					changed = true;
					BBChanged = true;
				}
			}
		} while(changed);
		
		return BBChanged;*/
		return blocks;
	}
	
	private static Asm find(Label label) throws Error{
		for (Asm asm: asms) {
			if (asm.op == Op.label && asm.dst == label) 
				return asm;
		}
		throw new Error("label not find");
	}
}