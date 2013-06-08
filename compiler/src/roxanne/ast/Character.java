package roxanne.ast;
// a kind of expr, stores the character, while Char denotes the type
public class Character extends Expr {
	public char c;
	public Character(char cc) {
		c = cc;
	}
}
