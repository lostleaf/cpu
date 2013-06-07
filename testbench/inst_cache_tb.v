module inst_cache_testbench;
    `include "parameters.v"

    wire [WORD_SIZE-1:0] out, ptr;
    reg clk = 0;
    reg [31:0]num = 0;
    wire inst_get, is_ready;
    always #1 clk = ~clk;
    inst_cache ic(out, clk, ptr, inst_get, is_ready);

    wire [WORD_SIZE:0] test_data [0:2];

    assign test_data[0] = {32'd1, 1'b1};
    assign test_data[1] = {32'd2, 1'b1};
    assign test_data[2] = {32'd3, 1'b1};

    assign {ptr, inst_get} = test_data[num];

    initial
        $monitor("time = %d, out = %h(%b), is_ready = %b, num = %d, inst_get = %b", $time, out, out, is_ready, num, inst_get);

    reg [31:0] dd = 0;
    always @(negedge clk) begin
        if (dd == 20) $finish;
        if (is_ready) num <= num + 1;
        dd <= dd+1;
    end
endmodule