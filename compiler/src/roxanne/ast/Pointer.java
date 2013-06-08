package roxanne.ast;

public class Pointer extends Type {
	public Type eletype;
	public Pointer(Type t) {
		eletype = t;
	}
}
