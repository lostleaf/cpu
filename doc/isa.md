ISA
=====
##instructions
`add` `sub` `mul` `lw` `sw` `addi` `subi` `muli` `lwi` `swi` `li` `j` `jr` `bge` 

##add

calculate `op1+op2` and save the result into `dst`
###usage
```
add dst, op1, op2

dst: register
op1: register
op2: register
```
###binary
`0000_dst(5bits)_op1(5bits)_op2(5bits)_0(13bits)`


##sub
calculate `op1-op2` and save the result into `dst`
###usage
```
add dst, op1, op2

dst: register
op1: register
op2: register
```
###binary
`0001_dst(5bits)_op1(5bits)_op2(5bits)_0(13bits)`


##mul
calculate `op1*op2` and save the result into `dst`
###usage
```
mul dst, op1, op2

dst: register
op1: register
op2: register
```
###binary
`0010_dst(5bits)_op1(5bits)_op2(5bits)_0(13bits)`


##lw
load form address `base+offset` into `dst`
###usage
```
add dst, base, offset

dst: register
base: register
offset: register
```
###binary
`0011_dst(5bits)_op1(5bits)_op2(5bits)_0(13bits)`


##sw
save `dst` to address `base+offset`
###usage
```
add dst, base, offset

dst: register
base: register
offset: register
```
###binary
`0100_dst(5bits)_op1(5bits)_op2(5bits)_0(13bits)`


##addi
calculate `op1+op2` and save the result into `dst`
###usage
```
add dst, op1, op2

dst: register
op1: register
op2: immediate
```
###binary
`0101_dst(5bits)_op1(5bits)_op2(18bits)`

##subi
calculate `op1-op2` and save the result into `dst`
###usage
```
add dst, op1, op2

dst: register
op1: register
op2: immediate
```
###binary
`0110_dst(5bits)_op1(5bits)_op2(18bits)`


##muli
calculate `op1*op2` and save the result into `dst`
###usage
```
mul dst, op1, op2

dst: register
op1: register
op2: immediate
```
###binary
`0111_dst(5bits)_op1(5bits)_op2(18bits)`


##lwi
load form address `base+offset` into `dst`
###usage
```
add dst, base, offset

dst: register
base: register
offset: immediate
```
###binary
`1000_dst(5bits)_op1(5bits)_op2(18bits)`


##swi
save `dst` to address `base+offset`
###usage
```
add dst, base, offset

dst: register
base: register
offset: immediate
```
###binary
`1001_dst(5bits)_op1(5bits)_op2(18bits)`


##li
load an immediate into `dst`
###usage
```
li dst, imm

dst: register
imm: immediate
```
###binary
`1010_dst(5bits)_imm(23bits)`

##j
jump to a label
###usage
```
j label

label: label
```
###binary
`1011_pc-offset(28bits)`

##jr
jump to address of `dst`
###usage
```
jr dst

dst: register
```
###binary
`1100_dst(5bits)_0(23bits)`

##bge
branch if `op1>=op2`
###usage
```
bge op1, op2, label

op1: register
op2: immediate
label: label
```
###binary
`1101_op1(5bits)_op2(10bits)_pc-offset(13bits)`
