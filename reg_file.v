module reg_file(get_num1, get_num2, out1, out2, get_enable, 
                set_num, set_val, set_enable, reset_enable, clk);

    `include "parameters.v"

    input wire [REG_FILE_SIZE-1:0]  get_num1, get_num2, set_num;
    input wire [WORD_SIZE-1:0]      set_val;
    output reg [WORD_SIZE-1:0]      out1, out2;

    input wire clk, get_enable, set_enable, reset_enable;

    reg [WORD_SIZE-1:0] register [REG_STACK_SIZE-1:0];

    reg [REG_FILE_SIZE:0] i;

    always @(posedge clk) begin
        if (reset_enable) begin
            for (i = 0; i < REG_STACK_SIZE; i = i+1) begin
                register[i] <= 0;
            end
        end else if (get_enable) begin
            out1 <= register[get_num1];
            out2 <= register[get_num2];
        end else if (set_enable) begin
            $display("reg %d set to %d", set_num, set_val);
            register[set_num] <= set_val;
        end
    end

endmodule
