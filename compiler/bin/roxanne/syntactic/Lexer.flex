package roxanne.syntactic;

%%

%unicode
%line
%column
%cup
%public
%implements Symbols	
%{
	private StringBuffer string = new StringBuffer();
	
	private void err(String message) {
		System.out.println("Scanning error in line " + yyline + ", column " + yycolumn + ": " + message);
	}	//?the column is the start column?

	private java_cup.runtime.Symbol tok(int kind) {
		return new java_cup.runtime.Symbol(kind, yyline, yycolumn);
	}

	private java_cup.runtime.Symbol tok(int kind, Object value) {
		return new java_cup.runtime.Symbol(kind, yyline, yycolumn, value);
	}
	private static Integer num2int(String s, int base) {
		int start = 0;
		if (base == 16) start = 2;
		int len = s.length(), first = len;
		for(int i = start; i < s.length(); ++i) {
			//System.out.println(s.charAt(i));
			if (s.charAt(i) != '0') {
				first = i;
				break;
			}
		}
		if (first == len) { return new Integer(0); }
		int ans = 0;
		int si=0;
		char c;
		for (int i = first; i < len; ++i) {
			c=s.charAt(i);
			si = c-'0';
			if (base == 16) {
				if ('a'<=c && c<='f') si = (c-'a'+1)*10;
				else if ('A'<=c && c<='F') si = (c-'A'+1)*10;	
			}
				
			ans = ans*base+si;
		}
		return ans;
	}
	private static char str2char(String s) {		//why static?
		StringBuffer str = new StringBuffer("");
		int len = s.length();
		for (int i = 1; i<len-1; ++i) {
			str.append(s.charAt(i));
		}
		char ans = ' ';
		if (str.length() == 1) { 
			ans = str.charAt(0);
			return ans;
		}
		switch(str.charAt(1)) {	//str[0] !=\?
			case 't': 	ans = '\t'; break;
			case 'r': 	ans = '\r'; break;
			case '\'': 	ans = '\''; break;
			case 'n':	ans = '\n'; break;
			case 'f':	ans = '\f'; break;
		}
		return ans;
	}
%}

%eofval{
	{
		if (yystate() == MCOMMENT) {
			err("Comment symbol /* */ do not match (EOF)!");
		 } 
		 else if (yystate() == STRING) {
		 	err("The last string is incomplete (EOF)!");
		 }
		 return tok(EOF, null);
	}
%eofval}

LineTerm = \n|\r|\r\n
Identifier = [_$a-zA-Z][_$a-zA-Z0-9]*
DecInteger = [1-9][0-9]*|0
OctInteger = 0[0-7]+		//?
HexInteger = 0x[0-9A-Fa-f]+		//start with 0?
Char = '[^\']'|'\\[nftr0]'|'\\''	//?
Whitespace = {LineTerm}|[ \t\f]

%state	MCOMMENT
%state	SCOMMENT
%state	STRING
%state	PREPROCESS

%%

