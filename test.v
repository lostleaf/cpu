`include "timescale.v"
module test;
	reg[31:0] flag = 'b0;
	initial begin		
		$display("%b", flag | ('b1<<3));
	end
endmodule