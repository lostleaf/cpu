module instr_fetch(inst, ptr, clk, fetch_enable);

    `include "parameters.v"

    wire [WORD_SIZE-1:0] out;
    output reg [WORD_SIZE-1:0] inst;
    input wire [WORD_SIZE-1:0] ptr;
    input wire clk, fetch_enable;

    instr_memory memory(out, ptr);

    always @(posedge clk) begin
        if (fetch_enable) inst <= out;
    end

endmodule
