#!/bin/bash

CC="java -jar compiler.jar"
$CC code.c 1>code.asm 2>/dev/null
