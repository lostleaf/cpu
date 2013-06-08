package roxanne.analysis;

import roxanne.addr.Temp;
import roxanne.error.Error;
import roxanne.translate.Level;
import roxanne.util.Constants;

public class LiveInterval implements Constants, Comparable<LiveInterval> {
	private Temp temp = null;
	private int sp;
	private int ep;
	//for dp
	public int value = 0;
	
	public LiveInterval(Temp t, int start, int end) {
		sp = start;
		ep = end;
		temp = t;
	}
	
	public void expand(int q) {
		if (q<sp) sp = q;
		if (q>ep) ep = q;
		++value;
 	}
	
	public int compareTo(LiveInterval interval) {
		return sp-interval.sp;
	}
	
	public int getStartPoint() {
		return sp;
	}
	
	public int getEndPoint() {
		return ep;
	}
	
	public Temp getTemp() {
		return temp;
	}
	
	public void bind(Integer regNum) {
		temp.bind(regNum);
		temp.level.addUsedRegister(regNum);
	}
	
	public Integer getRegister() {
		return temp.getRegister();
	}
	
	public void spill(Level lvl) throws Error {
		temp.spill(lvl);
	}
	
	public String toString() {
		return temp+": ["+sp+", "+ep+"] ("+value+")"+", reg: "+temp.printRegAlloc();
	}
}
