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

import roxanne.addr.Temp;
import roxanne.quad.Goto;
import roxanne.quad.LABEL;
import roxanne.quad.Leave;
import roxanne.quad.Quad;
import roxanne.quad.Ret;
import roxanne.translate.CompilationUnit;

public class BasicBlockBuilder {
	private static LinkedList<Quad> quads;
	private static ArrayList<BasicBlock> blocks = new ArrayList<BasicBlock>();
	private static Hashtable<Quad, BasicBlock> leaders = new Hashtable<Quad, BasicBlock>();
	
	
	private static void findLeader() {
		boolean isLeader = true;
		for (Quad quad: quads) {
			if (isLeader) {
				quad.isLeader = true;
				isLeader = false;
			}
			if (quad.isJump()) {
				LABEL label = quad.jumpLABEL();
				if (label != null)
					label.isLeader = true;
				isLeader = true;
			}
		}
	}
	
	private static void buildBlocks() {
		BasicBlock exit = new BasicBlock(), bb = null;
		blocks.add(exit);
		
		for (Quad quad: quads) {
			if (quad.isLeader) {
				bb = new BasicBlock();
				blocks.add(bb);
				bb.add(quad);
				leaders.put(quad, bb);
			} else 
				bb.add(quad);
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
			for (Quad quad: blocks.get(i).quads) {
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
	public static ArrayList<BasicBlock> createBBGraph(CompilationUnit unit) {
		blocks.clear();
		leaders.clear();
		
		quads = unit.quads;
		
		findLeader();
		buildBlocks();
		
		BasicBlock exit = blocks.get(0), bb = null;
		
		int len = blocks.size();
		for (int i = 1; i < len; ++i) {
			bb = blocks.get(i);
			Quad last = bb.getLast();
			if (last instanceof Goto) 
				addEdge(bb, leaders.get(last.jumpLABEL()));
			else if (last instanceof Ret || last instanceof Leave)
				addEdge(bb, exit);
			else if (last.isJump()) {
				addEdge(bb, blocks.get(i+1));
				addEdge(bb, leaders.get(last.jumpLABEL()));
			} else {
				// all jump is last, but last may not be a jump
				if (i+1 < len)
					addEdge(bb, blocks.get(i+1));
			}
				
		}
		return blocks;
	}
}