package roxanne.asm;

public class Node {
	public Asm asm;
	public Node l = null;
	public Node r = null;
	public Node m = null;
	
	public boolean calced = false;
	
	public Node(Asm a, Node ll, Node mm, Node rr) {
		asm = a;
		l = ll;
		r = rr;
		m = mm;
	}
	
	public String toString() {
		if (asm!=null) return asm.toString();
		else return "";
	}
}
