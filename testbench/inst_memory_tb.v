module inst_memory_testbench;
    `include "parameters.v"

    wire [WORD_SIZE-1:0] out, ptr;
    reg clk = 0;
    reg [31:0]num = 0;
    always #1 clk = ~clk;
    inst_memory im(out, ptr);

    wire [WORD_SIZE:0] test_data [0:2];

    assign test_data[0] = 32'd1;
    assign test_data[1] = 32'd2;
    assign test_data[2] = 32'd3;

    assign ptr = test_data[num];

    initial begin
        $monitor("time = %d, out = %h(%b)", $time, out, out);
    end

    always @(negedge clk) begin
        if (num == 3) begin
            $finish;
        end
        num <= num+1;
    end    
endmodule