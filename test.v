`include "timescale.v"
module test;
	`include "parameters.v"
	reg[WORD_SIZE-1:0] mask = ~'b0;
	reg[WORD_SIZE*2-1:0] CDB_data_data;
	reg[WORD_SIZE*2-1:0] data = 'b0;

	initial begin
		CDB_data_data = (32'd4<<32)|32'd5;
		data = data | (CDB_data_data & (mask<<(WORD_SIZE)));		
		$display("%h", data);
	end
endmodule