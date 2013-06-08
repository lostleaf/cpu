package roxanne.addr;

import roxanne.symbol.Symbol;

public class Label extends Addr {
	private static Integer count = 0;
	public Integer num;
	public Symbol name = null;
	
	public Label() {
		num = count++;
	}
	public Label(Symbol s) {
		num = null;
		name = s;
	}
	
	public Label(String str) {
		num = null;
		name = Symbol.symbol(str);
	}
	
	public String toString() {
		if (name == null)
			return "L"+ num.toString();
		else return name.toString();
	}
	
	public String gen() {
		if (name == null)
			return "L"+ num.toString();
		else return name.toString();
	}
}
