package roxanne.ast;

public class DecList {
	public Dec head;
	public DecList tail;
	public DecList(Dec d, DecList dl) { head = d; tail = dl; }
	public DecList(Dec d) { head = d; tail = null; }
	public DecList() {head = null; tail = null; }
}
