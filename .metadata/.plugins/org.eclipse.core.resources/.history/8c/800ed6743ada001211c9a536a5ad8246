package roxanne.analysis;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.LinkedHashSet;
import java.util.Set;

import roxanne.addr.Temp;
import roxanne.quad.*;

public class BasicBlock {
	private static int count = 0;
	public int num;
	
	public LinkedList<Quad> quads = new LinkedList<Quad>();
	public LinkedHashSet<BasicBlock> pre = new LinkedHashSet<BasicBlock>();
	public LinkedHashSet<BasicBlock> next = new LinkedHashSet<BasicBlock>();
	public LinkedHashSet<Temp> in = new LinkedHashSet<Temp>();
	public LinkedHashSet<Temp> out = new LinkedHashSet<Temp>();
	public LinkedHashSet<Temp> def = null;
	public LinkedHashSet<Temp> use = null;
	
	// for reachdef
	public LinkedHashSet<Quad> DRGen = new LinkedHashSet<Quad>();
	// kill is just the def;
	public LinkedHashSet<Quad> DRin = new LinkedHashSet<Quad>();
	public LinkedHashSet<Quad> DRout = new LinkedHashSet<Quad>();
	
	public BasicBlock() {
		num = count++;
	}
	
	public void add(Quad q) {
		quads.add(q);
	}
	
	public void addPre(BasicBlock bb) {
		pre.add(bb);
	}
	public void addNext(BasicBlock bb) {
		next.add(bb);
	}
	
	public Quad getLast() {
		return quads.getLast();
	}
	
	private void calcUseDef() {
		if (use != null && def != null)
			return;
		
		use = new LinkedHashSet<Temp>();
		def = new LinkedHashSet<Temp>();
		
		//LinkedHashSet<Temp> quse = new LinkedHashSet<Temp>();
		
		for (Quad q:quads) {
			for (Temp t: q.use()) {
				if (!def.contains(t))
					use.add(t);
			}
			def.addAll(q.def());
		}
	}
	
	// for test
	private String print(Set<Temp> set) {
		StringBuffer string = new StringBuffer();
		for (Temp t:set) {
			string.append(t);
		}
		return string.toString();
	}
	
	public void calcInOutUseDef() {
		calcUseDef();
		quads.getLast().out = out;
		Iterator<Quad> iter = quads.descendingIterator();
		Quad q = null, nextq = null;
		
		nextq = q = iter.next();
		q.in = (LinkedHashSet<Temp>)q.out.clone();
		Killer.kill(q.in, q.def());
		q.in.addAll(q.use());
		while(iter.hasNext()) {
			q = iter.next();
			q.out = (LinkedHashSet<Temp>)nextq.in.clone();	//!! use clone instead of a simple "=" because of the reference mechanism
			nextq = q;
			q.in = (LinkedHashSet<Temp>)q.out.clone();
			Killer.kill(q.in, q.def());
			q.in.addAll(q.use());
		}
	}
	
	public LinkedHashSet<Temp> use() {
		if (use != null) return use;
		calcUseDef();
		return use;
	}
	
	public LinkedHashSet<Temp> def() {
		if (def != null) return def;
		calcUseDef();
		return def;
	}
	
	public String toString() {
		StringBuffer str = new StringBuffer();
		str.append("===============================\n");
		for (Quad q:quads) 
			str.append("\t"+q+"\n");
		//str.append("\tpre: "+ pre+"\n");
		//str.append("\tnext: "+ next+"\n");
		str.append("\tin: "+in+"\n");
		str.append("\tout: "+out+"\n");
		str.append("\tDRin: "+DRin+"\n");
		str.append("\tDRout: "+DRout+"\n");
		str.append("===============================\n");
		return str.toString();
	}
}
