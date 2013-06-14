`include "timescale.v"

`include "ALU_RS.v"
`include "store_RS.v"
`include "def_param.v"
`include "reg_status.v"
`include "CDB_data_controller.v"
`include "reorder_buffer.v"
module CPU;
	`include "parameters.v"

	//CDB_data RB_INDEX lanes
	wire[WORD_SIZE*RB_SIZE-1:0]	CDB_data_data = 'bz;
	wire[RB_SIZE-1:0]			CDB_data_valid = 'bz;
	wire[WORD_SIZE*RB_SIZE-1:0]	CDB_data_addr = 'bz;

	wire[FU_NUM*WORD_SIZE-1:0]	FU_data_bus;
	wire[FU_NUM-1:0]			FU_valid_bus;
	wire[STORER_NUM*WORD_SIZE-1:0]	FU_addr_bus;
	wire[FU_NUM*RB_INDEX-1:0]	FU_RB_index_bus;

	//CDB_inst 1 lane
	wire[FU_INDEX-1:0]	CDB_inst_fu = 'bz;
	wire[WORD_SIZE-1:0]	CDB_inst_inst = 'bz;
	wire[RB_INDEX-1:0]	CDB_inst_RBindex = 'bz;

	wire[WORD_SIZE-1:0] vi, vj, vk;
	wire[RB_INDEX-1:0]	qi, qj, qk;
	wire[REG_INDEX-1:0]	numi = 'bz, numj = 'bz, numk = 'bz;

	wire[FU_NUM-1:0]	busy;

	// from RB to FU
	reg [FU_NUM-1:0]	write;

	reg clk, reset;

	// not done
	// for reg
	wire we_reg, we_status;
	wire[REG_INDEX-1:0]	ws_reg, ws_status;
	wire[WORD_SIZE-1:0]	wd_reg;
	wire[RB_INDEX-1:0]	wd_status;
	// for CDB_inst
	reg[FU_INDEX-1:0]	fu;
	wire[WORD_SIZE-1:0]	inst;
	reg[RB_INDEX-1:0]	RBindex;

	//for inst
	reg[OPCODE_WIDTH-1:0] 	op; 
	reg[REG_INDEX-1:0]		rs, rt, rd;

	// within RB
	reg[FU_INDEX-1:0]	RB_wt_by_FU[RB_SIZE-1:0];		//RB entry written by FU

	// from reorder buffer to datacache
	wire we_dcache;
	wire[WORD_SIZE-1:0]	wd_dcache;
	wire[WORD_SIZE-1:0]	ws_dcache;

	reg_status status(.get_num1(numi), .get_num2(numj), .get_num3(numk), .value1(vi), .value2(vj), .value3(vk), 
		.status1(qi), .status2(qj), .status3(qk),
		.write_reg_src(ws_reg), .write_reg_data(wd_reg), .write_reg_enable(we_reg), 
		.write_rs_src(ws_status), .write_rs_status(wd_status), .write_rs_enable(we_status), .reset(reset), .clk(clk));
	
	ALU_RS alu_rs[FU_NUM-STORER_NUM-1:0](.fu(CDB_inst_fu), .RB_index(CDB_inst_RBindex), .inst(CDB_inst_inst), .vj(vj), .vk(vk), 
		.qj(qj), .qk(qk), .reg_numj(numj), .reg_numk(numk), .busy_out(busy), 
		.CDB_data_data(CDB_data_data), .CDB_data_valid(CDB_data_valid), 
		.data_bus(FU_data_bus), .valid_bus(FU_valid_bus), .RB_index_bus(FU_RB_index_bus),.reset(reset), .clk(clk));

	store_RS store_rs[STORER_NUM-1:0](.fu(CDB_inst_fu), .RB_index(CDB_inst_RBindex), .inst(CDB_inst_inst), 
		.vi(vi), .vj(vj), .vk(vk), 
		.qi(qi), .qj(qj), .qk(qk), .reg_numi(numi), .reg_numj(numj), .reg_numk(numk), .busy_out(busy), 
		.CDB_data_data(CDB_data_data), .CDB_data_valid(CDB_data_valid), 
		.data_bus(FU_data_bus), .valid_bus(FU_valid_bus), .addr_bus(FU_addr_bus),
		.RB_index_bus(FU_RB_index_bus),.reset(reset), .clk(clk));

	CDB_data_controller data_ctrl(.CDB_data_data(CDB_data_data), .CDB_data_valid(CDB_data_valid), .CDB_data_addr(CDB_data_addr),
		.data_bus(FU_data_bus), .valid_bus(FU_valid_bus), .addr_bus(FU_addr_bus),
		.RB_index_bus(FU_RB_index_bus), .reset(reset), .clk(clk));

	reorder_buffer RB(.CDB_data_data(CDB_data_data), .CDB_data_valid(CDB_data_valid), .CDB_data_addr(CDB_data_addr), 
		.busy(busy), .we_reg(we_reg), .wd_reg(wd_reg), .ws_reg(ws_reg), 
		.we_mem(we_dcache), .wd_mem(wd_dcache), .ws_mem(ws_dcache), .numj(numj), .numk(numk),
		.vj(vj), .vk(vk), .qj(qj), .qk(qk), 
		.CDB_inst_fu(CDB_inst_fu), .CDB_inst_inst(CDB_inst_inst), .CDB_inst_RBindex(CDB_inst_RBindex), 
		.Rdest_status(ws_status), .RB_index_status(wd_status), .we_status(we_status),
		.reset(reset), .clk(clk));

	always begin
		#0.5 clk = 0;
		#0.5 clk = 1;
	end


	// for test
	always begin:test

		reg[WORD_SIZE-1:0] i;
		$dumpfile("CPU2.vcd");
		$dumpvars;

		$monitor("%g: CDB: 1:<v:%b, d:%g, a:%g>, 2:<v:%b, d:%g, a:%g>, busy: 0:%g, 1:%g",
			$realtime,
			CDB_data_valid[1], CDB_data_data[2*WORD_SIZE-1:WORD_SIZE], CDB_data_addr[2*WORD_SIZE-1:WORD_SIZE], 
			CDB_data_valid[2], CDB_data_data[3*WORD_SIZE-1:2*WORD_SIZE], CDB_data_addr[3*WORD_SIZE-1:2*WORD_SIZE],
			busy[0], busy[1]);

		/*$monitor($realtime, "inst:%b, fu:%d, RB_index = %d", 
			CDB_inst_inst, CDB_inst_fu, CDB_inst_RBindex);*/
		
		reset = 1;
		#1 reset = 0;
		//init reg
		/*for (i = 0; i < REG_FILE_SIZE; i = i+1) begin
			#1 	 we_reg = 1'b1;
				 ws_reg = i;
				 wd_reg = i;
		end

		rs = 0;
		rd = 1;
		rt = 2;
		// test of ALU_RS
		#1for (i = 5'b0; i < ADDER_NUM; i = i+1) begin
			    fu = i;
				RBindex = i;
				rs = rs+1;
				rt = rt+1;

			#1 begin end
		end*/

		/*#1for (i = 5'b0; i < STORER_NUM; i = i+1) begin
			    fu = FU_NUM-STORER_NUM+i;
				RBindex = i;
				rs = rs+1;
				rt = rt+1;
				rd = rd+1;

			#1 begin end
		end
		*/

		#110 $finish;
	end

	task setWriteBy;
		inout [FU_INDEX-1:0]	writeFU;
		inout [FU_NUM-1:0]		write;
		input [FU_INDEX-1:0]	fu;
		begin
			if (writeFU != NULL) begin
				setWrite(write, writeFU, 1'b0);
			end else begin end
			setWrite(write, fu, 1'b1);
			writeFU = fu;
		end
	endtask

	task setWrite;
		inout [FU_NUM-1:0]		write;
		input [FU_INDEX-1:0]	fu;
		input data;

		reg[FU_NUM-1:0] longData, mask;
		begin
			mask = ~(1'b1<<fu);
			longData = data<<fu;
			write =  (write&mask)|longData;
		end
	endtask

endmodule