module data_memory_testbench;
    `include "parameters.v"

    wire read_enable1, read_enable2, read_enable3, write_enable;
    wire [WORD_SIZE-1:0] ptr_read1, ptr_read2, ptr_read3, ptr_write, val;

    wire [WORD_SIZE-1:0] out1, out2, out3;
    wire hit_read1 = 0, hit_read2 = 0, hit_read3 = 0, hit_write = 0;
    reg clk = 0;
    always #5 clk = ~clk;

    reg [31:0]num = 0;

    data_cache dcache(ptr_read1,    ptr_read2,    ptr_read3, 
                      out1,         out2,         out3, 
                      hit_read1,    hit_read2,    hit_read3, 
                      read_enable1, read_enable2, read_enable3,
                      ptr_write, val, write_enable, hit_write, clk);

    wire [WORD_SIZE*2+1:0] test_data [7:0];

    assign test_data[0] = {32'd3, 32'd13, 32'd23, 32'd0, 32'b0, 1'b1, 1'b1, 1'b1, 1'b0};
    assign test_data[1] = {32'd3, 32'd13, 32'd23, 32'd0, 32'b0, 1'b1, 1'b1, 1'b1, 1'b0};
    assign test_data[2] = {32'd3, 32'd13, 32'd23, 32'd0, 32'b0, 1'b1, 1'b1, 1'b1, 1'b0};

    assign {ptr_read1, ptr_read2, ptr_read3, ptr_write, val, 
            read_enable1, read_enable2, read_enable3, write_enable} 
            = test_data[num];

    initial begin
        $monitor("time = %d,  out_data = %b, hit = %b",
                 $time, out_data, hit);
    end

    always @(negedge clk) begin
        num <= num+1;
        if (num > 2) $finish;
    end    
endmodule