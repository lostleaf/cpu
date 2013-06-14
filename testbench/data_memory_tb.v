module data_memory_testbench;
    `include "parameters.v"

    wire write_enable;
    wire [WORD_SIZE-1:0] ptr_in, ptr_out1, ptr_out2, ptr_out3;
    wire [WORD_SIZE-1:0] out_data1, out_data2, out_data3;
    wire [WORD_SIZE*BLOCK_SIZE-1:0] out_block1, out_block2, out_block3, in_block;

    reg clk = 0;
    always #5 clk = ~clk;

    reg [31:0]num = 0;

    data_memory dmemory(ptr_in, in_block, 
                    ptr_out1, ptr_out2, ptr_out3, 
                    out_data1, out_data2, out_data3, 
                    out_block1, out_block2, out_block3, clk, write_enable);

    wire [128:0] test_data [7:0];
    assign test_data[0] = {32'd3, 32'd1, 32'd2, 32'd3, 1'b0};
    assign test_data[1] = {32'd3, 32'd1, 32'd2, 32'd3, 1'b1};
    assign test_data[2] = {32'd3, 32'd1, 32'd2, 32'd3, 1'b0};


    assign {ptr_in, ptr_out1, ptr_out2, ptr_out3, write_enable} = test_data[num];
    assign in_block = 0;

    initial begin
        //$display("%b", test_data[0]);
         $monitor("time = %d,  memory[%1d] = %h,  out_data2[%1d] = %h,  out_data2[%1d] = %h",
                  $time,  ptr_out1, out_data1, ptr_out2, out_data2, ptr_out3, out_data3);
        //$monitor("%b %b %b %b",  ptr_in, ptr_out1, ptr_out2, ptr_out3);
    end

    always @(negedge clk) begin
        num <= num+1;
        if (num > 2) $finish;
    end    
endmodule