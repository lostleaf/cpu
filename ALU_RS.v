module ALU_RS(fu, RB_index, instr, vj, vk, qj, qk, result, valid, reg_numj, reg_numk, busy, reset, clk);
    `include "parameters.v"
    parameter op = ALU_ADD, stall = 1;      // able to be changed from outside

    input   wire[FU_INDEX-1:0]  fu;
    input   wire[RB_INDEX-1:0]  RB_index;
    input   wire[WORD_SIZE-1:0] instr;

    input   wire[WORD_SIZE-1:0] vj, vk;
    input   wire[FU_INDEX-1:0]  qj, qk;
    output  reg [REG_INDEX-1:0] reg_numj, reg_numk;
    
    output  reg [WORD_SIZE-1:0] result;
    output  reg                 valid, busy, reset, clk;


endmodule