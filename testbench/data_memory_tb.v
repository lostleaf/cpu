module data_memory_testbench;
    `include "parameters.v"

    wire write_enable;
    wire [WORD_SIZE-1:0] ptr, out_data, val;
    wire [WORD_SIZE*BLOCK_SIZE-1:0] out_block;

    reg clk = 0;
    always #5 clk = ~clk;

    reg [31:0]num = 0;

    data_memory dmemory(ptr, val, out_data, out_block, clk, write_enable);

    wire [WORD_SIZE*2:0] test_data [7:0];

    assign test_data[0] = {32'd3,  32'd0, 1'b0};
    assign test_data[1] = {32'd3,  32'd3, 1'b1};
    assign test_data[2] = {32'd3,  32'd0, 1'b0};
    assign test_data[3] = {32'd3,  32'd8, 1'b1};
    assign test_data[4] = {32'd3,  32'd0, 1'b0};

    assign {ptr, val, write_enable} = test_data[num];

    initial begin
        $monitor("time = %d,  out_data = %h(%b)",
                 $time,  out_data, out_data);
    end

    always @(negedge clk) begin
        num <= num+1;
        if (num > 4) $finish;
    end    
endmodule