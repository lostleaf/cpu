main:
	addi	$a0, $fp, 0
	addi	$a1, $fp, 900
	addi	$a2, $fp, 1800
	li	$a3, 0
L1:
	bge	$a3, 30, L0
	li	$t0, 0
L4:
	bge	$t0, 30, L3
	li	$t1, 0
L7:
	bge	$t1, 30, L6
	muli	$t2, $t0, 30
	muli	$t3, $a3, 30
	muli	$t4, $a3, 30
	add	$t2, $a1, $t2
	lwrr	$t2, $t2, $t1
	add	$t3, $a0, $t3
	lwrr	$t3, $t3, $t0
	add	$t4, $a2, $t4
	lwrr	$t5, $t4, $t1
	mul	$t2, $t3, $t2
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
	halt
