module inst_fetch_testbench ;
    
    `include "parameters.v"

    reg clk = 0;
    always #5 clk = ~clk;
    
    wire [0:WORD_SIZE-1] ptr, inst;
    wire busy;
    reg fetch_enable = 1;

    inst_fetch ifetch(inst, ptr, clk, fetch_enable, busy);


    wire [7:0] total = 5;
    reg [7:0] num = 0;

    wire [WORD_SIZE-1:0] data [0:7];
    assign data[0] = 32'd1;
    assign data[1] = 32'd4;
    assign data[2] = 32'd20;
    assign data[3] = 32'd21;
    assign data[4] = 32'd33;

    assign ptr = data[num];

    always @(negedge clk or negedge busy) 
        if (!busy) fetch_enable = 1;

    always @(posedge busy)
        fetch_enable = 0;

    always @(negedge clk) begin
        num <= num + 1;
        if (num > total) $finish;
    end

     initial 
         $monitor("time = %t, ptr = %d, inst = %h(%b), busy = %b, fetch_enable = %b", 
             $time, ptr, inst, inst, busy, fetch_enable);

endmodule