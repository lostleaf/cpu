`include "timescale.v"
module load_RS(fu, RB_index, inst, vj, vk,  qj, qk,                  
    reg_numj, reg_numk, busy_out, CDB_data_data, CDB_data_valid,
    data_bus, valid_bus, RB_index_bus, reset, clk);

    `include "parameters.v"
    parameter fuindex = 0, LoaderIndex = 0;     
    // it's op depends on the design outside on which fuindex corresponds to which op, 
    //and the info is in the <"fu", RB_index, "inst">

    // from CDB_inst
    input   wire    [FU_INDEX-1:0]              fu;
    input   wire    [RB_INDEX-1:0]              RB_index;
    input   wire    [WORD_SIZE-1:0]             inst;
    //from CDB_data
    input   wire    [WORD_SIZE*RB_SIZE-1:0]     CDB_data_data;
    input   wire    [RB_SIZE-1:0]               CDB_data_valid;
            
    output  wire    [FU_NUM*WORD_SIZE-1:0]      data_bus;
    output  wire    [FU_NUM-1:0]                valid_bus;
    output  wire    [STORER_NUM*WORD_SIZE-1:0]  addr_bus;
    output  wire    [FU_NUM*RB_INDEX-1:0]       RB_index_bus;
    

    input   wire    [WORD_SIZE-1:0]             vj, vk;
    input   wire    [RB_INDEX-1:0]              qj, qk; 
    output  reg     [REG_INDEX-1:0]             reg_numj = 'bz, reg_numk = 'bz;
    // from RB

    input   wire                reset, clk;
    output  wire[FU_NUM-1:0]    busy_out;
    
    reg     [WORD_SIZE-1:0]     Vj, Vk, result = 0;
    reg     [RB_INDEX-1:0]      Qj, Qk, dest;
    reg     [OPCODE_WIDTH-1:0]  op;
    reg                         busy, valid;
    
    assign busy_out[fuindex]                                            = busy;
    assign addr_bus[(StorerIndex+1)*WORD_SIZE-1: StorerIndex*WORD_SIZE] = result;
    assign valid_bus[fuindex]                                           = valid;
    assign data_bus[(fuindex+1)*WORD_SIZE-1:fuindex*WORD_SIZE]          = data;
    assign RB_index_bus[(fuindex+1)*RB_INDEX-1:fuindex*RB_INDEX]        = dest;

    always @(posedge clk or posedge reset) begin
        if (reset) begin
            busy <= 1'b0;
            dest <= NULL;
            valid <= 1'b0;
            result <= 'b0;
        end else if (!busy) begin: checkIssue
                #0.1 if (fu == fuindex) begin
                        $display($realtime, "%m : %d receive inst:%b", fuindex, inst);
                        busy  <= 1'b1;
                        dest  <= RB_index;
                        op = inst[WORD_SIZE-1:WORD_SIZE-OPCODE_WIDTH];

                        reg_numj = inst[RS_START: RS_START-REG_INDEX+1];
                        // getData(vi, qi, Vi, Qi, CDB_data_data, CDB_data_valid);
                        getData(vj, qj, Vj, Qj, CDB_data_data, CDB_data_valid);
                        if (op === INST_LWRR) begin
                            reg_numk = inst[RT_START: RT_START-REG_INDEX+1];
                            getData(vk, qk, Vk, Qk, CDB_data_data, CDB_data_valid);
                        end
                        else begin
                            Vk = inst[IMM_START:0];
                            Qk = READY;
                        end
                        
                        #0.1;
                        reg_numj = 'bz;
                        reg_numk = 'bz;

                        valid <= 1'b0;
                    end else begin
                    end
            end else begin: execute
                reg ok;
                ok = 1'b1;
                checkAndGetData(Qj, Vj, CDB_data_data, CDB_data_valid, ok);
                checkAndGetData(Qk, Vk, CDB_data_data, CDB_data_valid, ok);
                if (ok) begin
                    #0.1 result = Vj-Vk;
                    data = Vi;
                    valid = 1'b1;
                    busy = 1'b0;
                    #1.3 valid = 0'b0;
                    dest = NULL;
                end
                else begin end
            end
    end
    
    task getData;   //(v, q, CDB_data_data, CDB_data_valid, V, Q)
        input[WORD_SIZE-1:0] v;
        input[RB_INDEX-1:0]  q;
        output  reg [WORD_SIZE-1:0] V;
        output  reg [RB_INDEX-1:0]  Q;
        input[WORD_SIZE*RB_SIZE-1:0]    CDB_data_data;
        input[RB_SIZE-1:0]              CDB_data_valid;
        begin
            if (q === READY) begin
                V = v;
                Q = READY;
            end else if (readValidBus(CDB_data_valid, q)) begin
                        V = readDataBus(CDB_data_data, q);
                        Q = READY;
                    end
            else Q = q;
        end
    endtask

    function[WORD_SIZE-1:0] readDataBus;
        input[WORD_SIZE*RB_SIZE-1:0] CDB_data_data;
        input[RB_INDEX-1:0]          index;       
        begin
            readDataBus = CDB_data_data>>(index*WORD_SIZE);
        end
    endfunction

    function readValidBus;
        input[RB_SIZE-1:0]  CDB_data_valid;
        input[RB_INDEX-1:0] index;        
        begin
            readValidBus = CDB_data_valid>>index;
        end
    endfunction

    task checkAndGetData;   //(Qj, Vj, CDB_data_data, CDB_data_valid, ok);
        inout [RB_INDEX-1:0]    Q;                  //!!inout, not ouput
        inout [WORD_SIZE-1:0]   V;
        input [WORD_SIZE*RB_SIZE-1:0]   dataBus;
        input [RB_SIZE-1:0]             validBus;
        inout ok;
        reg valid;
        begin
            //$display("<Q:%g, V:%g>", Q, V);
            if (Q === READY) begin end
            else begin 
                valid = readValidBus(validBus, Q);
                if (valid) begin
                    Q = READY;
                    V = readDataBus(dataBus, Q);
                end else ok = 1'b0;
            end
        end
    endtask

endmodule