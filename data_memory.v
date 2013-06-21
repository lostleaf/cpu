module data_memory (ptr_in, in_block, 
                    ptr_out1, ptr_out2, ptr_out3, 
                    out_data1, out_data2, out_data3, 
                    out_block1, out_block2, out_block3, clk, write_enable);
    
    `include "parameters.v"

    input  wire [WORD_SIZE-1:0]             ptr_in, ptr_out1, ptr_out2, ptr_out3;
    input  wire [BLOCK_SIZE*WORD_SIZE-1:0]  in_block;
    input  wire                             clk, write_enable;

    output wire [WORD_SIZE-1:0]             out_data1, out_data2, out_data3;
    output wire [BLOCK_SIZE*WORD_SIZE-1:0]  out_block1, out_block2, out_block3;

    reg [WORD_SIZE-1:0] memory [0:MEM_SIZE-1];

    wire[WORD_SIZE-1:0] ptr_block_in   = {ptr_in[31:4], 4'b0};
    wire[WORD_SIZE-1:0] ptr_block_out1 = {ptr_out1[31:4], 4'b0};
    wire[WORD_SIZE-1:0] ptr_block_out2 = {ptr_out2[31:4], 4'b0};
    wire[WORD_SIZE-1:0] ptr_block_out3 = {ptr_out3[31:4], 4'b0};

    assign out_block1 
        = {memory[ptr_block_out1+0],memory[ptr_block_out1+1],memory[ptr_block_out1+2],
        memory[ptr_block_out1+3],memory[ptr_block_out1+4],memory[ptr_block_out1+5],
        memory[ptr_block_out1+6],memory[ptr_block_out1+7],memory[ptr_block_out1+8],
        memory[ptr_block_out1+9],memory[ptr_block_out1+10],memory[ptr_block_out1+11],
        memory[ptr_block_out1+12],memory[ptr_block_out1+13],memory[ptr_block_out1+14],
        memory[ptr_block_out1+15]};

    assign out_block2 
        = {memory[ptr_block_out2+0],memory[ptr_block_out2+1],memory[ptr_block_out2+2],
        memory[ptr_block_out2+3],memory[ptr_block_out2+4],memory[ptr_block_out2+5],
        memory[ptr_block_out2+6],memory[ptr_block_out2+7],memory[ptr_block_out2+8],
        memory[ptr_block_out2+9],memory[ptr_block_out2+10],memory[ptr_block_out2+11],
        memory[ptr_block_out2+12],memory[ptr_block_out2+13],memory[ptr_block_out2+14],
        memory[ptr_block_out2+15]};

    assign out_block3 
        = {memory[ptr_block_out3+0],memory[ptr_block_out3+1],memory[ptr_block_out3+2],
        memory[ptr_block_out3+3],memory[ptr_block_out3+4],memory[ptr_block_out3+5],
        memory[ptr_block_out3+6],memory[ptr_block_out3+7],memory[ptr_block_out3+8],
        memory[ptr_block_out3+9],memory[ptr_block_out3+10],memory[ptr_block_out3+11],
        memory[ptr_block_out3+12],memory[ptr_block_out3+13],memory[ptr_block_out3+14],
        memory[ptr_block_out3+15]};

    assign out_data1 = memory[ptr_out1];
    assign out_data2 = memory[ptr_out2];
    assign out_data3 = memory[ptr_out3];

    initial begin:init
        reg[WORD_SIZE-1:0] i;
        for(i = 0; i < MEM_SIZE; i = i + 1)
            memory[i] = 0;
        $readmemh("ram_data.hex", memory);
    end

    always @(posedge clk) begin
        // $display("%d %d %d", ptr_out1, ptr_out2, ptr_out3);
        if (write_enable) begin
            {memory[ptr_block_in+0],memory[ptr_block_in+1],memory[ptr_block_in+2],
            memory[ptr_block_in+3],memory[ptr_block_in+4],memory[ptr_block_in+5],
            memory[ptr_block_in+6],memory[ptr_block_in+7],memory[ptr_block_in+8],
            memory[ptr_block_in+9],memory[ptr_block_in+10],memory[ptr_block_in+11],
            memory[ptr_block_in+12],memory[ptr_block_in+13],memory[ptr_block_in+14],
            memory[ptr_block_in+15]} = in_block;
        end
    end
endmodule
