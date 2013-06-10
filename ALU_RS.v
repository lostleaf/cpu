module ALU_RS(fu, RB_index, inst, vj, vk, qj, qk, 					
	reg_numj, reg_numk, busy_out, CDB_data_data, CDB_data_valid, CDB_data_data_out, CDB_data_valid_out, reset, clk);

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
	output	reg [WORD_SIZE*RB_SIZE-1:0]	CDB_data_data_out;
	output 	reg [RB_SIZE-1:0]			CDB_data_valid_out;

	input	wire[WORD_SIZE-1:0]	vj, vk;
	input	wire[RB_INDEX-1:0]	qj, qk;
	output	reg [REG_INDEX-1:0]	reg_numj, reg_numk;
	
	input	wire				reset, clk;
	output	wire[FU_NUM-1:0]	busy_out;
	
	reg[WORD_SIZE-1:0]	Vj, Vk;
	reg[RB_INDEX-1:0]	Qj, Qk;
	reg[RB_INDEX-1:0]	dest;	
	reg[OPCODE_WIDTH-1:0]	op;
	reg 				busy;
	
	assign busy_out[fuindex:fuindex] = busy;


	always @(posedge clk or reset) begin
		if (reset)
			busy <= 1'b0;
		else if (!busy) begin: checkIssue
				#0.1 if (fu == fuindex) begin
					busy  <= 1'b1;
					dest  <= RB_index;
					op <= inst[WORD_SIZE-1:WORD_SIZE-OPCODE_WIDTH];

					reg_numj = inst[RS_START: RS_START-REG_INDEX+1];
					reg_numk = inst[RT_START: RT_START-REG_INDEX+1];
					
					getData(vj, qj, Vj, Qj,CDB_data_data, CDB_data_valid);
					getData(vk, qk, Vk, Qk, CDB_data_data, CDB_data_valid);
					
					writeValid(CDB_data_valid_out,RB_index, 1'b0);
				end else /*do nothing*/ begin end
			end else begin: execute
				reg ok;
				ok = 1'b1;
				checkAndGetData(Qj, Vj, CDB_data_data, CDB_data_valid, ok);
				checkAndGetData(Qk, Vk, CDB_data_data, CDB_data_valid, ok);
				if (ok) begin
					case (op)
						INST_SUB: begin
							#0.5 writeValid(CDB_data_valid_out, dest, 1'b1); // still need the stall?
								 writeData(CDB_data_data_out, dest, Vj-Vk);
						end
						INST_ADD: begin
							#0.5 writeValid(CDB_data_valid_out, dest, 1'b1); // still need the stall?
								 writeData(CDB_data_data_out, dest, Vj+Vk);
						end
						INST_MUL: begin 
							#(MUL_STALL+0.5) writeValid(CDB_data_valid_out, dest, 1'b1); // still need the stall?
								 writeData(CDB_data_data_out, dest, Vj*Vk);
						end
					endcase
				end else /*do nothing*/ begin end
			end
	end

	task writeValid;
		output[RB_SIZE-1:0]  validBus;
		input [RB_INDEX-1:0] dest;	
		input				 data;

		reg[RB_INDEX-1:0]			mask, longData;
		begin
			mask = ~(1'b1 << dest);
			longData = data<<dest;
			validBus = longData|(mask&validBus);
		end
	endtask

	task writeData;
		output[WORD_SIZE*RB_SIZE-1:0] dataBus;
		input [RB_INDEX-1:0] 	dest;
		input [WORD_SIZE-1:0]	data;

		reg[WORD_SIZE*RB_SIZE-1:0]	mask, longData;
		reg[WORD_SIZE-1:0]			FF;
		begin
			FF = ~0;
			mask = ~(FF<<(dest*WORD_SIZE));
			longData = data<<(dest*WORD_SIZE);
			dataBus = (dataBus&mask)|longData;
		end
	endtask

	task getData;	//(v, q, CDB_data_data, CDB_data_valid, V, Q)
		input[WORD_SIZE-1:0] v;
		input[RB_INDEX-1:0]	 q;
		output	reg [WORD_SIZE-1:0] V;
		output	reg [RB_INDEX-1:0]	Q;
		input[WORD_SIZE*RB_SIZE-1:0] 	CDB_data_data;
		input[RB_SIZE-1:0]				CDB_data_valid;
		begin
			if (q == READY) begin
				V <= v;
				Q <= READY;
			end	else if (readValidBus(CDB_data_valid, q)) begin
						V <= readDataBus(CDB_data_data, q);
						Q <= READY;
					end
			else Q <= q;
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

	task checkAndGetData;
		input [RB_INDEX-1:0]  Q;
		output[WORD_SIZE-1:0] V;		// ??
		input [WORD_SIZE*RB_SIZE-1:0] CDB_data_data;
		input [RB_SIZE-1:0] 		  CDB_data_valid;
		output ok;
		begin
			if (readValidBus(CDB_data_valid, Q)) begin
				V = readDataBus(CDB_data_data, Q);
			end else ok = 1'b0;
		end
	endtask

endmodule