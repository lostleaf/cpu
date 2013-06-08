module test ;
    reg clk = 1;
    always #1 clk = ~clk;
    reg [31:0] in1=1, in2=2;
    wire [31:0] out;
    wire [3:0] op = 4'h0;
    wire alu_enable = 1'b1;

    alu alu1(op, in1, in2, clk, out, alu_enable);

    always #2 in1 <= in1+1;


    initial begin
        $monitor("time = %t, in1 = %d, out = %d", $time, in1, out);
         #10 $finish;
    end
endmodule