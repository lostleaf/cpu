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
import roxanne.quad.*;
import roxanne.translate.CompilationUnit;

public class LivenessAnalysis {
	private LinkedList<CompilationUnit> units;
	
	private LinkedList<Quad> quads;
	private ArrayList<BasicBlock> blocks = new ArrayList<BasicBlock>();
	private Hashtable<Quad, BasicBlock> leaders = new Hashtable<Quad, BasicBlock>();
	private LinkedList<BasicBlock> queue = new LinkedList<BasicBlock>();
	private Set<BasicBlock> inqueue = new HashSet<BasicBlock>();
	
	private HashMap<Temp, LiveInterval> map = new HashMap<Temp, LiveInterval>();
	
	public LivenessAnalysis(LinkedList<CompilationUnit> cu) {
		units = cu;
	}
	
	private void findLeader() {
		boolean isLeader = true;
		for (Quad quad: quads) {
			if (isLeader) {
				quad.isLeader = true;
				isLeader = false;
			}
			if (quad.isJump()) {
				LABEL label = quad.jumpLABEL();
				if (label != null) {
					label.isLeader = true;
				}
				isLeader = true;
			}
		}
	}
	
	private void buildBlocks() {
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
	
	private void addEdge(BasicBlock from, BasicBlock to) {
		from.addNext(to);
		to.addPre(from);
	}
	
	private void printBlocks() {
		int len = blocks.size();
		for (int i = 1 ;i  < len; ++i) {
			System.out.println("bb["+i+"]:");
			System.out.println(blocks.get(i));
		}
	}
	
	// BB for basic block
	private void createBBGraph() {
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
				// if or call
				addEdge(bb, blocks.get(i+1));
				addEdge(bb, leaders.get(last.jumpLABEL()));
			} else {
				// all jump is last, but last may not be a jump
				if (i+1 < len)
					addEdge(bb, blocks.get(i+1));
			}
				
		}
		
	}
	
	private void calcBBInOutEach() {
		int len = blocks.size();
		for (int i = 1; i < len; ++i) 
			blocks.get(i).calcInOutUseDef();
	}
	
	private boolean calcBBInOut() {
		/*queue.add(blocks.get(0));
		inqueue.add(blocks.get(0));
		BasicBlock now = null;
		
		while (!queue.isEmpty()) {
			now = queue.remove(); //System.out.println(now);
			inqueue.remove(now);
			for (BasicBlock next: now.next) {
				now.out.addAll(next.in);
			}
			
			LinkedHashSet<Temp> newin = now.out;
			newin.removeAll(now.def());
			newin.addAll(now.use());
			
			if (!newin.equals(now.in)) {
				now.in = newin;
				for (BasicBlock pre: now.pre)
					if (!inqueue.contains(pre)) {
						queue.add(pre);
						inqueue.add(pre);
					}
			}
			
		}*/
		boolean changed = false;
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
		
		return BBChanged;
	}
	
	// for each compilation unit
	private void findInOut() {
		quads = new LinkedList<Quad>();
		for (CompilationUnit unit: units)
			quads.addAll(unit.quads);
		blocks.clear();
		leaders.clear();
		
		/*System.out.println("=========in find in out=========");
		for (Quad q:quads) 
			System.out.println("\t"+q);
		System.out.println("=========in find in out=========");
		*/
		findLeader();
		createBBGraph();
		boolean changed = false;
		do {
			calcBBInOutEach();
			changed = calcBBInOut();
		} while(changed);
		//printBlocks();
	}
	
	private boolean isCallOrEnter(Quad q) {
		if (q instanceof CallProc || q instanceof CallFunc || q instanceof Enter) return true;
		return false;
	}
	
	public ArrayList<LiveInterval> getLiveIntervals() {
		if (map.isEmpty()) {
			findInOut();

			int qcount = 0;
			Quad q = null;
			for (CompilationUnit unit: units) {
				for (Iterator<Quad> iter = unit.quads.iterator(); iter.hasNext();) {
					q = iter.next();
					Iterator<Temp> defIter = q.def().iterator();
					if (defIter.hasNext()) {
						Temp def = defIter.next();
						if (/*unit.level.parent != null &&*/ !def.mustBeSpilled() && !isCallOrEnter(q)) {
							if (!q.in.contains(def) && !q.out.contains(def)) {
								iter.remove();
								continue;
							}
						}
					}
					
					++qcount;
					for (Temp t:q.in) {
						t.expand(qcount);
						map.put(t, t.getInterval());
					}
					for (Temp t:q.out) {
						t.expand(qcount);
						map.put(t, t.getInterval());
					}
				}
			}
		}
		
		ArrayList<LiveInterval>liveIntervals = new ArrayList<LiveInterval>(map.values());
		Collections.sort(liveIntervals);	// sort by start point
		//System.out.println("haha");
		/*for (LiveInterval i:liveIntervals) {
			System.out.println(i);
		}*/
		/*for (CompilationUnit unit:units) {
			System.out.println(unit);
		}*/
		return liveIntervals;
		
	}
	
	private String print(Set<Temp> set) {
		StringBuffer string = new StringBuffer();
		//System.out.println(set.size());
		for (Temp t: set) {
			string.append(t.toString()+", ");
			//System.out.println(t.toString());
		}
		if (string.length() == 0) string.append("empty");
		return string.toString();
	}
	public void print() {
		for (CompilationUnit unit:units) {
			System.out.println("==============="+unit.label+"==============");
			for (Quad quad: quads) {
				/*if (quad instanceof Branch) {
					print(quad.in);
				}*/
				System.out.println("\tin of "+quad+": "+quad.in);
			}	
			System.out.println("===========end of "+unit.label+"===========");
		}
	}
}