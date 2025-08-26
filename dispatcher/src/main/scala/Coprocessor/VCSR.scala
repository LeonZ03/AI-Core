package Corprocessor

import chisel3._
import chisel3.util._

object VParams {
  val xLen = 64
  val XLEN = xLen

  val VLEN = 2048  // Must be power of 2
  val vlenb = VLEN/8
  val bVL = log2Up(VLEN) + 1
  val bVstart = bVL - 1
}
import VParams._

class VCSRType extends Bundle {
  val vstart = UInt(bVstart.W)
  val vl = UInt(bVL.W)
  val vxrm = UInt(2.W)
  val frm = UInt(3.W)
  val vlmul = UInt(3.W)
  val vsew = UInt(3.W)
  val vill = Bool()
  val ma = Bool()
  val ta = Bool()
}

class VCSR extends Module {
  val io = IO(new Bundle {
    val instr = Input(UInt(32.W))
    val isConfig = Input(Bool()) // is config instruction
    val rs1 = Input(UInt(XLEN.W))
    val rs2 = Input(UInt(XLEN.W))
    val rs1_is_x0 = Input(Bool())
    val rd_is_x0 = Input(Bool())
    // output rd
    val rd = Output(UInt(XLEN.W))
    // vcsr values
    val csr = Output(new VCSRType)
  })

  val vcsr_regs = RegInit(0.U.asTypeOf(new VCSRType))

  val is_vsetvli  = io.instr(31, 30) === "b00".U && io.instr(14, 12) === "b111".U
  val is_vsetivli = io.instr(31, 30) === "b11".U && io.instr(14, 12) === "b111".U
  val is_vsetvl   = io.instr(31, 25) === "b1000000".U && io.instr(14, 12) === "b111".U

  val vtype_imm = Mux(is_vsetvli, io.instr(30, 20), io.instr(29, 20))
  val new_vlmul_imm = vtype_imm(2, 0)
  val new_vsew_imm = vtype_imm(5, 3)
  val new_vta_imm = vtype_imm(6)
  val new_vma_imm = vtype_imm(7)

  val new_vtype = Wire(new Bundle{
    val vlmul = UInt(3.W)
    val vsew = UInt(3.W)
    val ta = Bool()
    val ma = Bool()
  })

  when (is_vsetvl) {
    new_vtype.vlmul := io.rs2(2, 0)
    new_vtype.vsew  := io.rs2(5, 3)
    new_vtype.ta   := io.rs2(6)
    new_vtype.ma   := io.rs2(7)
  } .otherwise { // vsetvli or vsetivli
    new_vtype.vlmul := new_vlmul_imm
    new_vtype.vsew  := new_vsew_imm
    new_vtype.ta   := new_vta_imm
    new_vtype.ma   := new_vma_imm
  }

  val avl = Wire(UInt(XLEN.W))
  val uimm = io.instr(19, 15)

  when (is_vsetivli) {
    avl := uimm
  } .otherwise { // vsetvli or vsetvl
    when (!io.rd_is_x0 && !io.rs1_is_x0) {
      avl := io.rs1
    } .elsewhen (!io.rd_is_x0 && io.rs1_is_x0) {
      avl := (-1.S(XLEN.W)).asUInt
    } .otherwise { // rd_is_x0 && rs1_is_x0
      avl := vcsr_regs.vl
    }
  }

  val vlen_div_sew = (VLEN.U >> (new_vtype.vsew + 3.U))

  val vlmax = Wire(UInt(bVL.W))

  when (new_vtype.vlmul(2)) { // Fractional LMUL
    val shift_amt = MuxCase(0.U(3.W), Seq(
      (new_vtype.vlmul === "b111".U) -> 1.U(3.W),
      (new_vtype.vlmul === "b110".U) -> 2.U(3.W),
      (new_vtype.vlmul === "b101".U) -> 3.U(3.W)
    ))
    vlmax := vlen_div_sew >> shift_amt
  } .otherwise { // Integer LMUL
    vlmax := vlen_div_sew << new_vtype.vlmul(1, 0)
  }

  val new_vl = Mux(avl > vlmax, vlmax, avl)

  when (io.isConfig) {
    vcsr_regs.vlmul  := new_vtype.vlmul
    vcsr_regs.vsew   := new_vtype.vsew
    vcsr_regs.ta     := new_vtype.ta
    vcsr_regs.ma     := new_vtype.ma
    vcsr_regs.vl     := new_vl
    vcsr_regs.vstart := 0.U
    vcsr_regs.vill   := false.B // Gen in vpu
  }

  io.rd := Mux(io.isConfig, new_vl, 0.U)
  io.csr := vcsr_regs
}
