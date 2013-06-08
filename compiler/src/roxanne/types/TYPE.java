package roxanne.types;

import roxanne.addr.Addr;

public abstract class TYPE {
	abstract public TYPE actual();
	protected Addr width = null;
	public abstract Addr getWidth();
	public boolean equals(TYPE t) {
		if (t == null) return false;
		if (this instanceof INT || this instanceof CHAR)
			return t instanceof INT || t instanceof CHAR;
		else if (this instanceof VOID)
			return this==t;
		else if (this instanceof POINTER) 
				if (!(t instanceof POINTER)) return false;
				else return ((POINTER)this).equals((POINTER)t);
		else if (this instanceof ARRAY)
				if (!(t instanceof ARRAY)) return false;
				else return ((ARRAY) this).equals((ARRAY) t);
		else if (this instanceof RECORD)
				if (!(t instanceof RECORD)) return false;
				else return ((RECORD) this).equals((RECORD) t);
		else if (this instanceof PARAS)
				if (!(t instanceof PARAS)) return false;
				else return ((PARAS) this).equals((PARAS) t);
		else if (this instanceof FUNC)
				if (!(t instanceof FUNC)) return false;
				else return ((FUNC) this).equals((FUNC)t);
		else if (this instanceof STRUCT)
				if (!(t instanceof STRUCT)) return false;
				else return ((STRUCT)this).equals((STRUCT)t);
		else if (this instanceof UNION)
			if (!(t instanceof UNION)) return false;
			else return ((UNION)this).equals((UNION)t);
		else if (this instanceof WRONG)
				if (!(t instanceof WRONG)) return false;
				else return true;
		return false;		
	}
}
