package roxanne.ast;

import roxanne.env.Pack;;

public class InitDector {
	public Dector name;
	public Expr values;
	public Pack pack = null;
	public InitDector(Dector d, Expr e) {
		name = d;
		values = e;
	}
}
