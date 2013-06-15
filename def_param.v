module def_param;
	defparam 
	CPU.alu_rs[0].fuindex = 0,
	CPU.alu_rs[1].fuindex = 1,
	CPU.alu_rs[2].fuindex = 2,
	CPU.alu_rs[3].fuindex = 3,
	CPU.alu_rs[4].fuindex = 4,
	
	CPU.store_rs[0].fuindex = 5,	CPU.store_rs[0].StorerIndex = 0,
	CPU.store_rs[1].fuindex = 6,	CPU.store_rs[1].StorerIndex = 1,

	CPU.load_rs[0].fuindex = 7,
	CPU.load_rs[1].fuindex = 8,
	CPU.load_rs[2].fuindex = 9;
endmodule