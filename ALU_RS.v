module ALU_RS(fu, RB_index, inst, vj, vk, qj, qk, 					
	reg_numj, reg_numk, busy_out, CDB_data_data, CDB_data_valid, 
	data_bus, valid_bus, RB_index_bus, reset_bus, clk);

	`include "parameters.v"
	parameter fuindex = 0;		
	// it's op depends on the design outside on which fuindex corresponds to which op, 
	//and the info is in the <"fu", RB_index, "inst">

	// from CDB_inst
	input	wire[FU_INDEX-1:0] 	fu;
	input	wire[RB_INDEX-1:0] 	RB_index;
	input	wire[WORD_SIZE-1:0] inst;
	//from CDB_data
	input	wire[WORD_SIZE*RB_SIZE-1:0]	CDB_data_data;
	input 	wire[RB_SIZE-1:0]			CDB_data_valid;
	output	wire[FU_NUM*WORD_SIZE-1:0]	data_bus;
	output	wire[FU_NUM-1:0]			valid_bus;
	output	wire[FU_NUM*RB_INDEX-1:0]		RB_index_bus;
	

	input	wire[WORD_SIZE-1:0]	vj, vk;
	input	wire[RB_INDEX-1:0]	qj, qk; 
	output	reg [REG_INDEX-1:0]	reg_numj = 'bz, reg_numk = 'bz;
	// from RB

	input	wire				clk;
	output	wire[FU_NUM-1:0]	busy_out;
	input	wire[FU_NUM-1:0]	reset_bus;
	
	reg[WORD_SIZE-1:0]	Vj, Vk;
	reg[RB_INDEX-1:0]	Qj, Qk;
	reg[RB_INDEX-1:0]	dest;	
	reg[OPCODE_WIDTH-1:0]	op;
	reg busy;
	reg[WORD_SIZE-1:0]	result;
	reg valid;
	wire reset;
	
	assign busy_out[fuindex:fuindex] = busy;
	assign data_bus[(fuindex+1)*WORD_SIZE-1: fuindex*WORD_SIZE] = result;
	assign valid_bus[fuindex: fuindex] = valid;
	assign RB_index_bus[(fuindex+1)*RB_INDEX-1:fuindex*RB_INDEX] = dest;
	assign reset = reset_bus[fuindex];

	always @(posedge clk or posedge reset) begin
		// $display($realtime, ": %d busy:%d, sees CDB_inst: %d, %b", fuindex, busy, fu, inst);
		if (reset) begin
			busy <= 1'b0;
			dest <= NULL;
			valid <= 1'b0;
			result <= 'b0;
		end else if (!busy) begin: checkIssue
				#0.1 if (fu == fuindex) begin
						$display($realtime, " %m: %d receive inst:%b", fuindex, inst);
						op = inst[WORD_SIZE-1:WORD_SIZE-OPCODE_WIDTH];
						busy  <= 1'b1;
						dest  <= RB_index;
						reg_numj = inst[RS_START: RS_START-REG_INDEX+1];
						getData(vj, qj, Vj, Qj,CDB_data_data, CDB_data_valid);
						//$display("qj = %d", qj);
						if (op == INST_ADD || op == INST_SUB || op == INST_MUL) begin
							reg_numk = inst[RT_START: RT_START-REG_INDEX+1];
							getData(vk, qk, Vk, Qk, CDB_data_data, CDB_data_valid);
						end
						else begin 
							Vk = inst[IMM_START:0];
							Qk = READY;
						end
						#0.1 reg_numj = 'bz;
							reg_numk = 'bz;

							valid <= 1'b0;
					end else begin
					end
			end else begin end
	end
	
	always @(posedge clk) 
		if (busy) begin: execute
			reg ok;
			//$display($realtime, ": %d about to execute", fuindex);

			ok = 1'b1;
			checkAndGetData(Qj, Vj, CDB_data_data, CDB_data_valid, ok);
			checkAndGetData(Qk, Vk, CDB_data_data, CDB_data_valid, ok);
			//$display("op = %h, ok = %d, Qj = %d, Vj = %d, Qk = %d, Vk = %d",op, ok, Qj, Vj, Qk, Vk);
			if (ok) begin
				case (op)
					INST_SUB: begin
						#0.1 result = Vj-Vk;
					end
					INST_SUBI: begin
						#0.1 result = Vj-Vk;
					end
					INST_ADD: begin
						#0.1 result = Vj+Vk;
					end
					INST_ADDI: begin
						#0.1 result = Vj+Vk;
					end
					INST_MUL: begin 
						#(MUL_STALL+0.1) result = Vj*Vk;
					end
					INST_MULI: begin
						#(MUL_STALL+0.1) result = Vj*Vk;
					end
					default: begin	end
				endcase
					
				valid = 1'b1;
				//$display($realtime, ": %g result = %d, dest = %d", fuindex, result, dest);
				busy = 1'b0;
				#0.5 dest = NULL;
				#0.8 valid = 0'b0;
			end
		end
		else begin end


	task getData;	//(v, q, CDB_data_data, CDB_data_valid, V, Q)
		input[WORD_SIZE-1:0] v;
		input[RB_INDEX-1:0]	 q;
		output	reg [WORD_SIZE-1:0] V;
		output	reg [RB_INDEX-1:0]	Q;
		input[WORD_SIZE*RB_SIZE-1:0] 	CDB_data_data;
		input[RB_SIZE-1:0]				CDB_data_valid;
		begin
			if (q === READY) begin
				V = v;
				Q = READY;
			end	else if (readValidBus(CDB_data_valid, q)) begin
						V = readDataBus(CDB_data_data, q);
						Q = READY;
					end
			else Q = q;
			//$display($realtime, "valid of %d: %d", q, readValidBus(CDB_data_valid, q));
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

	task checkAndGetData;	//(Qj, Vj, CDB_data_data, CDB_data_valid, ok);
		inout [RB_INDEX-1:0]	Q;					//!!inout, not ouput
		inout [WORD_SIZE-1:0]	V;
		input [WORD_SIZE*RB_SIZE-1:0]	dataBus;
		input [RB_SIZE-1:0]				validBus;
		inout ok;
		reg valid;
		begin
			if (Q === READY) begin end
			else begin 
				valid = readValidBus(validBus, Q);
				if (valid) begin
					V = readDataBus(dataBus, Q);
					Q = READY;
				end else ok = 1'b0;
			end
		end
	endtask

endmodule
