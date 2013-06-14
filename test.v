module test;
initial begin:t
	reg[3:0] i;
	i = 0;
	$display("%d", 4'd15> 32'd14);
end
endmodule