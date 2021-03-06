package roxanne.main;

import java.io.*;
import java.util.LinkedList;

import roxanne.regalloc.LinearScan;
import roxanne.regalloc.RegAlloc;
import roxanne.semantic.Semantic;
import roxanne.syntactic.*;
import roxanne.translate.CompilationUnit;
import roxanne.translate.DataFrag;
import roxanne.translate.Level;
import roxanne.translate.Translator;
import roxanne.analysis.LivenessAnalysis;
import roxanne.analysis.Optimizer;
import roxanne.analysis.Scheduler;
import roxanne.asm.Asm;
import roxanne.ast.*;
import roxanne.codegen.*;
import roxanne.error.Error;

public class Main {

	public static String pathOf(String filename) {
		return Main.class.getResource(filename).getPath();		//??
	}

	private static void compile(String filename) throws IOException {
		InputStream inp = new FileInputStream(filename);
		Parser parser = new Parser(inp);	//?
		java_cup.runtime.Symbol parseTree = null;		//??
		try {
			parseTree = parser.parse();
		} catch (Throwable e) {
			e.printStackTrace();
			//throw new Error(e.toString());
			System.exit(1);
			return;
		} finally {
			inp.close();
		}
		DefList program = (DefList) parseTree.value;
		
		try{
		//======================================Semantic=============================================
			Semantic semantic = new Semantic();	
			semantic.trans(program);
		//======================================Semantic=============================================
		
			
		//======================================MidCode==============================================
			Translator translator = new Translator(new Level());
			translator.translate(program);
		//======================================MidCode==============================================
		//====================================CodeSchedule============================================
			LinkedList<CompilationUnit> units = translator.getUnits();
			LinkedList<Asm> asms = Asm.gen(units);
			Scheduler scheduler = new Scheduler(asms);
			asms = new LinkedList<Asm>(scheduler.reSchedule());
		//====================================CodeSchedule============================================
		//======================================RegAlloc==============================================
			
			RegAlloc regalloc = new LinearScan();
			regalloc.bindRegister(asms);
		//======================================RegAlloc==============================================
		//=======================================CodeGen==============================================
			Codegen codegen = new Codegen();
			codegen.gen(asms);

			codegen.flush();
		//=======================================CodeGen==============================================	
		} catch(Error e) {
			e.printStackTrace();
			System.exit(1);
		}
		System.exit(0);
	}
	
	private static void getTokens(String filename) throws IOException {

		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		InputStream inp = new FileInputStream(filename);
		//PrintWriter out = new PrintWriter(new FileOutputStream(
			//	filename.replace(".c", ".s")));

		Yylex yy= new Yylex(inp);	//?
		java_cup.runtime.Symbol sym = null;
		try {
			sym = yy.next_token();		//everytime sth is matched or when sth. is returned
			while(sym.sym != Yylex.EOF) {
				//String input = reader.readLine();
				//System.out.println(sym.sym);
				switch(sym.sym)	{	//??
				case Symbols.NUM: System.out.println(sym.value); System.out.println(sym.value);break;
				case Symbols.STRINGTEXT: System.out.println(sym.value); 
									 //System.out.println(sym.value);
									 break;
				case Symbols.CHARACTER: System.out.println(sym.value+"test the whitespace"); break;
				default: System.out.println(yy.yytext()); 
						 //System.out.println(yy.yytext()); 
						 //System.out.println(yy.string+"in default");
				}
				sym=yy.next_token();
			
			}
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			inp.close();
		}
		
		//out.close();
		//System.out.println(filename.replace(".c", ".s"));
	}
	
	public static void main(String argv[]) throws IOException{
		//System.out.println(pathOf(argv[0]));
		compile(argv[0]);
		
		//compile(pathOf("newtest.c"));
		//getTokens(pathOf("example2.c"));
		//getTokens(pathOf("example3.c"));
		//getTokens(pathOf("newtest.c"));
		//System.out.println(calc(pathOf("expr.c")));
	}
}
