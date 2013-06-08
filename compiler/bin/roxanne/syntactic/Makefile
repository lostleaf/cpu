all: Parser.java Symbols.java Yylex.java

Parser.java: Lexer.cup
	java -jar ../../../lib/java-cup-11a.jar -parser Parser -symbols Symbols -interface  -expect 300< Lexer.cup

Symbols.java: Lexer.cup
	java -jar ../../../lib/java-cup-11a.jar -parser Parser -symbols Symbols -interface < Lexer.cup

Yylex.java: Lexer.flex
	java -cp ../../../lib/JFlex.jar JFlex.Main Lexer.flex

clean:
	rm -f Parser.java Symbols.java Yylex.java
