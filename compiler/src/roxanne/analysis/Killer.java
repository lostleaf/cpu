package roxanne.analysis;

import java.util.LinkedHashSet;

import roxanne.addr.Temp;

public class Killer {
	public static void kill(LinkedHashSet<Temp> in, LinkedHashSet<Temp> def) {
		// so far simple version
		in.removeAll(def);
		//System.out.println(in);
		/*boolean killlv = false;
		for (Temp t:def) {
			if (t != Temp.fp && t != Temp.gp && t.addr != null && t.entry == null) {
				killlv = true;
			}
		}
		if (killlv)
			for(Temp t: in) {
				if (t.addr != null)
					in.remove(t);
			}*/
	}

}
