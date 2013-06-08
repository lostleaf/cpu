//not finished yet
module data_memory (out, ptr, clk, is_busy, inst_get);
    
    `include "parameters.v"

    input   wire [WORD_SIZE-1:0] ptr;
    input   wire inst_get;
    input   wire clk;

    output  reg is_busy=0;
    output  reg [WORD_SIZE-1:0] out;

    reg [WORD_SIZE-1:0] memory [0:MEM_SIZE-1];
    reg [WORD_SIZE-1:0] pointer;

    always @(posedge clk) begin
        if (!is_busy) begin
            if (inst_get) begin
                is_busy = 1;
                pointer = ptr;
                #10 out = memory[pointer[3:0]];
                is_busy = 0;
            end
        end
    end

endmodule