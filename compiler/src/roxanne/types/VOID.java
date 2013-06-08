package roxanne.types;

import roxanne.addr.*;

public final class VOID extends TYPE{
	private static VOID instance = new VOID();
	private VOID() {}
	public static VOID getInstance() {return instance;}
	public VOID actual() { return instance; }
	public Addr getWidth() { return null; }
}
