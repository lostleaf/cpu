module inst_cache (out, clk, ptr, hit, cache_enable);
    
    `include "parameters.v"

    input wire  clk, cache_enable;
    output reg  hit = 0;

    output reg  [WORD_SIZE-1:0] out;
    input wire  [WORD_SIZE-1:0] ptr;
    wire        [WORD_SIZE-1:0] inst;

    wire        [WORD_SIZE*BLOCK_SIZE-1:0] inst_block;
    
    inst_memory imemory(inst_block, inst, ptr);
    
    wire  [3:0]   offset;
    wire  [9:0]   index;
    wire  [17:0]  inst_tag;

    reg   [WORD_SIZE*BLOCK_SIZE-1:0]    cache [CACHE_SIZE-1:0];
    reg   [TAG_SIZE-1:0]                tag   [CACHE_SIZE-1:0];
    
    assign offset   = ptr[3:0];
    assign index    = ptr[13:4];
    assign inst_tag = ptr[31:14];

    always @(posedge clk) begin
        if (cache_enable) begin
            if (tag[index] === inst_tag) begin
                hit <= 1'b1;
                out <= inst;
            end else begin
                hit <= 1'b0;
                tag[index]   <= inst_tag;
                cache[index] <= inst_block;
                out <= inst;
            end
        end
    end
endmodule