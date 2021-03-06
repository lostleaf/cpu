package roxanne.analysis;

import java.util.*;


import roxanne.addr.Temp;
import roxanne.asm.*;
import roxanne.asm.Asm.Op;
import roxanne.error.Error;

public class Scheduler {
	private ArrayList<Asm> asms;
	private Node root;
	private int front, back;
	
	public Scheduler(LinkedList<Asm> asmList) {
		asms = new ArrayList(asmList);
		//System.out.println(asms);
	}
	
	/*public ArrayList<Asm> unroll(int times) throws roxanne.error.Error {
		findStartEnd();
		Asm add = asms.get(back+2);
		ArrayList<Asm> asmtail = (ArrayList<Asm>) asms.subList(back+1, asms.size());
		while(asms.size()>back+1) asms.remove(asms.size()-1);
		
		for (int i = 0; i < times; ++i) {
			
		}
	}*/
	
	public ArrayList<Asm> reSchedule() throws roxanne.error.Error {
		buildGraph();
		ArrayList<Node> nodes = reSchedule(root);
		//System.out.println(nodes);
		int p = 0;
		for(int i = front; i <= back; ++i) {
			for(; p < nodes.size(); ++p) {
				if (!isStall(nodes.get(p))) break;
			}
			asms.set(i, nodes.get(p).asm);
			++p;
		}
		//System.out.println(asms);
		return asms;
	}
	
	private void append(ArrayList<Node> list, Node n) throws roxanne.error.Error {
		if (list.isEmpty()) {
			list.add(n);
			return;
		}
		Node last = list.get(list.size()-1);
		int stall = Asm.getStall(last.asm.op);
		for (int i = 0; i < stall; ++i) 
			list.add(new Node(null, null, null, null));
		list.add(n);		
	}
	
	private boolean isStall(Node n) {
		return n.asm == null;
	}
	
	private void merge(ArrayList<Node> to, int toEnd, ArrayList<Node> from, int fromEnd) throws roxanne.error.Error {
		if (from.isEmpty() || toEnd < 0 || fromEnd < 0) return;

		
		int stallCnt = 0;
		for (int i = fromEnd; i >=0; --i){
			if (!isStall(from.get(i)))
				break;
			else ++stallCnt;
		}

		fromEnd -= stallCnt+1;
		ArrayList<Node> toCopy = (ArrayList<Node>) to.clone();
		int insertPos = toEnd-Asm.getStall(from.get(fromEnd+1).asm.op)-1;
		if (insertPos < 0) {
			for (; insertPos < 0; ++insertPos) {
				to.add(0, new Node(null, null, null, null));
			}
		}
		if (isStall(to.get(insertPos))) {
			to.set(insertPos, from.get(fromEnd+1));
			merge(to, insertPos, from, fromEnd);
		} else {
			toCopy.add(insertPos+1, from.get(fromEnd+1));
			merge(toCopy, insertPos+1, from, fromEnd);
			
			// find the firstStall
			for (int i = insertPos; i >= 0; --i) {
				if (isStall(to.get(i))) {
					insertPos = i;
					break;
				}
			}
			if (insertPos >= 0) {
				to.set(insertPos, from.get(fromEnd+1));
				merge(to, insertPos, from, fromEnd);
			} else {
				to.add(0, from.get(fromEnd+1));
				merge(to, 0, from, fromEnd);
			}
			
			if (toCopy.size() < to.size())
				to = toCopy;
		}
	}
	private ArrayList<Node> reSchedule(Node n) throws roxanne.error.Error {
		if (n == null || n.calced) return new ArrayList<Node>();
		n.calced = true;
		
		ArrayList<Node> l = reSchedule(n.l), m = null, r = null;
		append(l, n);
		if (n.m != null) {
			m = reSchedule(n.m);
			if (l.size() >= m.size())
				merge(l,l.size()-1, m, m.size()-1);
			else {
				l.remove(l.size()-1);
				append(m, n);
				merge(m, m.size()-1, l, l.size()-1);
				l = m;
			}
		}
		
		if (n.r != null) {
			r = reSchedule(n.r);
			if (l.size() >= r.size())
				merge(l,l.size()-1, r, r.size()-1);
			else {
				l.remove(l.size()-1);
				append(r, n);
				merge(r, r.size()-1, l, l.size()-1);
				l = r;
			}
		}
		
		//System.out.println(n);
		//System.out.println(l);
		
		return l;
	}
	
	private void buildGraph() throws Error {
		
		findStartEnd();
		//System.out.println("front:"+asms.get(front)+";back:"+ asms.get(back));
		Asm a = null;
		for (int i = front; i <= back; ++i) {
			a = asms.get(i);
			a.defOut = (LinkedHashSet<Asm>) asms.get(i-1).defOut.clone();
			switch(a.op) {
			case add: case sub: case mul: case lwrr:
				a.node.l = find((Temp)a.src1, a.defOut);
				if (a.node.l == null)
					a.node.l = find((Temp)a.src2, a.defOut);
				else a.node.r = find((Temp)a.src2, a.defOut);
				break;
			case addi: case subi: case muli: case lw:
				a.node.l = find((Temp)a.src1, a.defOut);
				break;
			case swrr: 
				// ignore the addr
				a.node.l = find((Temp)a.dst, a.defOut);
				a.node.m = find((Temp)a.src1, a.defOut);
				a.node.r = find((Temp)a.src2, a.defOut);
				if (a.node.l == null && a.node.m != null) {
					a.node.l = a.node.m;
					a.node.m = null;
				}
				if (a.node.l == null && a.node.r != null) {
					a.node.l = a.node.r;
					a.node.r = null;
				}
				break;
			case sw:
				a.node.l = find((Temp)a.dst, a.defOut);
				break;
			case li: case label: case j:
				break;
			case jr:
				a.node.l = find((Temp)a.dst, a.defOut);
				break;
			case bge:
				a.node.l = find((Temp)a.dst, a.defOut);
				break;
			case move:
				a.node.l = find((Temp)a.src1, a.defOut);
				break;
			}
			//System.out.println(a+"\tdef:"+a.def());
			if (!a.def().isEmpty()) {
				kill(a.def(), a.defOut);
				a.defOut.add(a);
			}
			
			root = asms.get(back).node;
		}
		//print(root);
	}
	
	private void print(Node n) {
		if (n == null) {
			System.out.print("null");
			return;
		}
		System.out.print("{"+n.asm+", l:");
		print(n.l);
		System.out.print(", m:");
		print(n.m);
		System.out.print(", r:");
		print(n.r);
		System.out.print("}");
	}
	
	private void kill(LinkedHashSet<Temp> def, LinkedHashSet<Asm> defOut) throws Error{
		if (def.size() != 1) throw new Error("def size not 1");
		Iterator<Asm> iter = defOut.iterator();
		while(iter.hasNext()) {
			if (iter.next().def().containsAll(def))
				iter.remove();
		}
	}
	private Node find(Temp d, LinkedHashSet<Asm> defs) {
		for (Asm a: defs) {
			if (a.def().contains(d)) return a.node;
		}
		return null;
	}
	private void findStartEnd() {
		int cnt = 0, len = asms.size();
		for (int i = 0; i < len; ++i) {
			if (asms.get(i).op == Op.label) {
				++cnt;
				if (cnt == 4) front = i+2;
				if (cnt == 5) {
					back = i-1;
					return;
				}
			}
		}
	}
}
