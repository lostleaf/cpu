package roxanne.types;

import roxanne.addr.*;

public final class INT extends TYPE{
	private static INT instance = new INT();
	private INT() { width = new Const(wordSize);}
	public static INT getInstance() {return instance;}
	public INT actual() { return instance; }
	public Const getWidth() { return (Const) width; }
	public String toString() {
		return "int";
	}
}
