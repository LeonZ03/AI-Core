package Corprocessor

import chisel3._
import chisel3.util._

class Corprocessor(params: CvxifParams = new CvxifParams) extends Module {
  val io = IO(new Bundle {
    val issue_valid   = Input(Bool())
    val issue_req     = Input(new XIssueReq(params.X_HARTID_WIDTH, params.X_ID_WIDTH))
    val register_data = Input(Vec(params.X_NUM_RS, UInt(params.X_RFR_WIDTH.W)))
    
    val isConfig      = Input(Bool())
    val isVec         = Input(Bool())
    val isMat         = Input(Bool())

    val result_valid  = Output(Bool())
    val result        = Output(new XResult(params.X_HARTID_WIDTH, params.X_ID_WIDTH, params.X_DUALWRITE, params.X_RFW_WIDTH))
  })

  // input
  val issue_valid   = io.issue_valid
  val issue_req     = io.issue_req
  val register_data = io.register_data
  val isConfig      = io.isConfig
  val isVec         = io.isVec
  val isMat         = io.isMat
 
  // temp
  val result_valid  = RegInit(false.B)
  val result = RegInit(0.U.asTypeOf(new XResult(params.X_HARTID_WIDTH, params.X_ID_WIDTH, params.X_DUALWRITE, params.X_RFW_WIDTH)))

  // output
  io.result_valid  := result_valid
  io.result        := Mux(result_valid, result, 0.U.asTypeOf(new XResult(params.X_HARTID_WIDTH, params.X_ID_WIDTH, params.X_DUALWRITE, params.X_RFW_WIDTH)))

  // 模块
  val MCSR = Module(new MCSR)
  MCSR.io.in := issue_req.instr
  MCSR.io.isConfig := isConfig && isMat && issue_valid
  MCSR.io.xrs1 := register_data(0)

  // 其它字段
  result_valid  := issue_valid
  result.hartid := issue_req.hartid
  result.id     := issue_req.id
  result.rd     := issue_req.instr(11, 7)
  result.we     := MCSR.io.isConfig
  result.data   := MCSR.io.xrd
}