	.text
	.globl main
main:
	addi	$a0, $fp, 0
	addi	$a1, $fp, 6
	addi	$a2, $fp, 12
	li	$a3, 0
L1:
	bge	$a3, 2, L0
	li	$t0, 0
L4:
	bge	$t0, 3, L3
	li	$t1, 0
L7:
	bge	$t1, 2, L6
	muli	$t2, $a3, 3
	add	$t3, $t0, $t0
	add	$t3, $a1, $t3
	lwrr	$t3, $t3, $t1
	add	$t2, $a0, $t2
	lwrr	$t2, $t2, $t0
	add	$t4, $a3, $a3
	add	$t4, $a2, $t4
	lwrr	$t5, $t4, $t1
	mul	$t2, $t2, $t3
	add	$t2, $t5, $t2
	swrr	$t2, $t4, $t1
L8:
	addi	$t1, $t1, 1
	j	L7
L6:
L5:
	addi	$t0, $t0, 1
	j	L4
L3:
L2:
	addi	$a3, $a3, 1
	j	L1
L0:

	.data 0x10000000
