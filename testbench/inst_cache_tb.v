module inst_cache_testbench;
    `include "parameters.v"

    wire [WORD_SIZE-1:0] out, ptr;
    wire hit;
    reg clk = 1;
    always #1 clk = ~clk;

    inst_cache ic(out, clk, ptr, hit, clk);

    wire [WORD_SIZE-1:0] test_data [0:7];

    assign test_data[0] = 32'd1;
    assign test_data[1] = 32'd33;
    assign test_data[2] = 32'd20;
    assign test_data[3] = 32'd4;
    assign test_data[4] = 32'd4;


    assign ptr = test_data[num];

    reg [31:0] num = 0;
    always @(negedge clk) begin
        $display("%d", num);
        if (num == 20) $finish;
        num <= num + 1;
    end

endmodule