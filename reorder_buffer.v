`include "timescale.v"
`include "inst_cache.v"
module reorder_buffer(CDB_data_data, CDB_data_valid, CDB_data_addr, busy, 
	we_reg, wd_reg, ws_reg, we_mem, wd_mem, ws_mem, numj, numk, vj, vk, qj, qk, 
	CDB_inst_fu, CDB_inst_inst, CDB_inst_RBindex, Rdest_status, RB_index_status, we_status, reset, clk);
	`include "parameters.v"

	input	wire[RB_SIZE*WORD_SIZE-1:0]	CDB_data_data, CDB_data_addr;
	input	wire[RB_SIZE-1:0]			CDB_data_valid;

	input	wire[FU_NUM-1:0]	busy;

	output	reg we_reg = 1'b0, we_mem = 1'b0;
	output	reg [WORD_SIZE-1:0]	wd_reg, wd_mem;
	output	reg [REG_INDEX-1:0]	ws_reg;	
	output	reg [WORD_SIZE-1:0]	ws_mem;

	output	reg [REG_INDEX-1:0]	numj = 'bz, numk = 'bz;
	input	wire[WORD_SIZE-1:0]	vj, vk;
	input	wire[RB_INDEX-1:0]	qj, qk;

	output	reg [FU_INDEX-1:0]	CDB_inst_fu;
	output	reg [WORD_SIZE-1:0]	CDB_inst_inst;
	output	reg [RB_INDEX-1:0]	CDB_inst_RBindex;

	output	reg [REG_INDEX-1:0]	Rdest_status;
	output	reg [RB_INDEX-1:0]	RB_index_status;
	output	reg 				we_status = 1'b0;

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

	always @(posedge clk or posedge reset) begin
		if (reset) begin:rst
			reg [WORD_SIZE-1:0]	i;
			for (i = 0; i <  RB_SIZE; i = i + 1) begin
				RB_valid[i] <= 1'b0;
			end	
			pc <= 0;
			we_reg <= 1'b0; we_mem <= 1'b0;	we_status <= 1'b0;
			head <= 1'b0;	tail <= 1'b0;	back <= 1'b0;
			CDB_inst_fu <= NO_FU;
			numj <= 'bz;	numk <= 'bz;
		end
		else 
			if (notFull(head, back))  begin: IF
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
					pc = pc+1;
				end 
			end
			else begin end
	end

	always @(posedge clk) begin:issue
		reg [WORD_SIZE-1:0] inst_now;
		reg [OPCODE_WIDTH-1:0] op;
		if (RB_valid[inc(tail)]) begin
			inst_now = RB_inst[inc(tail)];
			op = inst_now[WORD_SIZE-1:WORD_SIZE-OPCODE_WIDTH];
			//ignore brach so far
			if (op == INST_BGE) begin
				we_status = 1'b0;
			end
			else begin:issueIfCan
				reg free;
				reg[FU_INDEX-1:0] 	i;
				reg[FU_INDEX-1:0]	fuend;
				getFuStartAndNum(op, i, fuend);
				free = 1'b0;
				for (i = i; i < fuend && !free; i = i+1) begin
					if (!busy[i]) begin
						tail = inc(tail);
						CDB_inst_inst = inst_now;
						CDB_inst_fu = i;
						CDB_inst_RBindex = tail;
						free = 1'b1;
					end
					else begin 	end
				end
				if (!free) begin
					CDB_inst_fu = NO_FU;
					we_status = 1'b0;
				end
				else begin: updateRegStatus
					//$display($realtime, "RB_inst_inst:%b, fu = %d", CDB_inst_inst, CDB_inst_fu);
					#0.5
					if (op != INST_SW && op != INST_SWRR) begin	//no jump will be put into RB
						Rdest_status = getRdest(inst_now);
						we_status = 1'b1;
						RB_index_status = tail;
						//$display("Rdest = %d, RB_index_status = %d", Rdest_status, RB_index_status);
					end	
					else begin 
						we_status = 1'b0;
					end
				end
			end
		end
		else begin
			CDB_inst_fu = NO_FU;			// change RS!!
		end
	end

	function[REG_INDEX-1:0]	getRdest;
		input [WORD_SIZE-1:0]	inst;
		reg [OPCODE_WIDTH-1:0]	op;
	begin
		op = inst[WORD_SIZE-1:WORD_SIZE-OPCODE_WIDTH];
		if (op != INST_LI) 
			getRdest = inst[RD_START: RD_START-REG_INDEX+1];
		else 
			getRdest = inst[RS_START: RS_START-REG_INDEX+1];
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
			default: begin
				$display($realtime, "RB is issuing a jump");
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
endmodule