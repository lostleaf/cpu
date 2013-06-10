module inst_memory (out_block, out_inst, ptr);

    `include "parameters.v"

    input  wire [WORD_SIZE-1:0] ptr;
    output wire [WORD_SIZE-1:0] out_inst;

    output wire [WORD_SIZE*BLOCK_SIZE-1:0] out_block;
            
    reg [WORD_SIZE-1:0] memory [MEM_SIZE-1:0];

    wire[WORD_SIZE-1:0] p = {ptr[31:4], 4'b0};
    assign out_block = {  memory[p+0],memory[p+1],memory[p+2],memory[p+3],
                    memory[p+4],memory[p+5],memory[p+6],memory[p+7],
                    memory[p+8],memory[p+9],memory[p+10],memory[p+11],
                    memory[p+12],memory[p+13],memory[p+14],memory[p+15]};

    assign out_inst = memory[ptr];
    
    initial begin
        $readmemb("binary", memory);
        // $monitor("ptr = %h, p = %h", ptr, p);
    end
endmodule