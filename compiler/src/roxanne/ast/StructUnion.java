package roxanne.ast;

public class StructUnion extends Type {
	public String name;
	public DecList record;
	public StructUnion() {name=null; record=null;}
	public StructUnion(String n) {name=n; record = null;}
	public StructUnion(DecList r) { name=null; record=r; }
	public StructUnion(String n, DecList r) { name = n; record = r; }
}
