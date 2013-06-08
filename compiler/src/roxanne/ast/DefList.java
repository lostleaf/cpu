package roxanne.ast;

public class DefList {
	public Def head;
	public DefList tail;
	public DefList(Def d, DefList dl) {
		head = d;
		tail = dl;
	}
}
