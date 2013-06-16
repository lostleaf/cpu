module store_RS(fu, RB_index, inst,vi, vj, vk, qi, qj, qk, 					
	reg_numi, reg_numj, reg_numk, busy_out, CDB_data_data, CDB_data_valid,
	data_bus, valid_bus, addr_bus, RB_index_bus, reset_bus, clk);

	`include "parameters.v"
	parameter fuindex = 0, StorerIndex = 0;		
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
	output	wire[STORER_NUM*WORD_SIZE-1:0]	addr_bus;
	output	wire[FU_NUM*RB_INDEX-1:0]		RB_index_bus;
	

	input	wire[WORD_SIZE-1:0]	vi, vj, vk;
	input	wire[RB_INDEX-1:0]	qi, qj, qk; 
	output	reg [REG_INDEX-1:0]	reg_numi = 'bz, reg_numj = 'bz, reg_numk = 'bz;
	// from RB

	input	wire				clk;
	output	wire[FU_NUM-1:0]	busy_out;
	input	wire[FU_NUM-1:0]	reset_bus;
	
	reg[WORD_SIZE-1:0]	Vi, Vj, Vk;
	reg[RB_INDEX-1:0]	Qi, Qj, Qk;
	reg[RB_INDEX-1:0]	dest;	
	reg[OPCODE_WIDTH-1:0]	op;
	reg busy;
	reg[WORD_SIZE-1:0]	result = 'b0, data = 'b0;
	reg valid;
	wire reset;
	
	assign busy_out[fuindex:fuindex] = busy;
	assign addr_bus[(StorerIndex+1)*WORD_SIZE-1: StorerIndex*WORD_SIZE] = result;
	assign valid_bus[fuindex: fuindex] = valid;
	assign data_bus[(fuindex+1)*WORD_SIZE-1:fuindex*WORD_SIZE] = data;
	assign RB_index_bus[(fuindex+1)*RB_INDEX-1:fuindex*RB_INDEX] = dest;
	assign reset = reset_bus[fuindex];

	always @(posedge clk or posedge reset) begin
		if (reset) begin
			busy <= 1'b0;
			dest <= NULL;
			valid <= 1'b0;
			result <= 'b0;
		end else if (!busy) begin: checkIssue
				#0.1;
				if (fu == fuindex) begin
						$display($realtime, ": %d receive inst:%b", fuindex, inst);
						busy  <= 1'b1;
						dest  <= RB_index;
						op = inst[WORD_SIZE-1:WORD_SIZE-OPCODE_WIDTH];

						reg_numi = inst[RD_START: RD_START-REG_INDEX+1];
						reg_numj = inst[RS_START: RS_START-REG_INDEX+1];
						getData(vi, qi, Vi, Qi, CDB_data_data, CDB_data_valid);
						getData(vj, qj, Vj, Qj, CDB_data_data, CDB_data_valid);
						if (op === INST_SWRR) begin
							reg_numk = inst[RT_START: RT_START-REG_INDEX+1];
							getData(vk, qk, Vk, Qk, CDB_data_data, CDB_data_valid);
						end
						else begin
							Vk = inst[IMM_START:0];
							Qk = READY;
						end
						
						#0.1 reg_numi = 'bz;
						reg_numj = 'bz;
						reg_numk = 'bz;


						valid <= 1'b0;
				end else begin
				end
			end else begin	end
	end
	

	

	always @(posedge clk) 
		if (busy) begin: execute
				reg ok;
				ok = 1'b1;
				checkAndGetData(Qj, Vj, CDB_data_data, CDB_data_valid, ok);
				checkAndGetData(Qk, Vk, CDB_data_data, CDB_data_valid, ok);
				checkAndGetData(Qi, Vi, CDB_data_data, CDB_data_valid, ok);
				if (ok) begin
					#0.1 result = Vj-Vk;
					data = Vi;
					valid = 1'b1;
					busy = 1'b0;
					#0.5 dest = NULL;
					#0.8 valid = 0'b0;
					
				end
				else begin 
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
		end
	endtask

	function[WORD_SIZE-1:0] readDataBus;
		input[WORD_SIZE*RB_SIZE-1:0] CDB_data_data;
		input[RB_INDEX-1:0]			 index;		  
		begin
			readDataBus = CDB_data_data>>(index*WORD_SIZE);
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
			//$display("<Q:%g, V:%g>", Q, V);
			if (Q === READY) begin end
			else begin 
				valid = readValidBus(validBus, Q);
				if (valid) begin
					Q = READY;
					V = readDataBus(dataBus, Q);
				end else ok = 1'b0;
			end
		end
	endtask

endmodule
