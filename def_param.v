module def_param;
	defparam 
	CPU.alu_rs[0].fuindex = 0,
	CPU.alu_rs[1].fuindex = 1,
	CPU.alu_rs[2].fuindex = 2,
	CPU.alu_rs[3].fuindex = 3,
	CPU.alu_rs[4].fuindex = 4,
	CPU.alu_rs[5].fuindex = 5,
	CPU.alu_rs[6].fuindex = 6,
	CPU.alu_rs[7].fuindex = 7,
	CPU.alu_rs[8].fuindex = 8,

	CPU.store_rs[0].fuindex = 9,	CPU.store_rs[0].StorerIndex = 0,
	CPU.store_rs[1].fuindex = 10,	CPU.store_rs[1].StorerIndex = 1,

	CPU.load_rs1.fuindex = 11, CPU.load_rs1.LoaderIndex = 0,
	CPU.load_rs2.fuindex = 12, CPU.load_rs2.LoaderIndex = 1,
	CPU.load_rs3.fuindex = 13, CPU.load_rs3.LoaderIndex = 2;
endmodule