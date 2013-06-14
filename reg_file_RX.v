 module reg_file(get_num1, get_num2, get_num3, out1, out2, out3,
                set_num, set_val, set_enable, reset_enable, clk);

    `include "parameters.v"

    input wire [REG_INDEX-1:0]  get_num1, get_num2,get_num3, set_num;
    input wire [WORD_SIZE-1:0]      set_val;
    output wire [WORD_SIZE-1:0]      out1, out2, out3;

    input wire clk, set_enable, reset_enable;

    reg [WORD_SIZE-1:0] register [REG_FILE_SIZE-1:0];

    reg [REG_INDEX:0] i;

    assign out1 = register[get_num1];
    assign out2 = register[get_num2];
    assign out3 = register[get_num3];

    // for test
    initial begin
        
    end

    always @(negedge clk or posedge reset_enable) begin
        if (reset_enable) begin
            for (i = 0; i < REG_FILE_SIZE; i = i+1) begin
                register[i] <= 0;
            end
        end else if (set_enable) begin
            register[set_num] <= set_val;
        end
    end

endmodule
