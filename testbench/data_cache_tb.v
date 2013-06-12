module data_memory_testbench;
    `include "parameters.v"

    wire write_enable, read_enable, hit;
    wire [WORD_SIZE-1:0] ptr, out_data, val;

    reg clk = 0;
    always #5 clk = ~clk;

    reg [31:0]num = 0;
    data_cache dcache(ptr, val, out_data, read_enable, write_enable, clk, hit);

    wire [WORD_SIZE*2+1:0] test_data [7:0];

    assign test_data[0] = {32'd3,  32'd0, 1'b0, 1'b1};
    assign test_data[1] = {32'd3,  32'd3, 1'b1, 1'b0};
    assign test_data[2] = {32'd3,  32'd0, 1'b0, 1'b1};
    assign test_data[3] = {32'd3,  32'd8, 1'b1, 1'b0};
    assign test_data[4] = {32'd3,  32'd0, 1'b0, 1'b1};

    assign {ptr, val, write_enable, read_enable} = test_data[num];

    initial begin
        $monitor("time = %d,  out_data = %b, hit = %b",
                 $time, out_data, hit);
    end

    always @(negedge clk) begin
        num <= num+1;
        if (num > 4) $finish;
    end    
endmodule