Plan for Tomasulo With Reorder Buffer
====
##RB

head->

tail->

back->


##1 IF
###Reorder Buffer
	@posedge: 
		check if not full
			new PC put into PC=> get Instr from instr cache
	@negedge: 
		if (instr hit)
			get the instr -> add to RB back
			Pc = Pc+4
		else 
			wait until the 99th cycle's negedge
			to get the instr and add to RB's back
			if (j)
				flush tail+1 <= entry index <= back
				PC = PC+PCoffset	// +4??
			else PC = PC+4

##2 ISSUE
###Reorder Buffer
	@posedge
		if (tail+1) has instr
			if (branch && jump)
				set PC to RB[tail]+PCOFFSET
			else
				issue if can
				++tail
	@negedge
		if head has instr
			write
			++head;

####issue if can

RB
	
	@posedge
		if FU not busy
			<FU, RB entry index, instr> put onto FU's CDB_instr
	@negedge
		update Register status (I'll write)

RS

	@posedge
		#0.1 if see <FU, RB entry index, instr>
			update busy, op, invaild, Dest
			check corresponding register status to update Qj, Qk, Vj, Vk
				if (Qj ready)
					put the data onto Vj
				else check corresponding reorder buffer entry's CDB
					if (ready)
						put the data on Vj
					else set wait for index 
				

##3 execute
###each RS (eg. a mul has 3 RS, each work independently)
	@posedge
		// since busy set at #0.1 after posedge
		// for a new op, it'll wait until the next cycle to execute
		if (!busy || Qj or Qk not ready)
			set corresponding ALU_enable false
		else {
			set ALU's in1 and in2
			if (add or sub)
				#0.5/*at negedge*/ read ALU_out and put it onto the Dest's CDB
			else if (mul)
				#3.5 read ALU_out and put it onto the Dest's CDB
			else if (load buffer) {
				#0.5 if (hit)
					read cache data and put it onto the Dest's CDB
				     else {
					#99/*at the 99th cycle's negedge*/ read cache data and put it onto the Dest's CDB     
				     }
			} else if (store buffer) {
				// mem[Qj-Qk] = Qi
				if (Qi ready)
				#0.5/*at negedge*/ read Qj+Qk and put it onto the Dest's CDB			read Qi put onto Dest's addr CDB
			}
		}

##4 write back
####Reorder Buffer
@posedge check head's corresponding CDB

	if (data valid)
		if (Rdest not empty)
			write it to the register file
			set corresponding register status to empty
		else // addr not empty
			write data to mem[addr] in dcache
####!! important 
	since no read and write from the same mem addr, RAW hazard will not happen


##Questions remained 
####Dcache
has multiple addr and rdata

	if hit, read simultaneously
	else only one request is put into memory??
	
####memory
2 addr port
write and read simultaneously?? will there be any hazard??

####branch
how to deal with branch
when to flush?? when to set PC
RB check oprand for branch ready or not
if not, stop to do anything??

####Work Allocation??
#### li?
good instruction referred in VMIPS p266

CVI	V1, R1//	V1[i] = i*R1

we could have 

CVI		V1, imm		// V1[i] = i*imm

	
