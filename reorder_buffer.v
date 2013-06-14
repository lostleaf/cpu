`include "timescale.v"
`include "inst_cache.v"
module reorder_buffer(CDB_data_data, CDB_data_valid, CDB_data_addr, busy, 
	we_reg, wd_reg, ws_reg, we_mem, wd_mem, ws_mem, numj, numk, vj, vk, qj, qk, 
	CDB_inst_fu, CDB_inst_inst, CDB_inst_RBindex, Rdest_status, RB_index_status, we_status, reset, clk);
	`include "parameters.v"

	input	wire[RB_SIZE*WORD_SIZE-1:0]	CDB_data_data, CDB_data_addr;
	input	wire[RB_SIZE-1:0]			CDB_data_valid;

	input	wire[FU_NUM-1:0]	busy;

	output	reg we_reg, we_mem;
	output	reg [WORD_SIZE-1:0]	wd_reg, wd_mem;
	output	reg [REG_INDEX-1:0]	ws_reg;	
	output	reg [WORD_SIZE-1:0]	ws_mem;

	output	reg [REG_INDEX-1:0]	numj, numk;
	input	wire[WORD_SIZE-1:0]	vj, vk;
	input	wire[RB_INDEX-1:0]	qj, qk;

	output	reg [FU_INDEX-1:0]	CDB_inst_fu;
	output	reg [WORD_SIZE-1:0]	CDB_inst_inst;
	output	reg [RB_INDEX-1:0]	CDB_inst_RBindex;

	output	reg [REG_INDEX-1:0]	Rdest_status;
	output	reg [RB_INDEX-1:0]	RB_index_status;
	output	reg 				we_status;

	input	wire clk, reset;

	reg [WORD_SIZE-1:0]	RB_PC	[RB_SIZE-1:0];
	reg [REG_INDEX-1:0]	RB_Rdest[RB_SIZE-1:0];
	reg [WORD_SIZE-1:0]	RB_addr	[RB_SIZE-1:0];
	reg [WORD_SIZE-1:0]	RB_data	[RB_SIZE-1:0];
	reg 				RB_valid[RB_SIZE-1:0];
	reg [WORD_SIZE-1:0]	RB_inst [RB_SIZE-1:0];
	// ready?

	reg [RB_INDEX-1:0]	head = 'b0, tail = 'b0, back = 'b0;

	reg 	cache_enable = 1'b0;
	wire	hit;
	wire[WORD_SIZE-1:0]	inst;
	reg [WORD_SIZE-1:0] pc;


	inst_cache icache(inst, clk, pc, hit, cache_enable);

	always @(posedge clk or posedge reset) begin: IF
		if (reset) begin:rst
			reg [WORD_SIZE-1:0]	i;
			for (i = 0; i <  RB_SIZE; i = i + 1) begin
				RB_valid[i] <= 1'b0;
			end	
			pc <= 0;
		end
		else if (notFull(head, back))  begin
				cache_enable = 1;
				#0.5 if (hit) begin
				end
				else begin
					#(MEM_STALL-1) begin end
				end
				if (inst[WORD_SIZE-1:WORD_SIZE-OPCODE_WIDTH] === INST_J)
					pc <= pc+inst[J_PCOFFSET_START:0];
				else begin:addInst
					back = inc(back);
					RB_PC[back] = pc;
					RB_valid[back] = 1'b1;
					RB_inst[back] = inst;
					$display($realtime, "back = %d, pc = %d, inst = %b", back, pc, inst);
					pc = pc+1;
				end 
			end
			else begin end
	end

	always @(posedge clk) begin:issue
		if (RB_valid[inc(tail)]) begin
			tail = inc(tail);
			//ignore brach so far
			if (RB_inst[tail][WORD_SIZE-1:WORD_SIZE-OPCODE_WIDTH] == INST_BGE) begin
				
			end
			else begin:issueIfCan
				reg free = 1'b1;
				reg[WORD_SIZE-1:0] 	i;
				reg[FU_INDEX-1:0]	fu;
				case(RB_inst[tail][WORD_SIZE-1:WORD_SIZE-OPCODE_WIDTH])
			end
		end
		else begin end
	end
	

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
endmodule