package Corprocessor

import chisel3._
import chisel3.util._

class First_Stage_Decoder(params: CvxifParams = new CvxifParams) extends Module {
  val io = IO(new Bundle {
    val issue_valid     = Input(Bool())
    val issue_req       = Input(new XIssueReq(params.X_HARTID_WIDTH, params.X_ID_WIDTH))
    val issue_ready     = Output(Bool())
    val issue_resp      = Output(new XIssueResp(params.X_DUALWRITE, params.X_NUM_RS, params.X_DUALREAD))
    val register_valid  = Input(Bool())
    val register        = Input(new XRegister(params.X_HARTID_WIDTH, params.X_ID_WIDTH, params.X_NUM_RS, params.X_RFR_WIDTH, params.X_DUALREAD))
    val register_data   = Output(Vec(params.X_NUM_RS, UInt(params.X_RFR_WIDTH.W)))
    // to MCSR
    val isVec           = Output(Bool())
    val isMat           = Output(Bool())
    val isConfig        = Output(Bool())
  })

  // input
  val issue_valid     = io.issue_valid
  val issue_req       = io.issue_req
  val register_valid  = io.register_valid
  val register        = io.register
  // temp
  val issue_ready     = Wire(Bool())
  val issue_resp      = Wire(new XIssueResp(params.X_DUALWRITE, params.X_NUM_RS, params.X_DUALREAD))
  val register_data   = Wire(Vec(params.X_NUM_RS, UInt(params.X_RFR_WIDTH.W)))
  // output
  io.issue_ready     := issue_ready
  io.issue_resp      := issue_resp
  io.register_data   := register_data

  // 指令译码
  val VDecode = Module(new VDecodeSimple)
  val MDecode = Module(new MDecodeSimple)

  VDecode.io.in := issue_req.instr
  MDecode.io.in := issue_req.instr

  val register_read_rs1 = WireDefault(false.B)
  val register_read_rs2 = WireDefault(false.B)

  when(VDecode.io.isVec){
    register_read_rs1 := VDecode.io.xrs1
    register_read_rs2 := VDecode.io.xrs2
    when(issue_valid){ 
        issue_resp.accept := true.B
        issue_resp.writeback := VDecode.io.xrd
        issue_resp.register_read := Cat(register_read_rs2, register_read_rs1)
    } .otherwise {
        issue_resp.accept := false.B
        issue_resp.writeback := 0.U((params.X_DUALWRITE + 1).W)
        issue_resp.register_read := 0.U((params.X_NUM_RS + params.X_DUALREAD).W)
    }  
  }.elsewhen(MDecode.io.isMat){
    register_read_rs1 := MDecode.io.xrs1
    register_read_rs2 := MDecode.io.xrs2
    when(issue_valid){ 
        issue_resp.accept := true.B
        issue_resp.writeback := MDecode.io.xrd
        issue_resp.register_read := Cat(register_read_rs2, register_read_rs1)
    } .otherwise {
        issue_resp.accept := false.B
        issue_resp.writeback := 0.U((params.X_DUALWRITE + 1).W)
        issue_resp.register_read := 0.U((params.X_NUM_RS + params.X_DUALREAD).W)
    }  
  }.otherwise{
        register_read_rs1 := false.B
        register_read_rs2 := false.B
        issue_resp.accept := false.B
        issue_resp.writeback := 0.U((params.X_DUALWRITE + 1).W)
        issue_resp.register_read := 0.U((params.X_NUM_RS + params.X_DUALREAD).W)
    }  


  val ready_rs1 = register.rs_valid(0) || (!register_read_rs1)
  val ready_rs2 = register.rs_valid(1) || (!register_read_rs2)

  issue_ready := issue_valid && register_valid && ready_rs1 && ready_rs2
  register_data := register.rs

  io.isVec           := VDecode.io.isVec
  io.isMat           := MDecode.io.isMat
  io.isConfig        := VDecode.io.isConfig || MDecode.io.isConfig
}