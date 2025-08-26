# See LICENSE for license details.

#*****************************************************************************
# Copyright 2022 Thales DIS design services SAS
#
# Licensed under the Solderpad Hardware Licence, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# SPDX-License-Identifier: Apache-2.0 WITH SHL-2.0
# You may obtain a copy of the License at https:#solderpad.org/licenses/
#
#----------------------------------------------------------------------------------

#include "cvxif_macros.h"

  #-------------------------------------------------------------
  # Custom tests
  #-------------------------------------------------------------

  .globl main
main:

# core of the test

start0:
    li  a0, 1
    li  a1, 2
    #cus_add a0, a0, a1
    #.word 0b00000000000000000000000001111011
    .word 0b00000100000001011101010111110111
    
    # 延时循环（等待一段时间）
    #li   t0, 1000       # 延时计数（数值越大延时越长）
#delay_loop:
    #addi t0, t0, -1       # 计数减 1
    #bnez t0, delay_loop   # 如果还没数到 0，继续循环

    li a0, 0x0
    jal exit


