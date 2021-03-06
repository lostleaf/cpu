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
    reg [BLOCK_INDEX-1:0] zero = 'b0;

    wire[WORD_SIZE-1:0] ptr_block_in   = {ptr_in[WORD_SIZE-1:BLOCK_INDEX], zero};
    wire[WORD_SIZE-1:0] ptr_block_out1 = {ptr_out1[WORD_SIZE-1:BLOCK_INDEX], zero};
    wire[WORD_SIZE-1:0] ptr_block_out2 = {ptr_out2[WORD_SIZE-1:BLOCK_INDEX], zero};
    wire[WORD_SIZE-1:0] ptr_block_out3 = {ptr_out3[WORD_SIZE-1:BLOCK_INDEX], zero};

    assign out_block1 
        = {memory[ptr_block_out1+0], 
memory[ptr_block_out1+1], 
memory[ptr_block_out1+2], 
memory[ptr_block_out1+3], 
memory[ptr_block_out1+4], 
memory[ptr_block_out1+5], 
memory[ptr_block_out1+6], 
memory[ptr_block_out1+7], 
memory[ptr_block_out1+8], 
memory[ptr_block_out1+9], 
memory[ptr_block_out1+10], 
memory[ptr_block_out1+11], 
memory[ptr_block_out1+12], 
memory[ptr_block_out1+13], 
memory[ptr_block_out1+14], 
memory[ptr_block_out1+15], 
memory[ptr_block_out1+16], 
memory[ptr_block_out1+17], 
memory[ptr_block_out1+18], 
memory[ptr_block_out1+19], 
memory[ptr_block_out1+20], 
memory[ptr_block_out1+21], 
memory[ptr_block_out1+22], 
memory[ptr_block_out1+23], 
memory[ptr_block_out1+24], 
memory[ptr_block_out1+25], 
memory[ptr_block_out1+26], 
memory[ptr_block_out1+27], 
memory[ptr_block_out1+28], 
memory[ptr_block_out1+29], 
memory[ptr_block_out1+30], 
memory[ptr_block_out1+31], 
memory[ptr_block_out1+32], 
memory[ptr_block_out1+33], 
memory[ptr_block_out1+34], 
memory[ptr_block_out1+35], 
memory[ptr_block_out1+36], 
memory[ptr_block_out1+37], 
memory[ptr_block_out1+38], 
memory[ptr_block_out1+39], 
memory[ptr_block_out1+40], 
memory[ptr_block_out1+41], 
memory[ptr_block_out1+42], 
memory[ptr_block_out1+43], 
memory[ptr_block_out1+44], 
memory[ptr_block_out1+45], 
memory[ptr_block_out1+46], 
memory[ptr_block_out1+47], 
memory[ptr_block_out1+48], 
memory[ptr_block_out1+49], 
memory[ptr_block_out1+50], 
memory[ptr_block_out1+51], 
memory[ptr_block_out1+52], 
memory[ptr_block_out1+53], 
memory[ptr_block_out1+54], 
memory[ptr_block_out1+55], 
memory[ptr_block_out1+56], 
memory[ptr_block_out1+57], 
memory[ptr_block_out1+58], 
memory[ptr_block_out1+59], 
memory[ptr_block_out1+60], 
memory[ptr_block_out1+61], 
memory[ptr_block_out1+62], 
memory[ptr_block_out1+63]};

    assign out_block2 
        = { memory[ptr_block_out2+0], 
    memory[ptr_block_out2+1], 
    memory[ptr_block_out2+2], 
    memory[ptr_block_out2+3], 
    memory[ptr_block_out2+4], 
    memory[ptr_block_out2+5], 
    memory[ptr_block_out2+6], 
    memory[ptr_block_out2+7], 
    memory[ptr_block_out2+8], 
    memory[ptr_block_out2+9], 
    memory[ptr_block_out2+10], 
    memory[ptr_block_out2+11], 
    memory[ptr_block_out2+12], 
    memory[ptr_block_out2+13], 
    memory[ptr_block_out2+14], 
    memory[ptr_block_out2+15], 
    memory[ptr_block_out2+16], 
    memory[ptr_block_out2+17], 
    memory[ptr_block_out2+18], 
    memory[ptr_block_out2+19], 
    memory[ptr_block_out2+20], 
    memory[ptr_block_out2+21], 
    memory[ptr_block_out2+22], 
    memory[ptr_block_out2+23], 
    memory[ptr_block_out2+24], 
    memory[ptr_block_out2+25], 
    memory[ptr_block_out2+26], 
    memory[ptr_block_out2+27], 
    memory[ptr_block_out2+28], 
    memory[ptr_block_out2+29], 
    memory[ptr_block_out2+30], 
    memory[ptr_block_out2+31], 
    memory[ptr_block_out2+32], 
    memory[ptr_block_out2+33], 
    memory[ptr_block_out2+34], 
    memory[ptr_block_out2+35], 
    memory[ptr_block_out2+36], 
    memory[ptr_block_out2+37], 
    memory[ptr_block_out2+38], 
    memory[ptr_block_out2+39], 
    memory[ptr_block_out2+40], 
    memory[ptr_block_out2+41], 
    memory[ptr_block_out2+42], 
    memory[ptr_block_out2+43], 
    memory[ptr_block_out2+44], 
    memory[ptr_block_out2+45], 
    memory[ptr_block_out2+46], 
    memory[ptr_block_out2+47], 
    memory[ptr_block_out2+48], 
    memory[ptr_block_out2+49], 
    memory[ptr_block_out2+50], 
    memory[ptr_block_out2+51], 
    memory[ptr_block_out2+52], 
    memory[ptr_block_out2+53], 
    memory[ptr_block_out2+54], 
    memory[ptr_block_out2+55], 
    memory[ptr_block_out2+56], 
    memory[ptr_block_out2+57], 
    memory[ptr_block_out2+58], 
    memory[ptr_block_out2+59], 
    memory[ptr_block_out2+60], 
    memory[ptr_block_out2+61], 
    memory[ptr_block_out2+62], 
    memory[ptr_block_out2+63]};

    assign out_block3 
        = { memory[ptr_block_out3+0], 
    memory[ptr_block_out3+1], 
    memory[ptr_block_out3+2], 
    memory[ptr_block_out3+3], 
    memory[ptr_block_out3+4], 
    memory[ptr_block_out3+5], 
    memory[ptr_block_out3+6], 
    memory[ptr_block_out3+7], 
    memory[ptr_block_out3+8], 
    memory[ptr_block_out3+9], 
    memory[ptr_block_out3+10], 
    memory[ptr_block_out3+11], 
    memory[ptr_block_out3+12], 
    memory[ptr_block_out3+13], 
    memory[ptr_block_out3+14], 
    memory[ptr_block_out3+15], 
    memory[ptr_block_out3+16], 
    memory[ptr_block_out3+17], 
    memory[ptr_block_out3+18], 
    memory[ptr_block_out3+19], 
    memory[ptr_block_out3+20], 
    memory[ptr_block_out3+21], 
    memory[ptr_block_out3+22], 
    memory[ptr_block_out3+23], 
    memory[ptr_block_out3+24], 
    memory[ptr_block_out3+25], 
    memory[ptr_block_out3+26], 
    memory[ptr_block_out3+27], 
    memory[ptr_block_out3+28], 
    memory[ptr_block_out3+29], 
    memory[ptr_block_out3+30], 
    memory[ptr_block_out3+31], 
    memory[ptr_block_out3+32], 
    memory[ptr_block_out3+33], 
    memory[ptr_block_out3+34], 
    memory[ptr_block_out3+35], 
    memory[ptr_block_out3+36], 
    memory[ptr_block_out3+37], 
    memory[ptr_block_out3+38], 
    memory[ptr_block_out3+39], 
    memory[ptr_block_out3+40], 
    memory[ptr_block_out3+41], 
    memory[ptr_block_out3+42], 
    memory[ptr_block_out3+43], 
    memory[ptr_block_out3+44], 
    memory[ptr_block_out3+45], 
    memory[ptr_block_out3+46], 
    memory[ptr_block_out3+47], 
    memory[ptr_block_out3+48], 
    memory[ptr_block_out3+49], 
    memory[ptr_block_out3+50], 
    memory[ptr_block_out3+51], 
    memory[ptr_block_out3+52], 
    memory[ptr_block_out3+53], 
    memory[ptr_block_out3+54], 
    memory[ptr_block_out3+55], 
    memory[ptr_block_out3+56], 
    memory[ptr_block_out3+57], 
    memory[ptr_block_out3+58], 
    memory[ptr_block_out3+59], 
    memory[ptr_block_out3+60], 
    memory[ptr_block_out3+61], 
    memory[ptr_block_out3+62], 
    memory[ptr_block_out3+63]};

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
            {memory[ptr_block_in+0], 
    memory[ptr_block_in+1], 
    memory[ptr_block_in+2], 
    memory[ptr_block_in+3], 
    memory[ptr_block_in+4], 
    memory[ptr_block_in+5], 
    memory[ptr_block_in+6], 
    memory[ptr_block_in+7], 
    memory[ptr_block_in+8], 
    memory[ptr_block_in+9], 
    memory[ptr_block_in+10], 
    memory[ptr_block_in+11], 
    memory[ptr_block_in+12], 
    memory[ptr_block_in+13], 
    memory[ptr_block_in+14], 
    memory[ptr_block_in+15], 
    memory[ptr_block_in+16], 
    memory[ptr_block_in+17], 
    memory[ptr_block_in+18], 
    memory[ptr_block_in+19], 
    memory[ptr_block_in+20], 
    memory[ptr_block_in+21], 
    memory[ptr_block_in+22], 
    memory[ptr_block_in+23], 
    memory[ptr_block_in+24], 
    memory[ptr_block_in+25], 
    memory[ptr_block_in+26], 
    memory[ptr_block_in+27], 
    memory[ptr_block_in+28], 
    memory[ptr_block_in+29], 
    memory[ptr_block_in+30], 
    memory[ptr_block_in+31], 
    memory[ptr_block_in+32], 
    memory[ptr_block_in+33], 
    memory[ptr_block_in+34], 
    memory[ptr_block_in+35], 
    memory[ptr_block_in+36], 
    memory[ptr_block_in+37], 
    memory[ptr_block_in+38], 
    memory[ptr_block_in+39], 
    memory[ptr_block_in+40], 
    memory[ptr_block_in+41], 
    memory[ptr_block_in+42], 
    memory[ptr_block_in+43], 
    memory[ptr_block_in+44], 
    memory[ptr_block_in+45], 
    memory[ptr_block_in+46], 
    memory[ptr_block_in+47], 
    memory[ptr_block_in+48], 
    memory[ptr_block_in+49], 
    memory[ptr_block_in+50], 
    memory[ptr_block_in+51], 
    memory[ptr_block_in+52], 
    memory[ptr_block_in+53], 
    memory[ptr_block_in+54], 
    memory[ptr_block_in+55], 
    memory[ptr_block_in+56], 
    memory[ptr_block_in+57], 
    memory[ptr_block_in+58], 
    memory[ptr_block_in+59], 
    memory[ptr_block_in+60], 
    memory[ptr_block_in+61], 
    memory[ptr_block_in+62], 
    memory[ptr_block_in+63]} = in_block;
        end
        // $writememh("mem.hex",memory);
    end
endmodule
