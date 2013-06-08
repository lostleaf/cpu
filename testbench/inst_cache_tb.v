module inst_cache_testbench;
    `include "parameters.v"

    wire [WORD_SIZE-1:0] out, ptr;
    wire hit;
    reg clk = 1;
    always #1 clk = ~clk;

    inst_cache ic(out, clk, ptr, hit);

    wire [WORD_SIZE-1:0] test_data [0:3];

    assign test_data[0] = 32'd1;
    assign test_data[1] = 32'd2;
    assign test_data[2] = 32'd3;
    assign test_data[3] = 32'd17;


    wire [WORD_SIZE-1:0] e;
    assign ptr = test_data[num];

    initial
        $monitor("time = %d, out = %h(%b), hit = %b, ptr = %d", $time, out, out, hit, ptr);

    reg [31:0]num = 0;
    always @(negedge clk) begin
        if (num == 20) $finish;
        num <= num + 1;
    end
endmodule