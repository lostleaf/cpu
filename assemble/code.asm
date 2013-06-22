main:
addi $a0, $fp, 0
addi $a1, $fp, 6
addi $a2, $fp, 12
li $a3, 0
L1:
bge $a3, 2, L0
li $t0, 0
L4:
bge $t0, 3, L3
li $t1, 0
L7:
bge $t1, 2, L6
muli $t2, $a3, 3
add $s5, $a0, $t2
add $t3, $t0, $t0
add $t4, $a1, $t3
lwrr $t5, $t4, $t1
lwrr $t6, $s5, $t0
mul $t7, $t5, $t6
add $s1, $a3, $a3
add $s2, $a2, $s1
lwrr $s3, $s2, $t1
add $s4, $s3, $t7
swrr $s4, $s2, $t1
L8:
addi $t1, $t1, 1
j L7
L6:
L5:
addi $t0, $t0, 1
j L4
L3:
L2:
addi $a3, $a3, 1
j L1
L0: