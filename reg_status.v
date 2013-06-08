`include "timescale.v"
module reg_status(get_num1, get_num2, get_num3, value1, value2, value3, status1, status2, status3,
	 write_reg_src, write_reg_data, write_reg_enable, write_rs_src, write_rs_status, write_rs_enable, reset, clk);	// rs stands for reg_status
	
	`include "parameters.v"

	input 	wire[REG_INDEX-1:0] get_num1, get_num2, get_num3;
	output 	wire[WORD_SIZE-1:0] value1, value2, value3;
	output 	wire[FU_INDEX-1:0] status1, status2, status3;

	input	wire[REG_INDEX-1:0] write_reg_src;
	input	wire[WORD_SIZE-1:0] write_reg_data;
	input	wire write_reg_enable;

	input	wire[REG_INDEX-1:0] write_rs_src;
	input	wire[FU_INDEX-1:0]	write_rs_status;
	input	wire 				write_rs_enable;

	input	wire reset, clk;

	reg_file registers(get_num1, get_num2, get_num3, value1, value2, value3,
                write_reg_src, write_reg_data, write_reg_enable, reset, clk);
	reg[FU_INDEX-1:0]	statuses[REG_FILE_SIZE-1:0];

	assign status1 = statuses[get_num1];
	assign status2 = statuses[get_num2];
	assign status3 = statuses[get_num3];

	always @(posedge clk or reset) begin:rs
		reg[WORD_SIZE-1:0] i;
		if (reset) begin
			for (i = 0; i < REG_FILE_SIZE; i = i+1)
				statuses[i] = READY;
		end else if(write_rs_enable)
					statuses[write_rs_src] <= write_rs_status;
	end
endmodule
