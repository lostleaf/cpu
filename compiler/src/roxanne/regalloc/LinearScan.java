package roxanne.regalloc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;

import roxanne.addr.Temp;
import roxanne.analysis.LiveInterval;
import roxanne.analysis.LivenessAnalysis;
import roxanne.asm.Asm;
import roxanne.error.Error;
import roxanne.translate.CompilationUnit;
import roxanne.translate.Level;

public class LinearScan implements RegAlloc, Comparator<LiveInterval>{
	private static final int R = numOfSavedRegisters;
	private TreeSet<Integer> freeregs = new TreeSet<Integer>();
	private ArrayList<LiveInterval> active = new ArrayList<LiveInterval>();
	private HashSet<Integer> usedRegs = new HashSet<Integer>();

	public LinearScan() {
	}
		
	private Integer getFreeRegister() {
		Integer reg = freeregs.pollFirst();
		usedRegs.add(reg);
		return reg;
	}
	private void expireOldInterval(LiveInterval i) {
		Collections.sort(active, this);
		LiveInterval j = null;
		for (Iterator<LiveInterval> iter = active.iterator(); iter.hasNext();) {
			j = iter.next();
			if (j.getEndPoint() > i.getStartPoint())	// my change??
				return;
			iter.remove();
			freeregs.add(j.getRegister());
		}
	}
	
	private void spillAtInterval(LiveInterval i, Level lvl) throws Error {
		LiveInterval spill = active.get(active.size()-1);
		
		if (spill.getEndPoint() > i.getEndPoint()) {
			i.bind(spill.getRegister());
			spill.spill(lvl);
			active.remove(spill);
			active.add(i);
			
			//System.out.println(i);
		} else 
			i.spill(lvl);
	}

	@Override
	public int compare(LiveInterval i0, LiveInterval i1) {
		return i0.getEndPoint() - i1.getEndPoint();
	}

	@Override
	public void bindRegister(LinkedList<Asm> asms) throws Error {
		LivenessAnalysis analyzer = null;
		freeregs.clear();
		for (int i = 0; i < R; ++i)
			freeregs.add(i+baseOfSavedRegisters);

		analyzer = new LivenessAnalysis(asms);
		active.clear();
		usedRegs.clear();
		
		for (LiveInterval i: analyzer.getLiveIntervals()) {
			expireOldInterval(i);
			if (active.size() == R || i.getTemp().mustBeSpilled())
				spillAtInterval(i, i.getTemp().level);
			else {
				i.bind(getFreeRegister());
				active.add(i);
			}
			//System.out.println(i);
		}
	}

}
