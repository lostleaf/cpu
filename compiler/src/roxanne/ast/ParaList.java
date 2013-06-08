package roxanne.ast;

public class ParaList {
	public Argument head;
	public ParaList tail;
	public ParaList(Argument a, ParaList pl) {
		head = a;
		tail = pl;
	}
}
