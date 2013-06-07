module inst_cache (out, clk, ptr, inst_get, ready);
    
    `include "parameters.v"

    input wire  clk, inst_get;
    output reg  ready=0;

    output reg  [WORD_SIZE-1:0] out;
    input wire  [WORD_SIZE-1:0] ptr;
    wire        [WORD_SIZE-1:0] inst;
    reg         [WORD_SIZE-1:0] p;
    reg         [3:0]           offset;
    reg         [9:0]           index;

    reg         [WORD_SIZE-1:0] cache [SET_SIZE-1:0][CHANNEL_SIZE-1:0][BLOCK_SIZE-1:0];
    reg         [TAG_SIZE -1:0]  tag  [SET_SIZE-1:0][CHANNEL_SIZE-1:0];


    inst_memory memory(inst, p);

    always @(posedge clk) begin
        if (inst_get) begin
            ready  = 0;
            p      = ptr;
            offset = p[3:0];
            index  = p[13:4];
            ready  = 1;
        end
    end
endmodule