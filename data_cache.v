// use write back policy
// if miss, save dirty cache, load new block
module data_cache(ptr, val, out, read_enable, write_enable, clk, hit);

    `include "parameters.v"

    input  wire                 read_enable, write_enable, clk;
    input  wire [WORD_SIZE-1:0] ptr, val;

    output reg  [WORD_SIZE-1:0] out;
    output reg                  hit = 0;

    wire        [WORD_SIZE-1:0]             out_mem_data;
    wire        [WORD_SIZE*BLOCK_SIZE-1:0]  out_mem_block;
    reg                                     mwrite_enable;
    reg         [WORD_SIZE*BLOCK_SIZE-1:0]  in_mem_block;

    data_memory dmemory(ptr, in_mem_block, out_mem_data, out_mem_block, clk, mwrite_enable);

    wire  [3:0]   offset;
    wire  [9:0]   index;
    wire  [17:0]  data_tag;

    reg [WORD_SIZE*BLOCK_SIZE-1:0]  cache   [CACHE_SIZE-1:0];
    reg [TAG_SIZE-1:0]              tag     [CACHE_SIZE-1:0];
    reg                             dirty   [CACHE_SIZE-1:0];
    reg                             valid   [CACHE_SIZE-1:0];

    assign offset   = ptr[3:0];
    assign index    = ptr[13:4];
    assign data_tag = ptr[31:14];

    initial begin:init
        reg [WORD_SIZE-1:0] i;
        for(i = 0; i < CACHE_SIZE; i = i + 1) begin
            valid[i] = 1'b0;
            dirty[i] = 1'b0;
            tag[i]   = 18'b0;
        end
    end

    always @(posedge clk) begin
        if (write_enable && read_enable) begin
            $display("%m illegal, write and read both enabled");
            $stop;
        end

        if (write_enable) begin
            if (tag[index] == data_tag && valid[index]) begin 
                hit          = 1;
                dirty[index] = 1;
                valid[index] = 1;
                cache[index] = 
                    (// xor new value with original value in cache
                        (val ^ (cache[index] >> ((15 - offset) * WORD_SIZE) & 32'hffffffff))
                    //shift to original place
                        << ((15 - offset) * WORD_SIZE)
                    ) ^ cache[index];
            end else begin
                hit = 0;
                //write back
                if (dirty[index] && valid[index]) begin
                    in_mem_block  = cache[index];
                    mwrite_enable = 1'b1;
                end
                #1; // necessory?
                mwrite_enable = 1'b0;
                dirty[index]  = 0;
                valid[index]  = 1;
                cache[index]  = 
                    (// xor new value with original value in cache
                        (val ^ (cache[index] >> ((15 - offset) * WORD_SIZE) & 32'hffffffff))
                    //shift to original place
                        << ((15 - offset) * WORD_SIZE)
                    ) ^ cache[index];               
            end
        end

        if (read_enable) begin
            if (tag[index] == data_tag && valid[index]) begin 
                hit = 1;
                out = cache[index] >> ((15 - offset) * WORD_SIZE) & 32'hffffffff;
                dirty[index] = 1;
                valid[index] = 1;
            end else begin
                hit = 0;
                //write back
                $display($time);
                if (dirty[index] && valid[index]) begin
                    in_mem_block  = cache[index];
                    mwrite_enable = 1'b1;
                    #1; // necessory?
                    mwrite_enable = 1'b0;
                end
                cache[index]  = out_mem_block; 
                dirty[index]  = 0;
                valid[index]  = 1;
                out = cache[index] >> ((15 - offset) * WORD_SIZE) & 32'hffffffff;               
            end
        end
end
endmodule