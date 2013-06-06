module alu_testbench;

    `include "parameters.v"

    wire [0:3] op;
    wire [0:WORD_SIZE-1] in1, in2;
    wire [0:WORD_SIZE-1] expected;
    wire [0:WORD_SIZE-1] out;
    reg clk = 0;
    wire alu_enable = 1;

    alu alu1(op, in1, in2, clk, out, alu_enable);

    always #5 clk = !clk;

    wire [7:0] total = 3;
    reg [7:0] num = 0;

    wire [0:3 + WORD_SIZE*3] data [0:7];
    assign data[0] = { ALU_ADD,    32'd5,  32'd7,  32'd12 };
    assign data[1] = { ALU_SUB,    32'd15, 32'd4,  32'd11 };
    assign data[2] = { ALU_MUL,    32'd4,  32'd9,  32'd36 };

    assign { op, in1, in2, expected } = data[num];

    always @(negedge clk) begin
        if (num < total)
            $display("data = %h, op = %d, in1 = %d, in2 = %d, expected = %d, out = %d, AC = %d",
                    data[num], op, in1, in2, expected, out, (expected == out));
        else
            $finish;
        num <= num + 1;
    end

endmodule
