module pointer (out, val, set_enable, update_enable, clk);
    `include "parameters.v"

    output reg [WORD_SIZE-1:0] out = 0;
    input wire signed [WORD_SIZE-1:0] val;
    input wire clk, set_enable, update_enable;

    always @(posedge clk) begin
        if (set_enable) begin
            out <= val;
        end
        else if (update_enable) begin
            out <= out + val;
        end
    end
endmodule