parameter WORD_SIZE     = 32;
parameter MEM_SIZE      = 1024;
parameter REG_INDEX 	= 5;
parameter REG_FILE_SIZE= 32;

parameter BLOCK_SIZE    = 16;
parameter CACHE_SIZE    = 1024;
parameter TAG_SIZE      = 18;

parameter ALU_ADD       = 4'h0;
parameter ALU_SUB       = 4'h1;
parameter ALU_MUL       = 4'h2;

parameter INST_ADD      = 4'h0;
parameter INST_SUB      = 4'h1;
parameter INST_MUL      = 4'h2;
parameter INST_LW       = 4'h3;
parameter INST_SW       = 4'h4;
parameter INST_ADDI     = 4'h5;
parameter INST_SUBI     = 4'h6;
parameter INST_MULI     = 4'h7;
parameter INST_LWI      = 4'h8;
parameter INST_SWI      = 4'h9;
parameter INST_LI       = 4'ha;
parameter INST_J        = 4'hb;
parameter INST_JR       = 4'hc;
parameter INST_BGE      = 4'hd;

// for FU
parameter ADDER_NUM		= 3;
parameter MULTER_NUM	= 2;	// multiplier
parameter LOADER_NUM	= 3;
parameter STORER_NUM	= 2;
parameter FU_NUM		= ADDER_NUM+MULTER_NUM+LOADER_NUM+STORER_NUM;
parameter FU_INDEX		= 4;
parameter READY			= 15;

parameter MUL_STALL		= 4;

// for Reorder Buffer
parameter RB_SZIE		= 16;	// temporary
parameter RB_INDEX		= 4;