package roxanne.ast;

public class Num extends Expr{
	public Integer value;
	public Num(Integer n) {
		value = new Integer(n);
	}
	public int intValue()  {
		return value.intValue();
	}
	
	public String toString() {
		return value.toString();
	}
}
