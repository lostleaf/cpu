package roxanne.ast;

import roxanne.types.STRUCTUNION;

public class Dec extends Def {
	public Type type;
	public InitDecList list;
	public Dec (Type t, InitDecList l){
		type = t;
		list = l;
	}
}
