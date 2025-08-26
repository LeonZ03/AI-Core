package Corprocessor

import chisel3._
import chisel3.util._

class cvxif_example_coprocessor(params: CvxifParams = new CvxifParams) extends Module {
  val io = IO(new Bundle {
    val req  = Input(new CvxifReq(params.X_HARTID_WIDTH, params.X_ID_WIDTH, params.X_NUM_RS, params.X_RFR_WIDTH, params.X_DUALREAD, params.X_DUALWRITE))
    val resp = Output(new CvxifResp(params.X_HARTID_WIDTH, params.X_ID_WIDTH, params.X_NUM_RS, params.X_RFR_WIDTH, params.X_DUALREAD, params.X_DUALWRITE, params.X_RFW_WIDTH))
  })

  // 输入
  val req = io.req
  // temp
  val resp = Wire(new CvxifResp(params.X_HARTID_WIDTH, params.X_ID_WIDTH, params.X_NUM_RS, params.X_RFR_WIDTH, params.X_DUALREAD, params.X_DUALWRITE, params.X_RFW_WIDTH))
  // 输出
  io.resp := resp

  // 译码器
  val decoder = Module(new First_Stage_Decoder(params))
  decoder.io.issue_valid := req.issue_valid
  decoder.io.issue_req := req.issue_req
  decoder.io.register_valid := req.register_valid
  decoder.io.register := req.register

  // 协处理器核心
  val core = Module(new Corprocessor(params))
  core.io.issue_valid := decoder.io.issue_ready && decoder.io.issue_resp.accept
  core.io.issue_req := req.issue_req
  core.io.register_data := decoder.io.register_data
  core.io.isConfig := decoder.io.isConfig
  core.io.isVec := decoder.io.isVec
  core.io.isMat := decoder.io.isMat

  // 输出响应
  resp.compressed_ready := true.B
  resp.compressed_resp.instr := "b0".U // 不处理压缩指令，保持默认值
  resp.compressed_resp.accept := false.B

  resp.issue_ready := decoder.io.issue_ready
  resp.issue_resp := decoder.io.issue_resp

  resp.register_ready := true.B // 始终可接收寄存器请求

  resp.result_valid := core.io.result_valid
  resp.result := core.io.result
}
