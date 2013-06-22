module CDB_data_controller(CDB_data_data, CDB_data_valid, CDB_data_addr,
	data_bus, valid_bus, addr_bus, RB_index_bus, reset, clk);
	
	`include "parameters.v"
	output	reg [WORD_SIZE*RB_SIZE-1:0]	CDB_data_data;
	output 	reg [RB_SIZE-1:0]			CDB_data_valid;
	output	reg [RB_SIZE*WORD_SIZE-1:0]	CDB_data_addr;

	input	wire[WORD_SIZE*FU_NUM-1:0]	data_bus;
	input	wire[FU_NUM-1:0]			valid_bus;
	input	wire[STORER_NUM*WORD_SIZE-1:0]	addr_bus;
	input	wire[FU_NUM*RB_INDEX-1:0]	RB_index_bus;

	input	wire reset, clk;

	always @(negedge clk or posedge reset) begin
		if (reset) begin
			CDB_data_valid <= 'b0;
			CDB_data_data <= 'b0;		
			CDB_data_addr <= 'b0;	
		end
		else begin:setCDB
			reg[WORD_SIZE*RB_SIZE-1:0]	data, data_each;
			reg[RB_SIZE-1:0]			valid, valid_each;
			reg[RB_INDEX-1:0]			index;	
			reg[WORD_SIZE-1:0]			i;
			reg[WORD_SIZE-1:0]			mask;
			reg							valid_mask;

			data = 'b0;
			valid = 'b0;
			mask = ~'b0;
			valid_mask = 1'b1;

			for (i = 0; i < FU_NUM; i = i+1) begin
				index = readIndex(RB_index_bus, i);
				if (index == NULL || !readValid(valid_bus, i))	begin 
					//$display("data = %h", (((CDB_data_data>>(WORD_SIZE*i))[WORD_SIZE-1:0])<<(WORD_SIZE*i)));
					data = data | (CDB_data_data & (mask<<(WORD_SIZE*i)));
					valid = valid | (CDB_data_valid & (valid_mask<<i));
				end
				else begin
					data_each = readData(data_bus, i);
					$display($realtime, ":controler: %g %g",i,data_each);
					data_each = data_each << (index*WORD_SIZE);
					data = data | data_each;
					valid_each = readValid(valid_bus, i);
					valid_each = valid_each << index;
					valid = valid | valid_each;
				end
			end

			CDB_data_data = data;
			CDB_data_valid = valid;

			data = 'b0;
			for (i = 0; i < STORER_NUM; i = i+1) begin
				index = readIndex(RB_index_bus, FU_NUM-STORER_NUM+i);
				if (index == NULL)	begin 
					data = data | (CDB_data_addr & (mask<<(WORD_SIZE*i)));
				end
				else begin
					data_each = readData(addr_bus, i);
					data_each = data_each << (index*WORD_SIZE);
					data = data | data_each;
				end
			end
			CDB_data_addr = data;
		end
	end

	function[RB_INDEX-1:0] readIndex;
		input[FU_NUM*RB_INDEX-1:0]	RB_index_bus;
		input[WORD_SIZE-1:0]		fuindex;
	begin
		readIndex = RB_index_bus>>(fuindex*RB_INDEX);
	end
	endfunction

	function[WORD_SIZE-1:0] readData;
		input[WORD_SIZE*FU_NUM-1:0]	data_bus;
		input[WORD_SIZE-1:0]		fuindex;
	begin
		readData = data_bus>>(fuindex*WORD_SIZE);
	end
	endfunction

	function readValid;
		input[FU_NUM-1:0]	valid_bus;
		input[WORD_SIZE-1:0]	fuindex;
	begin
		readValid = valid_bus>>fuindex;		
	end
	endfunction
endmodule