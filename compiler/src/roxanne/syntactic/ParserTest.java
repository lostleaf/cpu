package roxanne.syntactic;

import java.io.*;

import roxanne.main.Main;
import roxanne.ast.*;
import com.google.gson.Gson;

final class ParserTest {
	private static void parse(String filename) throws IOException {
		InputStream inp = new FileInputStream(filename);
		PrintWriter out = new PrintWriter(new FileOutputStream(
				filename.replace(".c", ".s")));
		Parser parser = new Parser(inp);
		java_cup.runtime.Symbol parseTree = null;
		try {
			parseTree = parser.parse();
		} catch (Throwable e) {
			e.printStackTrace();
			throw new Error(e.toString());
		} finally {
			inp.close();
		}
		Gson gson = new Gson();
		System.out.println(gson.toJson(parseTree.value));
		//Find f = new Find(parseTree);
		//f.find();
	}

	public static void main(String argv[]) throws IOException {
		/*parse(Main.pathOf("example1.c"));
		parse(Main.pathOf("example2.c"));
		parse(Main.pathOf("example3.c"));
		parse(Main.pathOf("expr.c"));*/
		parse(Main.pathOf("newtest.c"));
	}
}

class Find{
	Expr root;
	public Find(java_cup.runtime.Symbol e){
		//System.out.println("haha");
		root =(Expr) e.value;
		//System.out.println("heihei");
	}
	public void find() {
		find(root);
	}
	public void find(Expr e){
		if (e == null) {
			System.out.println("Null");
			return;
		}
		System.out.println("heihei");
		if (e instanceof Num) {
			System.out.println(((Num)e).value);
			System.out.println("Num");
			return;
		}
		if (e instanceof Var) {
			System.out.println(((Var) e).name.toString());
			System.out.println("Var");
			return;
		}
		find(((BExpr)e).left);
		System.out.println(((BExpr) e).op);
		find(((BExpr)e).right);
	}
}