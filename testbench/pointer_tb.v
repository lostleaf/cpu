module pointer_testbench ;
    `include "parameters.v"

    wire signed [WORD_SIZE-1:0] val;
    wire [WORD_SIZE-1:0] out;
    wire set_enable, update_enable;
    reg clk = 0;
    reg [2:0]num = 0;

    always #1 clk = ~clk;
    pointer p(out, val, set_enable, update_enable, clk);

    wire [1 + WORD_SIZE : 0] test_data [0:1];

    assign test_data[0] = {32'h1, 1'b0, 1'b1};
    assign test_data[1] = {32'h3, 1'b1, 1'b0};

    assign {val, set_enable, update_enable} = test_data[num];

    initial
        $monitor("time = %d, out = %h", $time, out);

    always @(negedge clk) begin
        if (num == 2) begin
            $finish;
        end
        num <= num+1;
    end

endmodule