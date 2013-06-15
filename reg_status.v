`include "timescale.v"
`include "reg_file_RX.v"
module reg_status(get_num1, get_num2, get_num3, value1, value2, value3, status1, status2, status3,
	 				write_reg_src, 	write_reg_data,		write_reg_enable, 
	 				write_rs_src1, 	write_rs_status1,	write_rs_enable1,
	 				write_rs_src2, 	write_rs_status2,	write_rs_enable2,
	 				reset, clk);	// rs stands for reg_status
	
	`include "parameters.v"

	input 	wire[REG_INDEX-1:0] get_num1, get_num2, get_num3;
	output 	wire[WORD_SIZE-1:0] value1, value2, value3;
	output 	wire[FU_INDEX-1:0] status1, status2, status3;

	input	wire[REG_INDEX-1:0] write_reg_src;
	input	wire[WORD_SIZE-1:0] write_reg_data;
	input	wire write_reg_enable;

	input	wire[REG_INDEX-1:0] write_rs_src1, write_rs_src2;
	input	wire[FU_INDEX-1:0]	write_rs_status1, write_rs_status2;
	input	wire 				write_rs_enable1, write_rs_enable2;

	input	wire reset, clk;

	reg_file registers(get_num1, get_num2, get_num3, value1, value2, value3,
                write_reg_src, write_reg_data, write_reg_enable, reset, clk);
	reg[FU_INDEX-1:0]	statuses[REG_FILE_SIZE-1:0];

	assign status1 = statuses[get_num1];
	assign status2 = statuses[get_num2];
	assign status3 = statuses[get_num3];

	always @(negedge clk or posedge reset) begin:rs
		reg[WORD_SIZE-1:0] i;
		if (reset) begin
			for (i = 0; i < REG_FILE_SIZE; i = i+1) begin
				statuses[i] = READY;
				//$display("reg_status[%d] = %d", i, statuses[i]);
			end
		end 
		else begin
			if(write_rs_enable1) begin
					statuses[write_rs_src1] <= write_rs_status1;
					$display($realtime, ": reg_status[%d] = %d", write_rs_src1, write_rs_status1);
			end
			if(write_rs_enable2) begin
					statuses[write_rs_src2] <= write_rs_status2;
					$display($realtime, ": reg_status[%d] = %d", write_rs_src2, write_rs_status2);
			end
		end
	end
endmodule
