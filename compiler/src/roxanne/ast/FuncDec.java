package roxanne.ast;

import roxanne.types.FUNC;;

public class FuncDec extends Dector {
	public FUNC type = null;
	public PlainDector f;
	public ParaList paras;
	public FuncDec(PlainDector pd, ParaList pl) {
		f = pd;
		paras = pl;
	}
}
