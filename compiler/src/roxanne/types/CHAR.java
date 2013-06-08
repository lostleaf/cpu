package roxanne.types;

import roxanne.addr.*;

public final class CHAR extends TYPE{
	private static CHAR instance = new CHAR();
	private CHAR() { width = new Const(4);}
	public static CHAR getInstance() {return instance;}
	public CHAR actual() {return instance;}
	public Addr getWidth() { return width;}
	public String toString() {
		return "char";
	}
}
