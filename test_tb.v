module test_tb;

	`include "parameters.v"
	wire line;
	test t(line);
	always begin
		$monitor("%g", line);
		#10 $finish;
	end
endmodule