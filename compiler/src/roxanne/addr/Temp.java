package roxanne.addr;

import java.util.LinkedList;

import roxanne.analysis.LiveInterval;
import roxanne.asm.Asm;
import roxanne.asm.Asm.Op;
import roxanne.error.*;
import roxanne.error.Error;
import roxanne.env.VarEntry;
import roxanne.quad.Quad;
import roxanne.quad.Quad.ConstMode;
import roxanne.semantic.*;
import roxanne.types.TYPE;
import roxanne.translate.Level;
import roxanne.translate.Translator;
import roxanne.util.Constants;

public class Temp extends Addr implements Constants {
	public Integer num = 0;
	public static Integer count = 0;
	public Addr width;
	public TempMode mode = TempMode.TEMP;
	
	private LiveInterval interval = null;
	private	Integer regnum = spillReg;
	private boolean mustbespilled = false;
	
	public static Temp fp = new Temp(Constants.fp);
	public static Temp gp = new Temp(Constants.gp);
	public static Temp sp = new Temp(Constants.sp);
	public static Temp ra = new Temp(Constants.ra);
	public static enum TempMode {SU, VAR, ARRAY, POINTER, TEMP};
	
	/*
	 * the content in temp is saved at index(addr)
	 * it is bind to a varentry if it can
	 */
	public Addr addr = null;
	public Addr index = null;
	public Level level=null;
	public VarEntry entry = null;
	
	public Temp(int i) {
		num = count++;
		width = new Const(wordSize);
		level = null;
		entry = null;
		regnum = i;
	}
	public Temp(Addr w, Level l) {
		num = count++;
		width =w;
		level = l;
		entry = null;
	}
	public Temp(Addr w, Level l, VarEntry e) {
		num = count++;
		width =w;
		level = l;
		entry = e;
	}
	
	public String toString() {
		if (this == fp) return "$fp";
		if (this == gp) return "$gp";
		if (this == sp) return "$sp";
		if (this == ra) return "$ra";
		else {
			if (entry != null)
				return entry.name.toString();
			else if (addr != null)
				if (addr instanceof Const) {
					assert(index == null);
					return addr.toString()+"(0)";
				} else {
					assert(addr instanceof Temp);
					return ((index == null)? 0:index.toString())+"("+addr.toString()+")";
				}
			else
				return "$t"+num;
		}
	}
	
	public Temp getAddrUse() {
		if (addr != null && addr instanceof Temp) return ((Temp)addr).getAddrUse();
		return null;
	}
	
	public Temp getUse() {
		if (addr == null) return this;
		return (addr != null && addr instanceof Temp)? ((Temp)addr).getAddrUse():null;
	}
	
	public void expand(int cnt) {
		if (interval == null)
			interval = new LiveInterval(this, cnt, cnt);
		else interval.expand(cnt);
	}
	
	public void setInterval(LiveInterval i) {
		interval = i;
	}
	
	public LiveInterval getInterval() {
		assert(interval != null);
		return interval;
	}
	
	public boolean spilled() {
		return regnum == spillReg;
	}
	
	
	/*
	 *  for CodeGen
	 *  must genAfterDef first and then, genAfterUse 
	 *  for the same register, genUse or genDef for once is enough
	 *  as to before, use genBeforeUse
	 *  as to after, use genAfterDef
	 */
	
	
	// return the name of its allocated register or the register where it is load to
	// without sw before use reg
	public static Temp genLoadIfNeed(LinkedList<Asm> strings, Temp temp) {
		if (!temp.mustBeSpilled()) return temp;
		
		//String addrName = null;
		Temp ans = temp.level.newTemp(temp.width);
		if (temp.addr instanceof Temp) {
			Temp addr = genLoadIfNeed(strings,(Temp)temp.addr);
			if (temp.index == null || temp.index instanceof Const) {
				Addr index = Quad.genBeforeUseConst(strings, (Const)temp.index, temp.level, ConstMode.PCOFFSET);
				strings.add(new Asm(Op.lw, ans, addr, index));
			} else {
				//temp.index instanceof Temp
				Temp index = genLoadIfNeed(strings, (Temp)temp.index);
				strings.add(new Asm(Op.lwrr, ans, addr, index));
			}
		} /*else {
			System.out.println("test: "+temp.getRegister());
			assert(temp.index == null); 	// addr is label or const
			//addrName = temp.addr.gen();
			strings.add(new Asm(Op.lw, ans, temp.addr, null));
		}*/
		return ans;
	}
	
	public Temp genBeforeUse(LinkedList<Asm> strings) {
		return genLoadIfNeed(strings, this);
	}
		
	
	public LinkedList<Asm> genAfterDef(Temp storeDst) {
		// replaceRegNum must be three, use $t0 if needed for the address;
		/*
		 * if addr needs load, load addr using regForAddr
		 * sw	$tn, index(addr)	// $tn is $regForTemp
		 */
		LinkedList<Asm> strings = new LinkedList<Asm>();
		
		//String addrName = null;
		if (addr instanceof Temp) {
			Temp newAddr = genLoadIfNeed(strings, (Temp)addr);
			if (index ==  null || index instanceof Const) {
				Addr newIndex = Quad.genBeforeUseConst(strings, (Const)index, level, ConstMode.PCOFFSET);
				strings.add(new Asm(Op.sw, storeDst, newAddr, newIndex));
			} else {
				Temp newIndex = genLoadIfNeed(strings, (Temp)index);
				strings.add(new Asm(Op.swrr, storeDst, newAddr, newIndex));
			}
		} /*else {
			assert(index == null); 	// addr is label or const
			addrName = addr.gen();
			strings.add("\tsw\t"+regNames[regForTemp]+", "+addrName);
		}*/
		
		return strings;		
	}
	
	public void bind(Integer saveRegIndex) {
		regnum = saveRegIndex;
	}
	
	public Integer getRegister() {
		return regnum;
	}
	
	public void spill(Level lvl) throws Error {
		regnum = spillReg;
		if (addr!=null) return;
		addr = Translator.fpgp(lvl.parent == null);
		index = lvl.newLocal((Const) width);
	}
	
	public String gen() {
		assert(regnum != spillReg);
		return regNames[regnum];
	}
	
	/*public String printRegAlloc() {
		if (regnum == spillReg) {
			if (addr instanceof Temp)
				return Quad.genAddress(index, ((Temp)addr).printRegAlloc());
			else return (addr == null? null:addr.gen());
		} else return regNames[regnum];
	}
	*/
	/*
	 *  must be spilled <=> this can not be bound with a reg and only its addr will be used.
	 *  therefore, no need to put this into use. therefore, this and its addr only one needs to be put in use
	 *  
	 *  all the temps whose addr needs to be put into use (i.e. addr is not gp or fp) <=(contained in) temps which must be spilled
	 *  
	 *  all the must-be-spilleds do not participate in regAlloc,
	 *  and therefore, it's in neither in nor out
	 *  but in getIntervals, we cannot kill the quad that defines a must-be-spilled
	 */
	
	public void setMustBeSpilled() {
		mustbespilled = true;
	}
	
	public boolean mustBeSpilled() {
		return mustbespilled;
	}
}
