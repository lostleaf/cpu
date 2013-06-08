package roxanne.ast;

import java.util.LinkedList;

public class Str extends Expr {
	public String string;
	
	public Str(String s){
		string = s;
	}
	
	public LinkedList<String> cut() {
		LinkedList<String> strings = new LinkedList<String>();
		
		// part of format implemented
		int lastindex = 0;
		int len = string.length();
		for (int i = 0; i < len; ++i) {
			if (string.charAt(i)=='%') {
				int start = i;
				char format = string.charAt(i+1);
				if (format == '0') {
					i+=2;
					format = string.charAt(i+1);
				}
				if (format == 'd' || format == 'c' || format == 's' || format == '%') {
					if (start > lastindex)
						strings.add(string.substring(lastindex, i));
					++i;
					lastindex = i+1;
					strings.add(string.substring(start, i+1));
				} else throw new Error("incomplete format");

			} 
		}
		if (lastindex < len)
			strings.add(string.substring(lastindex, len));
		
		return strings;
	}
}
