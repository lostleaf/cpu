	.text
	.globl main
main:
	subi	$sp, $sp, 164
	addi	$fp, $sp, 164
	subi	$a2, $fp, 88
	subi	$a3, $fp, 112
	subi	$t0, $fp, 136
	li	$t1, 0
L1:
	bge	$t1, 2, L0
	li	$t2, 0
L4:
	bge	$t2, 3, L3
	li	$t3, 0
L7:
	bge	$t3, 2, L6
	muli	$t4, $t1, 12
	sub	$t4, $a2, $t4
	muli	$t5, $t2, 4
	muli	$t6, $t2, 8
	sub	$t6, $a3, $t6
	muli	$t7, $t3, 4
	lwrr	$k1, $t6, $t7
	lwrr	$k0, $t4, $t5
	mul	$t4, $k0, $k1
	muli	$t5, $t1, 8
	sub	$t5, $t0, $t5
	muli	$t6, $t3, 4
	lwrr	$k0, $t5, $t6
	add	$k0, $k0, $t4
	swrr	$k0, $t5, $t6
L8:
	addi	$t3, $t3, 1
	j	L7
L6:
L5:
	addi	$t2, $t2, 1
	j	L4
L3:
L2:
	addi	$t1, $t1, 1
	j	L1
L0:
	li	$v0, 0



	.data 0x10000000

