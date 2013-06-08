`include "timescale.v"
module reg_status_tb;
	`include "parameters.v"

	reg [REG_INDEX-1:0] get_num1 = 5'b0, get_num2 = 5'd1, get_num3 = 5'd2, set_num = 5'd0;
	wire [WORD_SIZE-1:0] v1, v2, v3;
	wire [FU_INDEX-1:0]q1, q2, q3;

	reg [WORD_SIZE-1:0] wd = 0;
	reg [REG_INDEX-1:0] ws, ws_rs;
	reg [FU_INDEX-1:0]	wd_rs;
	reg we = 1'b1, we_rs = 1, reset = 1'b0;
	reg clk;

	reg_status rs(get_num1, get_num2, get_num3, v1, v2, v3, q1, q2, q3,
	 	ws, wd, we, ws_rs, wd_rs, we_rs, reset, clk);	// rs stands for reg_status

	always begin
		#0.5 clk = 0;
		#0.5 clk = 1;
	end

	always begin
		$monitor("%g: r1 = %g, r2 = %g, r3 = %g, v1 = %g, v2 = %g, v3 = %g, s1 = %g, s2= %g, s3 = %g",
			 $realtime, get_num1, get_num2, get_num3, v1, v2, v3, q1, q2, q3);
		ws = 2;
		wd = 2;
		ws_rs = 3;
		wd_rs = 3;
		#0.1 repeat(10) begin
			#1   get_num1 = get_num1+1;
				 get_num2 = get_num2+1;
				 get_num3 = get_num3+1;
				 ws  = ws+1;
				 ws_rs = ws_rs+1;
				 wd =  wd+1;
				 wd_rs = wd_rs+1;
				 //$display("getnum1= %g, getnum2 = %g, getnum3 = %g", get_num1, get_num2, get_num3);

		end
		$finish;
	end
endmodule