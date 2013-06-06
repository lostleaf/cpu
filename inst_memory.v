module inst_memory (out, ptr);
    output wire [WORD_SIZE-1:0] out;
    input wire [WORD_SIZE-1:0] ptr;
    reg [WORD_SIZE-1:0] memory [0:MEM_SIZE-1];

    
    assign out = memory[pointer[7:0]];
endmodule