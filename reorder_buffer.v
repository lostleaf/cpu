`include "timescale.v"
`include "inst_cache.v"
module reorder_buffer(CDB_data_data, CDB_data_valid, CDB_data_addr, busy, 
						we_reg, wd_reg, ws_reg,
						we_mem, wd_mem, ws_mem, mem_hit,
						//numj, numk, vj, vk, qj, qk, 
						numRB, qRB,
						CDB_inst_fu, CDB_inst_inst, CDB_inst_RBindex,
						Rdest_status_issue, RB_index_status_issue,	we_status_issue,
						Rdest_status_wb,	RB_index_status_wb,		we_status_wb,
						reset_out, reset, clk);

	`include "parameters.v"

	input	wire[RB_SIZE*WORD_SIZE-1:0]	CDB_data_data, CDB_data_addr;
	input	wire[RB_SIZE-1:0]			CDB_data_valid;

	input	wire[FU_NUM-1:0]	busy;

	output	reg we_reg = 1'b0, we_mem = 1'b0;
	output	reg [WORD_SIZE-1:0]	wd_reg, wd_mem;
	output	reg [REG_INDEX-1:0]	ws_reg;	
	output	reg [WORD_SIZE-1:0]	ws_mem;
	input	wire 				mem_hit;

	output	reg [REG_INDEX-1:0]	numRB; //numj = 'bz, numk = 'bz;
	//input	wire[WORD_SIZE-1:0]	vj, vk;
	input	wire[RB_INDEX-1:0]	qRB;//qj, qk;

	output	reg [FU_INDEX-1:0]	CDB_inst_fu;
	output	reg [WORD_SIZE-1:0]	CDB_inst_inst;
	output	reg [RB_INDEX-1:0]	CDB_inst_RBindex;

	output	reg [REG_INDEX-1:0]	Rdest_status_issue, Rdest_status_wb;
	output	reg [RB_INDEX-1:0]	RB_index_status_issue, RB_index_status_wb;
	output	reg 				we_status_issue = 1'b0, we_status_wb = 1'b0;

	input	wire clk, reset;
	output 	reg[FU_NUM-1:0]  reset_out = ~0;

	//reg [WORD_SIZE-1:0]	RB_PC	[RB_SIZE-1:0];
	reg [REG_INDEX-1:0]	RB_Rdest[RB_SIZE-1:0];
	reg [FU_INDEX-1:0]	RB_fu	[RB_SIZE-1:0];
	reg [WORD_SIZE-1:0]	RB_addr	[RB_SIZE-1:0];
	reg [WORD_SIZE-1:0]	RB_data	[RB_SIZE-1:0];
	reg 				RB_valid[RB_SIZE-1:0];
	reg [WORD_SIZE-1:0]	RB_inst [RB_SIZE-1:0];
	reg 				RB_data_valid[RB_SIZE-1:0], RB_to_mem[RB_SIZE-1:0];
	// ready?

	reg [RB_INDEX-1:0]	head = 'b0, tail = 'b0, back = 'b0;

	reg 	cache_enable = 1'b0;
	wire	hit;
	wire[WORD_SIZE-1:0]	inst;
	reg [WORD_SIZE-1:0] pc;
	// for write back
	reg [WORD_SIZE-1:0]	cnt;
	reg 				cnt_enable = 1'b0;
	// free means issued
	reg free;
	reg [OPCODE_WIDTH-1:0] op;

	inst_cache icache(inst, clk, pc, hit, cache_enable);
	

	always @(posedge clk or posedge reset) begin
		// $display($realtime, "reset = %b", reset);
		if (reset) begin:rst
			reg [WORD_SIZE-1:0]	i;
			for (i = 0; i <  RB_SIZE; i = i + 1) begin
				RB_valid[i]      = 1'b0;
				RB_data_valid[i] = 1'b0;
			end	
			pc          = 0;
			we_reg      = 1'b0; 	we_mem	= 1'b0;	we_status_issue = 1'b0;	we_status_wb = 1'b0;
			head        = 1'b0;		tail 	= 1'b0;	back 			= 1'b0;
			CDB_inst_fu = NO_FU;
			//numj        = 'bz;		numk	= 'bz;
			cnt         = 0;
			cnt_enable  = 1'b0;
			reset_out	= ~'b0;
			#0.1 reset_out	= 'b0;
		end
		else 
			// $display(RB_inst[back][31:28]);
			// $display($realtime, "notFull = %b", notFull(head, back));
			if (!notFull(head, back)) begin
				$display($realtime, "pc = %g, full, head = %g, back=%g, tail = %g",
					pc, head, back, tail);
			end
			if (notFull(head, back) && RB_inst[back][31:28] !== INST_HALT)  begin: IF
				cache_enable = 1;
				#0.5 if (hit) begin
				end
				else begin
					#(MEM_STALL-1) begin end
				end
				if (inst[WORD_SIZE-1:WORD_SIZE-OPCODE_WIDTH] === INST_J)
					pc = inst[J_PCOFFSET_START:0];
				else begin:addInst
					back = inc(back);
					//RB_PC[back] = pc;
					RB_valid[back] = 1'b1;
					RB_inst[back] = inst;
					$display($realtime, "pc : %d, RB_inst[%0d] = %b", pc , back, inst);
					pc = pc+1;
				end 
			end
			else begin end
	end

	always @(posedge clk) begin:issue
		reg [WORD_SIZE-1:0] inst_now;
		if (RB_valid[inc(tail)]) begin: issueIfCan
			reg[FU_INDEX-1:0] 	i;
			reg[FU_INDEX-1:0]	fuend;
			inst_now = RB_inst[inc(tail)];
			op = inst_now[WORD_SIZE-1:WORD_SIZE-OPCODE_WIDTH];

			getFuStartAndNum(op, i, fuend);
			free = 1'b0;
			/*if (op == INST_BGE) 
				$display("start = %d, end = %d", i, fuend);)*/
			if (op === INST_HALT) begin
				free = 1'b1;
				tail = inc(tail);
			end
			for (i = i; i <= fuend && !free; i = i+1) begin
				if (!busy[i]) begin
					tail                = inc(tail);
					CDB_inst_inst       = inst_now;
					CDB_inst_fu         = i;
					CDB_inst_RBindex    = tail;
					free                = 1'b1;
					
					RB_data_valid[tail] = 1'b0;
					RB_fu[tail]         = i;
					$display($realtime, "issuing to %d: op = %h, %b tail= %0d",i, op, inst_now, tail);
					if (op === INST_SWRR || op === INST_SW) begin
						RB_to_mem[tail] = 1'b1;
					end
					else if (op === INST_BGE) begin
							RB_to_mem[tail] = 1'b0;
						end
						else begin
							RB_to_mem[tail] = 1'b0;
							RB_Rdest [tail] = getRdest(inst_now);
						end
					end
				else begin 	end
			end
			// $display($realtime, "free = %b fuend = %0d %b", free, fuend, inst_now);
			//getRegStatusIssue
			if (!free) begin
				we_status_issue = 1'b0;
				CDB_inst_fu = NO_FU;
			end
			else begin
				if (op != INST_SW && op != INST_SWRR && op != INST_BGE && op != INST_HALT) begin
					//j or jr will not in RB, branch will set free = 0;
					// $display("%b %b", RB_inst[tail], op);
					Rdest_status_issue    = getRdest(RB_inst[tail]);
					we_status_issue       = 1'b1;
					RB_index_status_issue = tail;
					$display($realtime, "set status %d %d", Rdest_status_issue, RB_index_status_issue);
				end	
				else we_status_issue = 1'b0;
			end
		end
		else begin
			CDB_inst_fu = NO_FU;			// change RS!!
			we_status_issue = 1'b0;
		end
		//$display($realtime, "tail = %d", tail);
	end

	always @(negedge clk) begin: updateCDB
		reg [WORD_SIZE-1:0] i, target;
		reg hasBranch;
		reg [RB_INDEX-1:0]	mark;
		reg [OPCODE_WIDTH-1:0]	op;

		hasBranch  = 1'b0;
		#0.1 for (i = head; i != inc(tail); i = (i + 1) % RB_SIZE ) begin
			// $display("%d %b", readValidBus(CDB_data_valid, i), RB_inst[i]);
			if (readValidBus(CDB_data_valid, i)) begin
				op               = RB_inst[i][INST_START:INST_START-OPCODE_WIDTH+1];
				RB_data[i]       = readDataBus(CDB_data_data, i);
				RB_data_valid[i] = 1'b1;
			// $display($realtime, "  rb data valid ", head, "   ", tail, "  ", i);
				if (RB_to_mem[i] || op == INST_BGE) 
					RB_addr[i] = readDataBus(CDB_data_addr, i);
				if (op == INST_BGE) begin
					// $display("find branch");
					hasBranch = 1'b1;
					mark      = i;
					target    = RB_inst[i][12:0];
				end
			end
		end

		if (hasBranch) begin: updatePCAndResetIfNeed
			// $display("branch taken? %b", RB_data[mark]);
			if (RB_data[mark]) begin
				pc = target;
				for (i = mark; i != inc(back); i = inc(i)) begin
					reset_out = reset_out | (1'b1<<RB_fu[i]);
					RB_valid[i] = 1'b0;
					$display($realtime, "kill inst %b", RB_inst[i]);
				end
				mark = dec(mark);
				tail = mark;
				back = mark;
				#0.1 reset_out = 'b0;
			end
		end
	end

	always @(posedge clk) begin: writeBack	// issue the command at posedge, the the execution unit truly write data at negedge
		// $display($realtime, " %b", RB_inst[inc(head)]);
		if (RB_inst[inc(head)][31:28] === INST_HALT)begin
			$display("stop");	
			$finish;
		end 
		if (RB_valid[inc(head)] && RB_data_valid[inc(head)]) begin
			head = inc(head);
			RB_valid[head] = 1'b0;
			$display("write back %0d %b", head, RB_inst[head]);
			if (RB_inst[head][INST_START:INST_START-OPCODE_WIDTH+1] == INST_BGE) begin
				we_mem       <= 1'b0;
				we_reg       <= 1'b0;
				we_status_wb <= 1'b0;
			end
			else if (!RB_to_mem[head]) begin:writeToReg
					we_mem          = 1'b0;

					numRB = RB_Rdest[head];
					$display($realtime, "numRB = %g, qRB = %g", numRB, qRB);
					if (qRB == head) begin
						we_reg          = 1'b1;
						wd_reg          = RB_data[head];
						ws_reg          = RB_Rdest[head];
						
						we_status_wb       = 1'b1;
						RB_index_status_wb = READY;
						Rdest_status_wb    = RB_Rdest[head];
						// $display("%0d %b", head, RB_inst[4]);
					end else begin
						we_reg = 1'b0;
						we_status_wb = 1'b0;
					end
				end
				else begin:writeToMem
					we_reg    = 1'b0;
					we_status_wb = 1'b0;

					if (cnt_enable && cnt < MEM_STALL)
						#(MEM_STALL-cnt) begin end
					cnt = 0;
					cnt_enable = 1'b0;

					we_mem = 1'b1;
					wd_mem = RB_data[head];
					ws_mem = RB_addr[head];

					#0.6 if (!mem_hit) begin
						cnt        = 1;
						cnt_enable = 1'b1;
					end else begin	end
					$display($realtime, "mem_hit = %b, we:%g, wd:%g, ws:%g", 
								mem_hit, we_mem, wd_mem, ws_mem);
				end
		end
		else begin
			we_mem <= 1'b0;
			we_reg <= 1'b0;
			we_status_wb <= 1'b0;
		end
		// $display($realtime, "head = %d rstwb= %d", head, Rdest_status_wb);
	end

	always @(posedge clk) begin: updateRegStatus
		#0.1 if ((we_status_wb && we_status_issue) && (Rdest_status_issue === Rdest_status_wb)) begin
				we_status_wb = 1'b0;
			end
	end


	function[REG_INDEX-1:0]	getRdest;
		input [WORD_SIZE-1:0]	inst;
		reg [OPCODE_WIDTH-1:0]	op;
	begin
		op = inst[WORD_SIZE-1:WORD_SIZE-OPCODE_WIDTH];
		getRdest = inst[RD_START: RD_START-REG_INDEX+1];
	end
	endfunction
	
	task getFuStartAndNum;//(op, i, fuend);
		input [OPCODE_WIDTH-1:0] op;
		output[FU_INDEX-1:0] fu_start;
		output[FU_INDEX-1:0] fu_end;
		reg [FU_INDEX-1:0]	fu_num;
	begin
		case (op)
			INST_ADD : begin
				fu_start = ADDER_START;
				fu_num   = ADDER_NUM;
			end
			INST_SUB :begin
				fu_start = ADDER_START;
				fu_num   = ADDER_NUM;
			end
			INST_MUL : begin
				fu_start = MULTER_START;
				fu_num   = MULTER_NUM;
			end
			INST_LWRR: begin
				fu_start = LOADER_START;
				fu_num   = LOADER_NUM;
			end
			INST_SWRR: begin
				fu_start = STORER_START;
				fu_num   = STORER_NUM;
			end
			INST_ADDI:begin
				fu_start = ADDER_START;
				fu_num   = ADDER_NUM;
			end
			INST_SUBI: begin
				fu_start = ADDER_START;
				fu_num   = ADDER_NUM;
			end
			INST_MULI: begin
				fu_start = MULTER_START;
				fu_num   = MULTER_NUM;
			end
			INST_LW  : begin
				fu_start = LOADER_START;
				fu_num   = LOADER_NUM;
			end
			INST_SW  : begin
				fu_start = STORER_START;
				fu_num   = STORER_NUM;
			end
			INST_LI  : begin
				fu_start = LOADER_START;
				fu_num   = LOADER_NUM;
			end
			INST_BGE : begin
				fu_start = BRANCH_START;
				fu_num 	 = BRANCH_NUM;
			end
			INST_HALT : begin
				/*do nothing*/
			end
			default: begin
				$display($realtime, "fatal: RB is issuing a jump");
				$finish;
			end 
		endcase
		fu_end = fu_start+fu_num-1;
	end
	endtask

	function notFull;
		input[RB_INDEX-1:0]	head;
		input[RB_INDEX-1:0]	back;
	begin
		notFull = !(inc(back)==head);
	end
	endfunction

	function[RB_INDEX-1:0] inc;
		input[RB_INDEX-1:0] ptr;
	begin
		inc = (ptr+1)%RB_SIZE;
	end
	endfunction

	function[RB_INDEX-1:0] dec;
		input[RB_INDEX-1:0] ptr;
	begin
		dec = (ptr-1)%RB_SIZE;
	end
	endfunction

	task getData;	//(v, q, CDB_data_data, CDB_data_valid, V, Q)
		input[WORD_SIZE-1:0] v;
		input[RB_INDEX-1:0]	 q;
		output	reg [WORD_SIZE-1:0] V;
		input[WORD_SIZE*RB_SIZE-1:0] 	CDB_data_data;
		input[RB_SIZE-1:0]				CDB_data_valid;
		begin
			if (q === READY) begin
				V = v;
			end	else if (readValidBus(CDB_data_valid, q)) begin
						V = readDataBus(CDB_data_data, q);
					end
			else begin
				$display($realtime, "fatal: oprand for BGE is not ready");
				$finish;
			end
		end
	endtask

	function[WORD_SIZE-1:0] readDataBus;
		input[WORD_SIZE*RB_SIZE-1:0] CDB_data_data;
		input[RB_INDEX-1:0]			 index;		  
		begin
			readDataBus = CDB_data_data>>(index*WORD_SIZE);
			//$display("shr: %d, CDB_data_data = %h, readDataBus = %d", index*WORD_SIZE, CDB_data_data, readDataBus);
		end
	endfunction

	function readValidBus;
		input[RB_SIZE-1:0] 	CDB_data_valid;
		input[RB_INDEX-1:0]	index;		  
		begin
			readValidBus = CDB_data_valid>>index;
		end
	endfunction

endmodule