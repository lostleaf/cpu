// use write back policy
// if miss, save dirty cache, load new block
module data_cache(ptr_read1,    ptr_read2,    ptr_read3, 
                  out1,         out2,         out3, 
                  hit_read1,    hit_read2,    hit_read3, 
                  read_enable1, read_enable2, read_enable3,
                  ptr_write, val, write_enable, hit_write, clk);

    `include "parameters.v"

    input  wire read_enable1, read_enable2, read_enable3, write_enable, clk;
    input  wire [WORD_SIZE-1:0] ptr_read1, ptr_read2, ptr_read3, ptr_write, val;

    output reg  [WORD_SIZE-1:0] out1, out2, out3;
    output reg  hit_read1, hit_read2, hit_read3, hit_write;

    wire        [WORD_SIZE-1:0]             out_mem_data1, out_mem_data2, out_mem_data3;
    wire        [WORD_SIZE*BLOCK_SIZE-1:0]  out_mem_block1, out_mem_block2, out_mem_block3;
    reg                                     mwrite_enable;
    reg         [WORD_SIZE*BLOCK_SIZE-1:0]  in_mem_block;

    data_memory dmemory(ptr_write, in_mem_block, 
                    ptr_read1, ptr_read2, ptr_read3, 
                    out_mem_data1, out_mem_data2, out_mem_data3, 
                    out_mem_block1, out_mem_block2, out_mem_block3, 
                    clk, write_enable);

    wire  [3:0]   offset_r1,   offset_r2,   offset_r3,   offset_w;
    wire  [9:0]   index_r1,    index_r2,    index_r3,    index_w;
    wire  [17:0]  data_tag_r1, data_tag_r2, data_tag_r3, data_tag_w;

    reg [WORD_SIZE*BLOCK_SIZE-1:0]  cache   [CACHE_SIZE-1:0];
    reg [TAG_SIZE-1:0]              tag     [CACHE_SIZE-1:0];
    reg                             dirty   [CACHE_SIZE-1:0];
    reg                             valid   [CACHE_SIZE-1:0];

    assign offset_r1   = ptr_read1[3:0];
    assign index_r1    = ptr_read1[13:4];
    assign data_tag_r1 = ptr_read1[31:14];
    
    assign offset_r2   = ptr_read2[3:0];
    assign index_r2    = ptr_read2[13:4];
    assign data_tag_r2 = ptr_read2[31:14];
    
    assign offset_r3   = ptr_read3[3:0];
    assign index_r3    = ptr_read3[13:4];
    assign data_tag_r3 = ptr_read3[31:14];
    
    assign offset_w    = ptr_write[3:0];
    assign index_w     = ptr_write[13:4];
    assign data_tag_w  = ptr_write[31:14];

    initial begin:init
        reg [WORD_SIZE-1:0] i;
        for(i = 0; i < CACHE_SIZE; i = i + 1) begin
            valid[i] = 1'b0;
            dirty[i] = 1'b0;
            tag[i]   = 18'b0;
        end
    end

    always @(posedge clk) begin
        if (read_enable1 || read_enable2 || read_enable3) begin
            if (read_enable1)
                read_data(dirty[index_r1], valid[index_r1], tag[index_r1], 
                        data_tag_r1, cache[index_r1], offset_r1, hit_read1, out1,
                        out_mem_block1);
            if (read_enable2)
                read_data(dirty[index_r2], valid[index_r2], tag[index_r2], 
                        data_tag_r2, cache[index_r2], offset_r2, hit_read2, out2,
                        out_mem_block2);
            if (read_enable3)
                read_data(dirty[index_r3], valid[index_r3], tag[index_r3], 
                        data_tag_r3, cache[index_r3], offset_r3, hit_read3, out3,
                        out_mem_block3);
            // $display("h : %b %d" , hit_read1, out_mem_block1);
        end
        // $display(cache[0]);

    end

    always @(negedge clk) begin
        // $display("write cache? %b", write_enable);
        if (write_enable) begin
            // $display("cache writed");
            if (tag[index_w] == data_tag_w && valid[index_w]) begin 
                hit_write      = 1;
                dirty[index_w] = 1;
                valid[index_w] = 1;
                cache[index_w] = 
                    (// xor new value with original value in cache
                        (
                            val ^ (cache[index_w] >> 
                            ((15 - offset_w) * WORD_SIZE) & 32'hffffffff)
                        )
                    //shift to original place
                        << ((15 - offset_w) * WORD_SIZE)
                    ) ^ cache[index_w];
            end else begin
                hit_write = 0;
                //write back
                if (dirty[index_w] && valid[index_w]) begin
                    in_mem_block  = cache[index_w];
                    mwrite_enable = 1'b1;
                end
                // #0.1; // necessory?
                mwrite_enable  = 1'b0;
                dirty[index_w] = 0;
                valid[index_w] = 1;
                tag[index_w]   = data_tag_w;               
                cache[index_w] = 
                    (// xor new value with original value in cache
                        (
                            val ^ (cache[index_w] >> 
                            ((15 - offset_w) * WORD_SIZE) & 32'hffffffff)
                        )
                    //shift to original place
                        << ((15 - offset_w) * WORD_SIZE)
                    ) ^ cache[index_w];
                $display(cache[0]);
            end
            $display($realtime, ": dcache[%g]  = %g",ptr_write, cache[index_w]);
        end
    end

    task read_data;
        inout dirty, valid;
        inout [TAG_SIZE-1:0] tag;
        input [TAG_SIZE-1:0] data_tag;
        inout [BLOCK_SIZE*WORD_SIZE-1:0] cache;
        input [3:0] offset;
        output hit;
        output [WORD_SIZE-1:0] out;
        input [BLOCK_SIZE*WORD_SIZE-1:0] out_mem_block;
        begin
            if (tag == data_tag && valid) begin 
                // $display(hit);
                hit = 1;
                out = cache >> ((15 - offset) * WORD_SIZE) & 32'hffffffff;
                dirty = 1;
                valid = 1;
            end else begin
                hit = 0;
                //write back
                if (dirty && valid) begin
                    in_mem_block  = cache;
                    mwrite_enable = 1'b1;
                    // #0.1; // necessory?
                    mwrite_enable = 1'b0;
                end
                cache = out_mem_block; 
                // $display("%t %d %d", $time, offset, out_mem_block, out_mem_block1);
                dirty = 0;
                valid = 1;
                tag = data_tag;
                out = cache >> ((15 - offset) * WORD_SIZE) & 32'hffffffff;               
            end
        end
    endtask
endmodule