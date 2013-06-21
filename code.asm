main:
addi $a0, $fp, 0
addi $a1, $fp, 6
addi $a2, $fp, 12
li $a3, 0               #a3 = i
L1:
bge $a3, 2, L0          
li $t0, 0               #t0 = j
L4:
bge $t0, 3, L3
li $t1, 0               #t1 = k
L7:
bge $t1, 2, L6
muli $t2, $a3, 3        
add $t2, $a0, $t2       #t2 = a0 + i*3
add $t3, $t0, $t0       
add $t3, $a1, $t3       #t3 = a1 + j*2
lwrr $t3, $t3, $t1      #t3 = [a1+j*2+k]  (a1[j][k])
lwrr $t2, $t2, $t0      #t2 = [a0+i*3+j]  (a0[i][j])
mul $t2, $t2, $t3       #t2 = t2*t3
add $t3, $a3, $a3       
add $t3, $a2, $t3       #t3 = a2+i*2
lwrr $t4, $t3, $t1      #t4 = [a2+i*2+k]  (a2[i][k])
add $t2, $t4, $t2       #t2 = t2+t4
swrr $t2, $t3, $t1      #[a2+i*2+k] = t2
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