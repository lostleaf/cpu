package roxanne.ast;

import roxanne.env.Pack;

//import roxanne.symbol.Symbol;
public class FuncDef extends Def {
	public Pack pack = null;
	public Type returntype;
	public ParaList paras;
	public PlainDector f;
	public Block block;
	/*public FuncDef(String id, Type t, ParaList pl, Block b) {
		f = Symbol.symbol(id);
		returntype = t;
		paras = pl;
		block = b;
	}*/
	public FuncDef(PlainDector id, Type t, ParaList pl, Block b) {
		f = id;
		returntype = t;
		paras = pl;
		block = b;
	}
}