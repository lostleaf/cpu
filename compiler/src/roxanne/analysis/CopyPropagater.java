package roxanne.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.ListIterator;

import roxanne.addr.Addr;
import roxanne.addr.Const;
import roxanne.addr.Temp;
import roxanne.asm.Asm;
import roxanne.quad.*;
import roxanne.translate.CompilationUnit;
import roxanne.translate.SizeHandler;

public class CopyPropagater {
	private ArrayList<BasicBlock> blocks;
	private CompilationUnit unit;
	
	public CopyPropagater(ArrayList<BasicBlock> b, CompilationUnit u){
		blocks = b;
		unit = u;
	}
	
	public boolean propagate() throws Error {
		DefReach defreach = new DefReach(blocks);
		boolean killed = false;
		boolean changed = false;
		
		do {
			defreach.calcDefReach();
			changed  = killExpr();			
		} while(changed);
		
		return killed;
	}
	
	private Quad replace(Quad quad, HashMap<Temp, Addr> map) {
		Addr value = null, dst = null, r = null, l = null;
		Quad newQ = null;
		if (quad instanceof Biop || quad instanceof Uop || quad instanceof Move) {
			if (quad instanceof Uop) {
				value = map.get(((Uop)quad).src);
				if (value == null || value instanceof Temp) return quad.replace(map);
				newQ = new MoveI(((Uop)quad).dst, SizeHandler.calcU(((Uop)quad).op, (Const)value));
				return newQ.replace(map);
			} else if (quad instanceof Biop) {
				l = map.get(((Biop)quad).left);
				r = map.get(((Biop)quad).right);
				if ((l == null ||  l instanceof Temp) && (r == null || r instanceof Temp)) return quad.replace(map);
				
				if (l instanceof Const && r instanceof Const)
					newQ = new MoveI(((Biop)quad).dst, SizeHandler.calcB((Const)l, ((Biop)quad).op, (Const)r));
				else {
					if (l instanceof Const) {
						Addr t = l; l = r; r = t;
					}
					newQ = new BioprI(((Biop)quad).dst, l, r);
				}
				return newQ.replace(map);
					
			} else if (quad instanceof Move) {
				value = map.get(((Move)quad).src);
				if (value == null || value instanceof Temp) return quad.replace(map);
				newQ = new MoveI(((Move)quad).dst, (Const)value);
				return newQ.replace(map);
			}
		} else return quad.replace(map);
	}
	
	private Quad replace(Quad quad) {
		Quad def = null, ans = null;
		HashMap<Temp, Addr> map = new HashMap<Temp, Addr>();
		for (Temp t:quad.use()) {
			def = null;
			for (Quad move: quad.DRin) {
				if (move instanceof Move || move instanceof MoveI) {
					if (move.def().contains(t)) {
						if (def != null) {
							def = null;
							break;
						}
						def = move;
					}
				}
			}
			if (def == null) continue;
			Addr right = (def instanceof Move)? ((Move)def).src: ((MoveI)def).src;
			map.put(t, right);
			--def.DRcount;
		}
		return replace(quad, map);
	}
	
	private boolean killExpr() {
		boolean ans = false;
		ListIterator<Quad> riter = (ListIterator<Quad>) unit.quads.iterator();
		Quad r = null, q = null;
		while (riter.hasNext()) {
			q = riter.next();
			r = replace(q);
			if (q != r) {
				riter.set(r);
				ans = true;
			}
		}
		
		Iterator<Quad> iter = unit.quads.iterator();
		Quad quad = null;
		while (iter.hasNext()) {
			quad = iter.next();
			if (quad.DRcount == 0) {
				iter.remove();
				ans = true;
			}
		}
		return ans;
	}
}

class DefReach{
	ArrayList<BasicBlock> blocks;
	DefReach(ArrayList<BasicBlock> list) {
		blocks = list;
	}
	void calcDefReach() throws Error {
		boolean changed;
		do {
			calcBBKillGenEach();
			changed = calcBBInOut();
		} while(changed);
		calcBBInOutEach();
	}
	
	boolean kill(LinkedHashSet<Temp> def, Asm q) {
		if (q.def().containsAll(def)) return true;
		return false;
	}
	
	boolean calcBBInOut() {
		for (int i = 0; i < blocks.size(); ++i)
			blocks.get(i).DRout.clear();
		boolean ans = false;
		boolean changed = false;
		BasicBlock b = null;
		do {
			changed = false;
			for (int i = 1; i < blocks.size(); ++i) {
				b = blocks.get(i);
				for (BasicBlock pre: b.pre) {
					b.DRin.addAll(pre.DRout);
				}
				
				LinkedHashSet<Quad> newout = (LinkedHashSet<Quad>) b.DRin.clone();
				Iterator<Quad> iter = newout.iterator();
				Quad q = null;
				while (iter.hasNext()) {
					q = iter.next();
					if (kill(b.def, q))
						iter.remove();
				}
				
				newout.addAll(b.DRGen);
				
				if (!b.DRout.containsAll(newout)) {
					ans = changed = true;
					b.DRout = newout;
				}
			}
		} while(changed);
		return ans;
	}
	void calcBBKillGenEach() {
		BasicBlock block = null;
		for (int i = 0; i < blocks.size(); ++i)  {
			blocks.get(i).DRGen = new LinkedHashSet<Asm>();
			blocks.get(i).def = new LinkedHashSet<Temp>();
		}
		
		Asm a = null;
		for (int i = 1; i < blocks.size(); ++i) {
			block = blocks.get(i);
			Iterator<Asm> iter = block.asms.descendingIterator();
			while (iter.hasNext()) {
				a = iter.next();
				if (a.isDef() && !kill(block.def, a)) {
					block.DRGen.add(a);
					block.def.addAll(a.def());
				}
			}
		}
	}
	
	void addCount(LinkedHashSet<Quad> set, Quad q) {
		for (Quad quad:set) {
			if (q.use().containsAll(quad.def())) {
				quad.DRcount++;
				return;
			}
			
		}
	}
	
	private void kill(LinkedHashSet<Quad> set, LinkedHashSet<Temp> defTemps) throws Error {
		if (defTemps.size() > 1) throw new Error("def size > 1 in kill");
		if (defTemps.size() == 0) return;
		
		Iterator<Temp> titer = defTemps.iterator();
		Temp t = titer.next();
		
		Iterator<Quad> iter = set.iterator();
		Quad q = null;
		while (iter.hasNext()) {
			q = iter.next();
			if (q.def().contains(t))
				iter.remove();
		}
	}
	
	void calcBBInOutEach() throws Error {
		BasicBlock block = null;
		
		for (int i = 1; i < blocks.size(); ++i) {
			block = blocks.get(i);
			Iterator<Quad> iter = block.quads.iterator();
			Quad q = iter.next(), pre = q;
			q.DRin = block.DRin;
			q.DRout = (LinkedHashSet<Quad>) q.DRin.clone();
			kill(q.DRout, q.def());
			if (q.isDef())	q.DRout.add(q);
			addCount(q.DRin, q);
			while (iter.hasNext()) {
				q = iter.next();
				q.DRin = (LinkedHashSet<Quad>) pre.DRout.clone();
				pre = q;
				
				kill(q.DRin, q.def());
				if (q.isDef()) q.DRout.add(q);
				addCount(q.DRin, q);
			}
		}
	}
}