Plan for Tomasulo With Reorder Buffer
====
##Unit List
1. reorder buffer (RB)
1. alu_rs (rs stands for reservation station)
    - can be used as mul or add,sub. But one alu_rs can be used for one purpose only, either mul,muli or add,addi,sub,subi
    - 3 alu_rs for mul,muli and 3 alu_rs for add,addi, sub, subi
1. 3 load_rs
1. 1 store_rs
1. 1 branch_rs
1. reg_status
1. reg_file
1. 1 data_cache
1. data_memory
1. 1 inst_cache
1. inst_memory
1. 1 CDB_data bus
    - it consists of 3 groups of wires:
        - wire[WORD_SIZE*RB_SIZE-1:0] CDB_data_data
            - data of each rs is written to it
        - wire[RB_SIZE-1:0]           CDB_data_valid
            - to show whether the data on CDB_data_data and CDB_data_addr is valid or not
        - wire[WORD_SIZE*RB_SIZE-1:0] CDB_data_addr
            - store_rs put the address on CDB_data_addr, and the write data on CDB_data_data
1. 1 CDB_data_controller
    - Since each rs can write to CDB_data bus, which will easily cause conflicts, we use CDB_data_controller to deal with all writes to the CDB_data bus.
1. 1 CDB_inst
    - for RB to issue instruction to the corresponding function unit(fu)
    - it consists of 3 groups of wires
        - wire[FU_INDEX-1:0]  CDB_inst_fu (to which fu)
        - wire[WORD_SIZE-1:0] CDB_inst_inst (inst to issue)
        - wire[RB_INDEX-1:0]  CDB_inst_RBindex (write result to this RB entry)



##Plan for Each Crutial Stage
####All writes occur at negedge while the command of write issue before this
##1 IF
###Reorder Buffer
	@posedge: 
		check if RB not full
			new PC put into PC
			get Instr from instr cache
	@negedge: 
		if (instr miss)
		  wait until the 99th cycle's negedge
		
		if (j)
			pc = jump target
        else 
		  get the instr and add to RB's back
		  Pc = Pc+1
####Note: Pc can be affected by branch at write back cycle.
		
##2 ISSUE
###Reorder Buffer
	@posedge
		if (inc(tail)) has instr			
		// if (RB_valid[inc(tail)]), inc(tail) = （tail+1）%RB_SIZE
			issue if can
			tail = inc(tail)
	

####issue if can

RB
	
	@posedge
		if there's a corresponding FU not busy
			<FU, RB entry index, inst> put onto CDB_inst
	    issue the command of updating reg_status
####Note: When "write-back" wants to update the same reg status, do not write back.
	    
Reg_status
	
	@negedge
		update Register status 

RS

	@posedge
	
	   if (!busy)
		#0.1 if see fu on CDB_inst_fu == RS's fuindex
			update
		       busy, op, 
		       invaild(set the correspoding CDB_data_valid to invalid), 
		       dest(the RB entry that issued the command)
			check corresponding register status to update Qj, Qk, Vj, Vk (and Qi, Vi if needed)
			for i, j, k
				if (Q ready)
					put the data onto V
				else check corresponding reorder buffer entry's CDB
					if (ready)
						put the data on V
					else set wait for index 	
					
		     for branch_rs		
		          it will execute and set CDB_data_data (jump? 1:0) at this issue cycle

##3 execute
###each RS (eg. a mul has 3 RS, each work independently)
	@posedge
		// since busy set at #0.1 after posedge
		// for a newly issued op, it'll wait until the next cycle to execute
		// put... onto CDB_data_... is a command issued to CDB_data_controller, which will truly write the data @(negedge clk)
		if (busy)
			if(Qj and Qk are both ready) {
		       if (add or sub or branch)
                     #0.1 set CDB_data_valid valid
                     put the result onto CDB_data_data
       	       else if (mul)
		      		#3.1 set CDB_data_valid valid
                    put the result onto CDB_data_data
			   else if (load buffer) {
			     	#0.5 if (hit)
			     		read cache data
			     	else 
			     	#99/*at the 99th cycle's negedge*/ read cache data 
			   	     
			        set CDB_data_valid valid
                    put the data onto CDB_data_data
			   } else if (store buffer) {
				    // mem[Qj+Qk] = Qi
				    if (Qi ready)
				    #0.1/*at negedge*/ put Qj+Qk onto the CDB_data_addr
				    put Qi onto CDB_data_addr
			   }
		}

##4 write back
####Reorder Buffer
@negedge 

    #0.1 check each entry's corresponding CDB
          if (there's a branch which wants to jump) {
                pc = jump target // since it update pc after the IF stage, the new pc will be set to jump target
          }

@posedge

	if (RB_data_valid[head])
		if (write to reg && the reg is still waiting for its data)
			write it to the register file
			set corresponding register status to empty
		else if (write to cache) {
			if (cnt_enable && cnt < MEM_STALL)
				#(MEM_STALL-cnt) begin end
			cnt = 0;
			cnt_enable = 1'b0;

			we_mem = 1'b1;
			wd_mem = RB_data[head];
			ws_mem = RB_addr[head];

			#0.6 if (!mem_hit) {
			cnt        = 1;
			cnt_enable = 1'b1;
            }
            // for the purpose of having the cnt and cnt_enable, see optimization-write back
		}


##Hardware Optimization
###RB
Using 3 pointers--head, tail and back--instead of the only 2 pointers -- head and tail-- normally used in RB.

Back is to load instruction in advance to reduce stall that may encounter if an instruction is only loaded when tail is about to issue it.
###Branch
The branch_rs will check its data availability at issue cycle. So if the data is available at the issue cycle, there'll be only 1 stall.
###Write back
When writing back to data cache and a write miss occurs, reorder buffer will not just wait until the write is done. It will still write to register file and will only stall at the next write to the memory.
###Data cache
The data cache has one write port and 3 read ports corresponding to the one store_rs and the 3 load_rs, so that the 3 load_rs can load data simultaneously.