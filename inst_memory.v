module inst_memory (out_block, out_inst, ptr);

    `include "parameters.v"

    input  wire [WORD_SIZE-1:0] ptr;
    output wire [WORD_SIZE-1:0] out_inst;

    output wire [WORD_SIZE*BLOCK_SIZE-1:0] out_block;
            
    reg [WORD_SIZE-1:0] memory [MEM_SIZE-1:0];

    wire[WORD_SIZE-1:0] ptr_block = {ptr[31:4], 4'b0};
    assign out_block = {  memory[ptr_block+0],memory[ptr_block+1],memory[ptr_block+2],
                        memory[ptr_block+3],memory[ptr_block+4],memory[ptr_block+5],
                        memory[ptr_block+6],memory[ptr_block+7],memory[ptr_block+8],
                        memory[ptr_block+9],memory[ptr_block+10],memory[ptr_block+11],
                        memory[ptr_block+12],memory[ptr_block+13],memory[ptr_block+14],
                        memory[ptr_block+15]};

    assign out_inst = memory[ptr];
    
    initial begin:init
        reg[WORD_SIZE-1:0] i;
        for(i = 0; i < MEM_SIZE; i = i + 1)
            data[i] = 32'h0;
        $readmemb("code.bin", memory);
    end
endmodule