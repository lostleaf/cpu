package roxanne.env;

import roxanne.types.*;

public class TypeDefEntry extends Entry {
	public NAME type;
	public TypeDefEntry(NAME t) {  type = t;}
	public String toString() {
		return type.toString();
	}
}
