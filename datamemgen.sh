#!/bin/bash
i=3
entry="memory[ptr_block_in+"
for j in {0..63}
do
echo "	$entry$j], "
done

