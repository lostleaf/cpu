`include "timescale.v"

`include "ALU_RS.v"
`include "reg_status.v"

module CPU;
	`include "parameters.v"

	//CDB_data RB_INDEX lanes
	wire[WORD_SIZE*RB_SIZE-1:0]	CDB_data_data;
	wire[RB_SIZE-1:0]			CDB_data_valid;
	//CDB_inst 1 lane
	wire[FU_INDEX-1:0]	CDB_inst_fu;
	wire[WORD_SIZE-1:0]	CDB_inst_inst;
	wire[RB_INDEX-1:0]	CDB_inst_RBindex;

	wire[WORD_SIZE-1:0] 	vi, vj, vk;
	wire[RB_INDEX-1:0]	qi, qj, qk;
	wire[REG_INDEX-1:0]	numi, numj, numk;

	wire[FU_NUM-1:0]	busy;

	reg clk, reset;

	// not done
	// for reg
	reg we_reg, we_status;
	reg[REG_INDEX-1:0]	ws_reg, ws_status;
	reg[WORD_SIZE-1:0]	wd_reg;
	reg[RB_INDEX-1:0]	wd_status;
	// for CDB_inst
	reg[FU_INDEX-1:0]	fu;
	wire[WORD_SIZE-1:0]	inst;
	reg[RB_INDEX-1:0]	RBindex;

	//for inst
	reg[OPCODE_WIDTH-1:0] 	op; 
	reg[REG_INDEX-1:0]		rs, rt, rd;

	reg_status status(numi, numj, numk, vi, vj, vk, qi, qj, qk,
	 	ws_reg, wd_reg, we_reg, ws_status, wd_status, we_status, reset, clk);

	ALU_RS alu_rs[ADDER_NUM-1:0](CDB_inst_fu, CDB_inst_RBindex, CDB_inst_inst, vj, vk, qj, qk, 					
			numj, numk, busy, CDB_data_data, CDB_data_valid, CDB_data_data, CDB_data_valid, reset, clk);


	always begin
		#0.5 clk = 0;
		#0.5 clk = 1;
	end

	// not done
	assign CDB_inst_fu = fu;
	assign CDB_inst_inst = inst;
	assign CDB_inst_RBindex = RBindex;
	assign inst = {op, rs, rd, rt, 12'b0};

	// for test
	always begin:test
		reg[WORD_SIZE-1:0] i;
		
		$monitor("%g: inst: %h_%h_%h_%h, j <%g, %g>, k <%g, %g>, CDB: 0 <%g, %g>, 1 <%g, %g>, 2 <%g, %g>, busy: 0:%g, 1:%g, 2:%g",
			$realtime, op, rs, rd, rt, vj, qj, vk, qk, CDB_data_data[WORD_SIZE-1:0], CDB_data_valid[0:0],
			CDB_data_data[2*WORD_SIZE-1:WORD_SIZE], CDB_data_valid[1:1], 
			CDB_data_data[3*WORD_SIZE-1:2*WORD_SIZE], CDB_data_valid[2:2],
			busy[0], busy[1], busy[2]);
		
		op = INST_ADD;
		reset = 1;
		#1 reset = 0;
		//init reg
		for (i = 0; i < REG_FILE_SIZE; i = i+1) begin
			#1 	 we_reg = 1'b1;
				 ws_reg = i;
				 wd_reg = i;
				 we_status = 1;
				 wd_status = i%FU_NUM;
				 ws_reg = i;
				 //$display("CPU %g: write <%g, %g, %g>", $realtime, i, wd_status);
		end

		rs = 0;
		rd = 1;
		rt = 2;
		for (i = 5'b0; i < ADDER_NUM; i = i+1) begin
			#1 fu = i;
				RBindex = i;
				rs = rs+1;
				rt = rt+1;
		end

		#20 $finish;
	end

endmodule