<YYINITIAL> {
	"#"		{ yybegin(PREPROCESS); }
	"/*"	{ yybegin(MCOMMENT); }
	"*/" 	{ err("Multi-line Comment symbol /**/ do not match!"); }
	"//"	{ yybegin(SCOMMENT); }
	
	"\""	{ string.delete(0, string.length()); yybegin(STRING);}	//??

	
	"typedef" { return tok(TYPEDEF); }
	"void" 	 { return tok(VOID); }
	"char"	 { return tok(CHAR); }
	"int"	 { return tok(INT); }
	"struct" { return tok(STRUCT); }
	"union"	 { return tok(UNION); }
	"if"     { return tok(IF); }
	"else"	 { return tok(ELSE); }
	"while"	 { return tok(WHILE); }
	"for"	 { return tok(FOR); }
	"continue" { return tok(CONTINUE); }
	"break"	 { return tok(BREAK); }
	"return" { return tok(RETURN); }
	"sizeof" { return tok(SIZEOF); }

	"(" { return tok(LPAREN); }
	")" { return tok(RPAREN); }
	"{" { return tok(LBRACE); }
	"}" { return tok(RBRACE); }
	"[" { return tok(LBRACKET); }
	"]" { return tok(RBRACKET); }

	";" { return tok(SEMICOLON); }
	"," { return tok(COMMA); }
	"." { return tok(DOT); }
	"..." { return tok(ELLIPSIS); }	//When is it used??
	
	"+" { return tok(PLUS); }
	"-" { return tok(MINUS); }
	"*" { return tok(TIMES); }
	"/" { return tok(DIVIDE); }
	"%" { return tok(MOD); }
	"++" { return tok(INC); }
	"--" { return tok(DEC); }
	"->" { return tok(PTR); }
	

	"==" { return tok(EQ); }
	"!=" { return tok(NE) ;}
	"<"  { return tok(LT); }
	"<=" { return tok(LE); }
	">"  { return tok(GT); }
	">=" { return tok(GE); }
	
	"|"	 { return tok(BITOR);}
	"^"	 { return tok(BITXOR); }
	"&"	 { return tok(BITAND); }
	"~"	 { return tok(BITNOT); }	//correct??
	"!"	 { return tok(NOT); }
	"||" { return tok(OR); }
	"&&" { return tok(AND); }
	
	"<<" { return tok(SHL); }
	">>" { return tok(SHR); }

	"="  { return tok(ASSIGN); }
	"*=" { return tok(MULASSIGN); }
	"/=" { return tok(DIVASSIGN); }
	"%=" { return tok(MODASSIGN); }
	"+=" { return tok(ADDASSIGN); }
	"-=" { return tok(SUBASSIGN); }
	"&=" { return tok(ANDASSIGN); }	//bit-wise
	"|=" { return tok(ORASSIGN); }
	"^=" { return tok(XORASSIGN); }
	"<<=" { return tok(SHLASSIGN); }
	">>=" { return tok(SHRASSIGN); }

	{Identifier} { String s = yytext();
				   if (Parser.idcontrol.isType(s)){
					   //System.out.println(s+" is a type"); 
					   return tok(TYPEID, s);
				   } else {
					   //System.out.println(s+" is an id");
				   		return tok(ID, yytext());  
				   }
				 }
	{DecInteger} { return tok(NUM, new Integer(yytext())); }
	{OctInteger} { return tok(NUM, num2int(yytext(),8)); }	
	{HexInteger} { return tok(NUM, num2int(yytext(),16)); }
	{Char}		 { return tok(CHARACTER, new Character(str2char(yytext()))); }
	{Whitespace} { /* skip */ }
	
	[^] { throw new RuntimeException("Illegal character " + yytext() + " in line " + (yyline + 1) + ", column " + (yycolumn + 1)); }
}

<MCOMMENT> {
	"*/" { yybegin(YYINITIAL); }
	[^]  {}
}

<PREPROCESS> {
	"\n" { yybegin(YYINITIAL); }
	[^]	 {}		
}

<SCOMMENT> {
	"\n" { yybegin(YYINITIAL); }
	[^]	 {}	
}
<STRING> {
	"\n"	{ throw new RuntimeException("new line in a string in line " + (yyline + 1) + ", column " + (yycolumn + 1)); }
	//" "		{ string.append(" "); }
	"\\0"	{ string.append("\\0"); }
	"\\\""	{ string.append("\\\""); }
	"\\n"	{ string.append("\\n");  }
	"\\r"	{ string.append("\\r");  }
	"\\t"	{ string.append("\\t");  }
	"\\f"	{ string.append("\\f");  }
	"\\'"	{ string.append("'");  }		//needed??
	"\\\\"	{ string.append("\\\\"); }
	"\""	{ yybegin(YYINITIAL);
			return tok(Symbols.STRINGTEXT, string.toString());		//!!!! already exist a state called STRING=>the STRING will refer to that state instead of Symbols.STRING
			}
	[^\"\\]	{ string.append(yytext()); }		//correct??
}
