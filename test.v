`include "timescale.v"
module test;
	reg clk = 1'b1;
	reg[31:0] data = 0;
	wire[31:0] data_wire;

	reg [31:0]	wire_data;

	assign data_wire = data;
	assign wire_data = data_wire;
	always begin
		#0.5 clk = 0;
		#0.5 clk = 1;
	end

	always @(posedge clk) begin
		data = data +1;
		$display($realtime, "data = %d", data);
	end

	always @(posedge clk) begin
		$display($realtime, "data_wire = %d, wire_data = %d", data_wire, wire_data);
	end
endmodule