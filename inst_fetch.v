module inst_fetch(out, ptr, clk, fetch_enable, busy);

    `include "parameters.v"

    input wire [WORD_SIZE-1:0] ptr;
    input wire clk, fetch_enable;
    output reg [WORD_SIZE-1:0] out;
    output reg busy = 0, cache_enable = 0;

    wire [WORD_SIZE-1:0] inst;
    reg  [WORD_SIZE-1:0] p;
    wire hit;

    inst_cache cache(inst, clk, ptr, hit, cache_enable);

    always @(posedge clk) begin
        // p = ptr;
        $display("time = %t, hit = %b", $time, hit);
        if (fetch_enable) begin
            cache_enable = 1;
            busy = 1;
            if (!hit) begin
                #100 out = inst;
            end else begin
                out = inst;
            end
            busy = 0;
        end else begin
            cache_enable = 0;
        end
    end

endmodule
