`timescale 1ns/100ps

module reg_file_tb;

	`include "parameters.v"

	reg[REG_INDEX-1:0] get_num1 = 5'b0, get_num2 = 5'b1, get_num3 = 5'd2, set_num = 5'd0;
	wire[WORD_SIZE-1:0] out1, out2, out3;

	reg [WORD_SIZE-1:0] set_val = 0;
	reg set_enable = 1'b1, reset_enable = 1'b0;
	reg clk;

	reg_file register(get_num1, get_num2, get_num3, out1, out2, out3, set_num, set_val, set_enable, reset_enable, clk);

	always begin
		#0.5 clk = 0;
		#0.5 clk = 1;
	end

	always begin
		$monitor("getnum1 = %g, getnum2 = %g, getnum3 = %g, realtime = %g, out1 = %g, out2 = %g, out3 = %g", 
			get_num1, get_num2, get_num3, $realtime, out1, out2, out3);
		set_val = 0;
		set_num = 2;
		#0.1 repeat(10) begin
			#1   get_num1 = get_num1+1;
				 get_num2 = get_num2+1;
				 get_num3 = get_num3+1;
				 set_num  = set_num+1;
				 set_val =  set_val+1;
				 //$display("getnum1= %g, getnum2 = %g, getnum3 = %g", get_num1, get_num2, get_num3);
		end
		$finish;
	end
endmodule