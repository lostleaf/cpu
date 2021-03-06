`include "timescale.v"

`include "ALU_RS.v"
`include "store_RS.v"
`include "def_param.v"
`include "reg_status.v"
`include "CDB_data_controller.v"
`include "reorder_buffer.v"
`include "load_RS.v"
`include "data_cache.v"
`include "data_memory.v"
`include "branch_RS.v"
module CPU;
    `include "parameters.v"

    //CDB_data RB_INDEX lanes
    wire[WORD_SIZE*RB_SIZE-1:0] CDB_data_data = 'bz;
    wire[RB_SIZE-1:0]           CDB_data_valid = 'bz;
    wire[WORD_SIZE*RB_SIZE-1:0] CDB_data_addr = 'bz;

    wire[FU_NUM*WORD_SIZE-1:0]  FU_data_bus;
    wire[FU_NUM-1:0]            FU_valid_bus;
    wire[STORER_NUM*WORD_SIZE-1:0]  FU_addr_bus;
    wire[FU_NUM*RB_INDEX-1:0]   FU_RB_index_bus;

    //CDB_inst 1 lane
    wire[FU_INDEX-1:0]  CDB_inst_fu = 'bz;
    wire[WORD_SIZE-1:0] CDB_inst_inst = 'bz;
    wire[RB_INDEX-1:0]  CDB_inst_RBindex = 'bz;

    wire[WORD_SIZE-1:0] vi, vj, vk;
    wire[RB_INDEX-1:0]  qi, qj, qk, qRB;
    wire[REG_INDEX-1:0] numi = 'bz, numj = 'bz, numk = 'bz, numRB = 'bz;

    wire[FU_NUM-1:0]    busy;

    // from RB to FU
    reg [FU_NUM-1:0]    write;

    reg clk, reset;         // reset RB, reg_file, CDB_controller, and reg_status
    wire[FU_NUM-1:0] reset_bus; // reset FUs

    // not done
    // for reg
    wire we_reg, we_status1, we_status2;
    wire[REG_INDEX-1:0] ws_reg, ws_status1, ws_status2;
    wire[WORD_SIZE-1:0] wd_reg;
    wire[RB_INDEX-1:0]  wd_status1, wd_status2;
    // for CDB_inst
    reg[FU_INDEX-1:0]   fu;
    wire[WORD_SIZE-1:0] inst;
    reg[RB_INDEX-1:0]   RBindex;

    //for inst
    reg[OPCODE_WIDTH-1:0]   op; 
    reg[REG_INDEX-1:0]      rs, rt, rd;

    // within RB
    reg[FU_INDEX-1:0]   RB_wt_by_FU[RB_SIZE-1:0];       //RB entry written by FU

    // from reorder buffer to datacache
    wire we_dcache;
    wire[WORD_SIZE-1:0] wd_dcache;
    wire[WORD_SIZE-1:0] ws_dcache;

    //for data cache
    wire cache_read_enable1, cache_read_enable2, cache_read_enable3, cache_write_enable;
    wire [WORD_SIZE-1:0] cache_ptr_read1, cache_ptr_read2, cache_ptr_read3;
    wire [WORD_SIZE-1:0] cache_ptr_write, cache_val;

    wire [WORD_SIZE-1:0] cache_out1, cache_out2, cache_out3;
    wire cache_hit_read1, cache_hit_read2, cache_hit_read3, cache_hit_write;

    data_cache dcache(cache_ptr_read1, cache_ptr_read2, cache_ptr_read3, 
                      cache_out1,      cache_out2,      cache_out3, 
                      cache_hit_read1, cache_hit_read2, cache_hit_read3, 
                      cache_read_enable1, cache_read_enable2, cache_read_enable3,
                      cache_ptr_write, cache_val, cache_write_enable, cache_hit_write,
                      clk);

    reg_status status(.get_num1(numi), .get_num2(numj), .get_num3(numk), .get_numRB(numRB), 
        .value1(vi), .value2(vj), .value3(vk), 
        .status1(qi), .status2(qj), .status3(qk), .statusRB(qRB),
        .write_reg_src(ws_reg), .write_reg_data(wd_reg), .write_reg_enable(we_reg), 
        .write_rs_src1(ws_status1), .write_rs_status1(wd_status1), 
        .write_rs_enable1(we_status1),
        .write_rs_src2(ws_status2), .write_rs_status2(wd_status2), 
        .write_rs_enable2(we_status2),
        .reset(reset), .clk(clk));
    
    ALU_RS alu_rs[ADDER_NUM+MULTER_NUM-1:0](.fu(CDB_inst_fu), 
        .RB_index(CDB_inst_RBindex), .inst(CDB_inst_inst), .vj(vj), .vk(vk), 
        .qj(qj), .qk(qk), .reg_numj(numj), .reg_numk(numk), .busy_out(busy), 
        .CDB_data_data(CDB_data_data), .CDB_data_valid(CDB_data_valid), 
        .data_bus(FU_data_bus), .valid_bus(FU_valid_bus), .RB_index_bus(FU_RB_index_bus),
        .reset_bus(reset_bus), .clk(clk));

    store_RS store_rs[STORER_NUM-1:0](.fu(CDB_inst_fu), .RB_index(CDB_inst_RBindex), 
        .inst(CDB_inst_inst), 
        .vi(vi), .vj(vj), .vk(vk), 
        .qi(qi), .qj(qj), .qk(qk), 
        .reg_numi(numi), .reg_numj(numj), .reg_numk(numk), .busy_out(busy), 
        .CDB_data_data(CDB_data_data), .CDB_data_valid(CDB_data_valid), 
        .data_bus(FU_data_bus), .valid_bus(FU_valid_bus), .addr_bus(FU_addr_bus),
        .RB_index_bus(FU_RB_index_bus),.reset_bus(reset_bus), .clk(clk));

    load_RS load_rs1(.fu(CDB_inst_fu), .RB_index(CDB_inst_RBindex), .inst(CDB_inst_inst),
        .vj(vj), .vk(vk), .qj(qj), .qk(qk),                  
        .reg_numj(numj), .reg_numk(numk), .busy_out(busy), 
        .CDB_data_data(CDB_data_data), .CDB_data_valid(CDB_data_valid),
        .data_bus(FU_data_bus), .valid_bus(FU_valid_bus), .RB_index_bus(FU_RB_index_bus), 
        .reset_bus(reset_bus), .clk(clk),
        .c_ptr(cache_ptr_read1), .c_out(cache_out1), .c_hit(cache_hit_read1),
        .c_read_enable(cache_read_enable1)
        );    

    load_RS load_rs2(.fu(CDB_inst_fu), .RB_index(CDB_inst_RBindex), .inst(CDB_inst_inst),
        .vj(vj), .vk(vk), .qj(qj), .qk(qk),                  
        .reg_numj(numj), .reg_numk(numk), .busy_out(busy), 
        .CDB_data_data(CDB_data_data), .CDB_data_valid(CDB_data_valid),
        .data_bus(FU_data_bus), .valid_bus(FU_valid_bus), .RB_index_bus(FU_RB_index_bus), 
        .reset_bus(reset_bus), .clk(clk),
        .c_ptr(cache_ptr_read2), .c_out(cache_out2), .c_hit(cache_hit_read2),
        .c_read_enable(cache_read_enable2)
        );    

    load_RS load_rs3(.fu(CDB_inst_fu), .RB_index(CDB_inst_RBindex), .inst(CDB_inst_inst),
        .vj(vj), .vk(vk), .qj(qj), .qk(qk),                  
        .reg_numj(numj), .reg_numk(numk), .busy_out(busy), 
        .CDB_data_data(CDB_data_data), .CDB_data_valid(CDB_data_valid),
        .data_bus(FU_data_bus), .valid_bus(FU_valid_bus), .RB_index_bus(FU_RB_index_bus), 
        .reset_bus(reset_bus), .clk(clk),
        .c_ptr(cache_ptr_read3), .c_out(cache_out3), .c_hit(cache_hit_read3),
        .c_read_enable(cache_read_enable3)
        );

    branch_RS branch_rs(.fu(CDB_inst_fu), .RB_index(CDB_inst_RBindex), .inst(CDB_inst_inst),
        .vj(vj), .vk(vk), .qj(qj), .qk(qk),                  
        .reg_numj(numj), .reg_numk(numk), .busy_out(busy), 
        .CDB_data_data(CDB_data_data), .CDB_data_valid(CDB_data_valid),
        .data_bus(FU_data_bus), .valid_bus(FU_valid_bus), .RB_index_bus(FU_RB_index_bus), 
        .reset_bus(reset_bus), .clk(clk)
        );

    CDB_data_controller data_ctrl(.CDB_data_data(CDB_data_data), 
        .CDB_data_valid(CDB_data_valid), .CDB_data_addr(CDB_data_addr),
        .data_bus(FU_data_bus), .valid_bus(FU_valid_bus), .addr_bus(FU_addr_bus),
        .RB_index_bus(FU_RB_index_bus), .reset(reset), .clk(clk));

    reorder_buffer RB(.CDB_data_data(CDB_data_data), 
        .CDB_data_valid(CDB_data_valid), .CDB_data_addr(CDB_data_addr), 
        .busy(busy), .we_reg(we_reg), .wd_reg(wd_reg), .ws_reg(ws_reg), 
        .we_mem(cache_write_enable), .wd_mem(cache_val), .ws_mem(cache_ptr_write), 
        //.numj(numj), .numk(numk),
        //.vj(vj), .vk(vk), .qj(qj), .qk(qk),
        .numRB(numRB), .qRB(qRB), 
        .CDB_inst_fu(CDB_inst_fu), .CDB_inst_inst(CDB_inst_inst), 
        .CDB_inst_RBindex(CDB_inst_RBindex), 
        .Rdest_status_issue(ws_status1), .RB_index_status_issue(wd_status1), 
        .we_status_issue(we_status1),
        .Rdest_status_wb(ws_status2),    .RB_index_status_wb(wd_status2),    
        .we_status_wb(we_status2),
        .reset_out(reset_bus),
        .reset(reset), .clk(clk));

    always begin
        #0.5 clk = 0;
        #0.5 clk = 1;
    end


    // for test
    initial begin:test

        reset = 1;
        #1 reset = 0;

        // #1000 $finish;
    end

    task setWriteBy;
        inout [FU_INDEX-1:0]    writeFU;
        inout [FU_NUM-1:0]      write;
        input [FU_INDEX-1:0]    fu;
        begin
            if (writeFU != NULL) begin
                setWrite(write, writeFU, 1'b0);
            end else begin end
            setWrite(write, fu, 1'b1);
            writeFU = fu;
        end
    endtask

    task setWrite;
        inout [FU_NUM-1:0]      write;
        input [FU_INDEX-1:0]    fu;
        input data;

        reg[FU_NUM-1:0] longData, mask;
        begin
            mask = ~(1'b1<<fu);
            longData = data<<fu;
            write =  (write&mask)|longData;
        end
    endtask

endmodule