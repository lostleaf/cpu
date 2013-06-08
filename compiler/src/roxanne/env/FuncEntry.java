package roxanne.env;

import roxanne.addr.*;
import roxanne.quad.LABEL;
import roxanne.translate.*;
import roxanne.ast.Block;
import roxanne.types.*;

public class FuncEntry extends Entry {
	public FUNC type;
	public Block block;
	
	public Level level = null;
	public LABEL label = null;
	
	public FuncEntry(FUNC fp) { type = fp; block = null;}
	
	public FuncEntry(FUNC fp, Block b) { type = fp; block = b; }

	public FuncEntry(FUNC f, Level lvl, LABEL lbl) {
		type = f;
		level = lvl;
		label = lbl;
	}
	
	public FuncEntry(FUNC f, Block b, Level lvl, LABEL lbl){
		type = f;
		block = b;
		level = lvl;
		label = lbl;
	}
	
	public String toString() {
		return type.toString();
	}
}
