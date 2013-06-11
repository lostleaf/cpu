module inst_fetch(out, ptr, clk, fetch_enable, busy);

    `include "parameters.v"

    input wire [WORD_SIZE-1:0] ptr;
    input wire clk, fetch_enable;
    output reg [WORD_SIZE-1:0] out;
    output reg busy = 0;

    wire [WORD_SIZE-1:0] inst;
    reg  [WORD_SIZE-1:0] p;
    wire hit, cache_enable;

    inst_cache cache(inst, clk, ptr, hit, cache_enable);

    assign cache_enable = fetch_enable;

    always @(posedge clk) begin
        #1;
        if (fetch_enable) begin
            // busy = 1;
            // if (!hit) begin
            //     #100 out = inst;
            // end else begin
            //     out = inst;
            // end
            // busy = 0;
            if (!hit) begin
                $display("miss");
            end
            out <= inst;
        end
    end

endmodule
