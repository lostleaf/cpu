parameter WORD_SIZE     = 32;
parameter MEM_SIZE      = 1024;
parameter REG_INDEX     = 5;
parameter REG_FILE_SIZE = 32;

parameter BLOCK_SIZE    = 16;
parameter CACHE_SIZE    = 1024;
parameter TAG_SIZE      = 18;

parameter ALU_ADD       = 4'h0;
parameter ALU_SUB       = 4'h1;
parameter ALU_MUL       = 4'h2;

// for INST
parameter INST_START         = 31;
parameter OPCODE_WIDTH       = 4;
parameter RS_START           = 22;
parameter RD_START           = 27;
parameter RT_START           = 17;
parameter IMM_START          = 17;
parameter J_PCOFFSET_START   = 27;
parameter BGE_IMM_START      = 22;
parameter BGE_PCOFFSET_START = 9;
parameter INST_ADD           = 4'h0;
parameter INST_SUB           = 4'h1;
parameter INST_MUL           = 4'h2;
parameter INST_LWRR          = 4'h3;
parameter INST_SWRR          = 4'h4;
parameter INST_ADDI          = 4'h5;
parameter INST_SUBI          = 4'h6;
parameter INST_MULI          = 4'h7;
parameter INST_LW            = 4'h8;
parameter INST_SW            = 4'h9;
parameter INST_LI            = 4'ha;
parameter INST_J             = 4'hb;
parameter INST_JR            = 4'hc;
parameter INST_BGE           = 4'hd;
//parameter INST_NOP		= 4'hf;

// for FU
parameter ADDER_NUM     = 4'd4;
parameter MULTER_NUM    = 4'd4;    // multiplier
parameter BRANCH_NUM	= 4'b1;
parameter STORER_NUM    = 4'd2;
parameter LOADER_NUM    = 4'd3;
parameter FU_NUM        = ADDER_NUM+MULTER_NUM+LOADER_NUM+STORER_NUM+BRANCH_NUM;
parameter ADDER_START	= 4'd0;
parameter MULTER_START	= ADDER_NUM;
parameter BRANCH_START	= MULTER_START+MULTER_NUM;
parameter STORER_START	= BRANCH_START+BRANCH_NUM;
parameter LOADER_START	= STORER_START+STORER_NUM;
parameter NO_FU			= 4'hf;
parameter FU_INDEX		= 4;

parameter MUL_STALL     = 3;
parameter MEM_STALL		= 100;

// for Reorder Buffer
parameter RB_SIZE       = 15;	// temporary
parameter RB_INDEX      = 4;
parameter READY         = 15;
parameter NULL          = READY;	// meaning an fu is not write ans to any CDB_bus	
