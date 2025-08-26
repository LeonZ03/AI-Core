package Corprocessor

import chisel3._
import chisel3.util._
import chisel3.util.experimental.decode._

object MInsts0 {
  def MSETTYPE               = BitPat("b000000000000?????100?????1110111")
  def MSETTILEM              = BitPat("b000001000000?????101?????1110111")
  def MSETTILEK              = BitPat("b000001000000?????110?????1110111")
  def MSETTILEN              = BitPat("b000001000000?????100?????1110111")
  def MLAE8                  = BitPat("b0000010??????????0000????1110111")
  def MLBE8                  = BitPat("b0000100??????????0000????1110111")
  def MLCE32                 = BitPat("b0000000??????????0100????1110111")
  def MSCE32                 = BitPat("b0000001??????????0100????1110111")
}
import MInsts0._

object MDecodeTable0 {
                    //	     isMat   xrs2 isConfig frd
                    //         | xrs1 | frs1 | xrd  |
                    //         |   |  |  |   |  |   |
  val default =       BitPat("b0   0  0  0   0  0   0")
  val table = Seq(                                      
    MSETTYPE       -> BitPat("b1   1  0  0   1  1   0"),
    MSETTILEM      -> BitPat("b1   1  0  0   1  1   0"),
    MSETTILEK      -> BitPat("b1   1  0  0   1  1   0"),
    MSETTILEN      -> BitPat("b1   1  0  0   1  1   0"),
    MLAE8          -> BitPat("b1   1  1  0   0  0   0"),
    MLBE8          -> BitPat("b1   1  1  0   0  0   0"),
    MLCE32         -> BitPat("b1   1  1  0   0  0   0"),
    MSCE32         -> BitPat("b1   1  1  0   0  0   0"),
  )
}


class MDecodeSimple extends Module {
  val io = IO(new Bundle {
    val in = Input(UInt(32.W))

    val isMat = Output(Bool())
    val isConfig = Output(Bool())
    val xrs1 = Output(Bool())
    val xrs2 = Output(Bool())
    val frs1 = Output(Bool())
    val xrd = Output(Bool())
    val frd = Output(Bool())
  })

  val truthTable0 = TruthTable(MDecodeTable0.table, MDecodeTable0.default)
  val decoderOut0 = decoder(QMCMinimizer, io.in, truthTable0)
  
  io.isMat := decoderOut0(6)
  io.xrs1 := decoderOut0(5)
  io.xrs2 := decoderOut0(4)
  io.frs1 := decoderOut0(3)
  io.isConfig := decoderOut0(2)
  io.xrd := decoderOut0(1)
  io.frd := decoderOut0(0)
}