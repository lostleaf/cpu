module inst_memory (out, ptr);

    `include "parameters.v"

    input wire [WORD_SIZE-1:0] ptr;
    output wire [WORD_SIZE-1:0] out;
    reg [WORD_SIZE-1:0] memory [0:MEM_SIZE-1];


    assign out = memory[ptr[7:0]];

    initial begin
        memory[0] = 32'b0110_11101_11101_000000000010101000;
        memory[1] = 32'b0101_11110_11101_000000000010101000;
        memory[2] = 32'b0110_00101_11110_000000000001011100;
        memory[3] = 32'b0110_00110_11110_000000000001110100;
        memory[4] = 32'b0110_00111_11110_000000000010001100;
    end
endmodule