package roxanne.addr;

public class Const extends Addr {
	public Integer value;
	public Integer width = 4;	// 4 or 2 for char
	
	public Const(Integer i) {value = i;}
	
	public Const(Integer i, Integer w) {
		value = i;
		width = w;
	}
	
	public String toString() {
		return value.toString();
	}
	
	public String gen() {
		return value.toString();
	}
}
