package Corprocessor

import chisel3._
import chisel3.util._
import chisel3.util.experimental.decode._

object MCSRNames {
    def MTYPE   = 0
    def MTILEM  = 1
    def MTILEN  = 2
    def MTILEK  = 3
    def MLENB   = 4
    def MRLENB  = 5
    def MAMUL   = 6
    def MSTART  = 7
    def MCSR    = 8
}

object MInsts1 {
  def MSETTYPE               = BitPat("b000000000000?????100?????1110111")
  def MSETTILEM              = BitPat("b000001000000?????101?????1110111")
  def MSETTILEN              = BitPat("b000001000000?????100?????1110111")
  def MSETTILEK              = BitPat("b000001000000?????110?????1110111")
}
import MInsts1._

object MDecodeTable1 {
                    //	   MSETTILEK MSETTILEN MSETTILEM MSETTYPE
                    //         |         |         |         |
  val default =       BitPat("b0         0         0         0")
  val table = Seq(                 
    MSETTYPE       -> BitPat("b0         0         0         1"),
    MSETTILEM      -> BitPat("b0         0         1         0"),
    MSETTILEN      -> BitPat("b0         1         0         0"),
    MSETTILEK      -> BitPat("b1         0         0         0"),
  )
}


class MCSR extends Module{
    val io = IO(new Bundle {
        val in = Input(UInt(32.W))
        val isConfig = Input(Bool())
        val xrs1 = Input(UInt(32.W))
        val xrd  = Output(UInt(32.W))
    })

    val m_max = 8
    val n_max = 8
    val k_max = 8

    val instr = io.in
    
    //寄存器向量
    val csr = RegInit(VecInit(Seq(
        0.U(32.W), //mtype
        0.U(32.W), //mtilem
        0.U(32.W), //mtilek
        0.U(32.W), //mtilen
        64.U(32.W), //mlenb
        8.U(32.W), //mrlenb
        4.U(32.W), //mamul
        0.U(32.W), //mstart
        0.U(32.W)  //mcsr
    )))

    //译码
    val truthTable = TruthTable(MDecodeTable1.table, MDecodeTable1.default)
    val decoderOut = decoder(QMCMinimizer, io.in, truthTable)

    //根据不同索引，读写csr寄存器
    io.xrd := 0.U
    when(io.isConfig) {
        when(decoderOut(MCSRNames.MTYPE)) {
            csr(MCSRNames.MTYPE) := io.xrs1
            io.xrd := csr(MCSRNames.MTYPE)
        }.elsewhen(decoderOut(MCSRNames.MTILEM)) {
            when(io.xrs1 <= m_max.U) {
                csr(MCSRNames.MTILEM) := io.xrs1
                io.xrd := io.xrs1
            } .otherwise {
                csr(MCSRNames.MTILEM) := m_max.U
                io.xrd := m_max.U
            }
        }.elsewhen(decoderOut(MCSRNames.MTILEN)) {
            when(io.xrs1 <= n_max.U) {
                csr(MCSRNames.MTILEN) := io.xrs1
                io.xrd := io.xrs1
            } .otherwise {
                csr(MCSRNames.MTILEN) := n_max.U
                io.xrd := n_max.U
            }
        }.elsewhen(decoderOut(MCSRNames.MTILEK)) {
            when(io.xrs1 <= k_max.U) {
                csr(MCSRNames.MTILEK) := io.xrs1
                io.xrd := io.xrs1
            } .otherwise {
                csr(MCSRNames.MTILEK) := k_max.U
                io.xrd := k_max.U
            }
        }
    }
}