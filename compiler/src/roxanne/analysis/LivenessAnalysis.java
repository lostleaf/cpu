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
import roxanne.asm.Asm;
import roxanne.quad.*;
import roxanne.translate.CompilationUnit;

public class LivenessAnalysis {
	private LinkedList<Asm> asms;
	private ArrayList<BasicBlock> blocks = new ArrayList<BasicBlock>();
	
	private HashMap<Temp, LiveInterval> map = new HashMap<Temp, LiveInterval>();
	
	public LivenessAnalysis(LinkedList<Asm> asmList) {
		asms = asmList;
	}
	
	private void printBlocks() {
		int len = blocks.size();
		for (int i = 1 ;i  < len; ++i) {
			System.out.println("bb["+i+"]:");
			System.out.println(blocks.get(i));
		}
	}
	

	
	private void calcBBInOutEach() {
		int len = blocks.size();
		for (int i = 1; i < len; ++i) 
			blocks.get(i).calcInOutUseDef();
	}
	
	private boolean calcBBInOut() {
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
		blocks = BasicBlockBuilder.createBBGraph(asms);
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

			int acount = 0;
			Asm a = null;
			for (Iterator<Asm> iter = asms.iterator(); iter.hasNext();) {
				a= iter.next();
				Iterator<Temp> defIter = a.def().iterator();
				if (defIter.hasNext()) {
					Temp def = defIter.next();
					if (!def.mustBeSpilled() /*&& !isCallOrEnter(a)*/) {
						if (!a.in.contains(def) && !a.out.contains(def)) {
							iter.remove();
							continue;
						}
					}
				}

				++acount;
				for (Temp t:a.in) {
					t.expand(acount);
					map.put(t, t.getInterval());
				}
				for (Temp t:a.out) {
					t.expand(acount);
					map.put(t, t.getInterval());
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
		for (Asm quad: asms) {
			System.out.println("\tin of "+quad+": "+quad.in);
			}	
	}
}
