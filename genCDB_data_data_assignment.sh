#!/bin/bash
for ((i = 0; i < 15; i = i+1))
do
	echo "always @(posedge readValidBus(CDB_data_valid,$i)) begin"
	echo "	RB_data_valid[$i] <= 1'b1;"
	echo "	RB_data[$i] <= readDataBus(CDB_data_data, $i);"
	echo "	if (RB_to_mem[$i])"
	echo "		RB_addr[$i] <= readDataBus(CDB_data_addr, $i);"
	echo "	else begin end"
	echo "end"
done

