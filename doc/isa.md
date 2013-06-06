ISA
=====
##instructions
`add`

`sub`

`mul`

`lw`

`sw`

`addi`

`subi`

`muli`

`lwi`

`swi`

`li`

`j`

`jr`

`bge`

##add
calculate `op1+op2` and save the result into `dst`
###usage
```
add dst, op1, op2

dst: register
op1: register
op2: register
```

##sub
calculate `op1-op2` and save the result into `dst`
###usage
```
add dst, op1, op2

dst: register
op1: register
op2: register
```

##mul
calculate `op1*op2` and save the result into `dst`
###usage
```
mul dst, op1, op2

dst: register
op1: register
op2: register
```

##lw
load form address `base+offset` into `dst`
###usage
```
add dst, base, offset

dst: register
base: register
offset: register
```

##sw
save `dst` to address `base+offset`
###usage
```
add dst, base, offset

dst: register
base: register
offset: register
```

##addi
calculate `op1+op2` and save the result into `dst`
###usage
```
add dst, op1, op2

dst: register
op1: register
op2: immediate
```

##subi
calculate `op1-op2` and save the result into `dst`
###usage
```
add dst, op1, op2

dst: register
op1: register
op2: immediate
```

##muli
calculate `op1*op2` and save the result into `dst`
###usage
```
mul dst, op1, op2

dst: register
op1: register
op2: immediate
```

##lwi
load form address `base+offset` into `dst`
###usage
```
add dst, base, offset

dst: register
base: register
offset: immediate
```

##swi
save `dst` to address `base+offset`
###usage
```
add dst, base, offset

dst: register
base: register
offset: immediate
```
