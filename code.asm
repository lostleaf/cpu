#   .text
#   .globl main
main:
    subi    $sp, $sp, 168
    addi    $fp, $sp, 168
    subi    $a1, $fp, 92
    subi    $a2, $fp, 116
    subi    $a3, $fp, 140
    li  $t0, 0
L1:
    bge $t0, 2, L0
    li  $t1, 0
L4:
    bge $t1, 3, L3
    li  $t2, 0
L7:
    bge $t2, 2, L6
    muli    $t3, $t0, 12
    sub $t3, $a1, $t3
    muli    $t4, $t1, 4
    sub $t3, $t3, $t4
    muli    $t4, $t1, 8
    sub $t4, $a2, $t4
    muli    $t5, $t2, 4
    sub $t4, $t4, $t5
    lw  $k0, 0($t3)
    lw  $k1, 0($t4)
    mul $t3, $k0, $k1
    muli    $t4, $t0, 8
    sub $t4, $a3, $t4
    muli    $t5, $t2, 4
    sub $t4, $t4, $t5
    lw  $k0, 0($t4)
    add $k0, $k0, $t3
    sw  $k0, 0($t4)
L8:
    addi    $t2, $t2, 1
    j   L7
L6:
L5:
    addi    $t1, $t1, 1
    j   L4
L3:
L2:
    addi    $t0, $t0, 1
    j   L1
L0:
    li  $v0, 0



    #.data 0x10000000

