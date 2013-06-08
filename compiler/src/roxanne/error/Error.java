package roxanne.error;

public class Error extends Exception {
	StringBuffer msg;
	public Error() { msg = new StringBuffer(); }
	public Error(String m) { msg = new StringBuffer(m); }
	public String toString() {return msg.toString();}
}
