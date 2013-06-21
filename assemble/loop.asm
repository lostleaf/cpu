li $t1, 10
start:
add $t2, $t2, $t1
subi $t1, $t1, 1
bge $t1, 1, start
halt