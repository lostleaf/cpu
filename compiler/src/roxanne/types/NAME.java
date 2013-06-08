package roxanne.types;
import roxanne.addr.Addr;
import roxanne.symbol.*;
public final class NAME extends TYPE {
	public Symbol name;
	private TYPE binding;
	
	public NAME() { name = null; binding = null; }
	public NAME(String s) { name = Symbol.symbol(s); binding = null; }
	public NAME(String s, TYPE t) { name = Symbol.symbol(s); binding = t; width = binding.width; }
	public void bind(TYPE t) { binding = t; width = binding.width; }
	public TYPE getBind() { return binding;}
	@Override
	public TYPE actual() {
		TYPE a = binding.actual();
		if (a instanceof WRONG) return new WRONG();
		return binding = a;
	}
	public boolean equals(NAME n) {
		if (this == n) return true;
		// if (binding == null) return false;
		return binding.equals(n.binding);
	}
	
	public String toString() {
		return "NAME: "+name.toString() + " "+binding.toString();
	}
	
	public Addr getWidth() {return width = binding.width;}
}
