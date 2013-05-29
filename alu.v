module alu (op, in1, in2, clk, out, able);

	`include "parameters.v"

	input wire [3:0] op;
	input wire clk, able;
	input wire [WORD_SIZE-1:0] in1, in2;
	output reg [WORD_SIZE-1:0] out;

	always @(posedge clk) begin
		if (able) begin
            case (op)
                ALU_ADD:
                    out <= in1 + in2;
                ALU_SUB:
                    out <= in1 - in2;
                ALU_MUL:
                    out <= in1 * in2;
                ALU_SLT:
                    out <= in1 < in2;
                ALU_AND:
                    out <= in1 & in2;
                ALU_OR:
                    out <= in1 | in2;
                ALU_XOR:
                    out <= in1 ^ in2;
                ALU_LSHIFT:
                    out <= in1 << in2;
                ALU_RSHIFT:
                	out <= in1 >> in2;
            endcase
		end
	end

endmodule
