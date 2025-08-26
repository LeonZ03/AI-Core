package Corprocessor

import chisel3._
import chisel3.util._


// x_compressed_req_t
class XCompressedReq(X_HARTID_WIDTH: Int) extends Bundle {
  val instr = UInt(16.W)                 // logic [15:0]
  val hartid = UInt(X_HARTID_WIDTH.W)   // hartid_t
}

// x_compressed_resp_t
class XCompressedResp extends Bundle {
  val instr = UInt(32.W)
  val accept = Bool()
}

// x_issue_req_t
class XIssueReq(X_HARTID_WIDTH: Int, X_ID_WIDTH: Int) extends Bundle {
  val instr = UInt(32.W)
  val hartid = UInt(X_HARTID_WIDTH.W)
  val id = UInt(X_ID_WIDTH.W)
}

// x_issue_resp_t
class XIssueResp(X_DUALWRITE: Int, X_NUM_RS: Int, X_DUALREAD: Int) extends Bundle {
  val accept = Bool()
  val writeback = UInt((X_DUALWRITE + 1).W)      // writeregflags_t
  val register_read = UInt((X_NUM_RS + X_DUALREAD).W)  // readregflags_t
}

// x_register_t
class XRegister(X_HARTID_WIDTH: Int, X_ID_WIDTH: Int, X_NUM_RS: Int, X_RFR_WIDTH: Int, X_DUALREAD: Int) extends Bundle {
  val hartid = UInt(X_HARTID_WIDTH.W)
  val id = UInt(X_ID_WIDTH.W)
  val rs = Vec(X_NUM_RS, UInt(X_RFR_WIDTH.W))
  val rs_valid = UInt((X_NUM_RS + X_DUALREAD).W)   // readregflags_t
}

// x_commit_t
class XCommit(X_HARTID_WIDTH: Int, X_ID_WIDTH: Int) extends Bundle {
  val hartid = UInt(X_HARTID_WIDTH.W)
  val id = UInt(X_ID_WIDTH.W)
  val commit_kill = Bool()
}

// x_result_t
class XResult(X_HARTID_WIDTH: Int, X_ID_WIDTH: Int, X_DUALWRITE: Int, X_RFW_WIDTH: Int) extends Bundle {
  val hartid = UInt(X_HARTID_WIDTH.W)
  val id = UInt(X_ID_WIDTH.W)
  val data = UInt(X_RFW_WIDTH.W)
  val rd = UInt(5.W)
  val we = UInt((X_DUALWRITE + 1).W)  // writeregflags_t
}

// cvxif_req_t
class CvxifReq(X_HARTID_WIDTH: Int, X_ID_WIDTH: Int, X_NUM_RS: Int, X_RFR_WIDTH: Int, X_DUALREAD: Int, X_DUALWRITE: Int) extends Bundle {
  val compressed_valid = Bool()
  val compressed_req = new XCompressedReq(X_HARTID_WIDTH)
  val issue_valid = Bool()
  val issue_req = new XIssueReq(X_HARTID_WIDTH, X_ID_WIDTH)
  val register_valid = Bool()
  val register = new XRegister(X_HARTID_WIDTH, X_ID_WIDTH, X_NUM_RS, X_RFR_WIDTH, X_DUALREAD)
  val commit_valid = Bool()
  val commit = new XCommit(X_HARTID_WIDTH, X_ID_WIDTH)
  val result_ready = Bool()
}

// cvxif_resp_t
class CvxifResp(X_HARTID_WIDTH: Int, X_ID_WIDTH: Int, X_NUM_RS: Int, X_RFR_WIDTH: Int, X_DUALREAD: Int, X_DUALWRITE: Int, X_RFW_WIDTH: Int) extends Bundle {
  val compressed_ready = Bool()
  val compressed_resp = new XCompressedResp
  val issue_ready = Bool()
  val issue_resp = new XIssueResp(X_DUALWRITE, X_NUM_RS, X_DUALREAD)
  val register_ready = Bool()
  val result_valid = Bool()
  val result = new XResult(X_HARTID_WIDTH, X_ID_WIDTH, X_DUALWRITE, X_RFW_WIDTH)
}

class CvxifParams(
  val X_NUM_RS: Int = 2,
  val X_DUALREAD: Int = 0,
  val X_DUALWRITE: Int = 0,
  val X_ID_WIDTH: Int = 3,
  val X_HARTID_WIDTH: Int = 64,
  val X_RFR_WIDTH: Int = 64,
  val X_RFW_WIDTH: Int = 64
)

class CvxifIO(params: CvxifParams) extends Bundle {
  val req = Input(new CvxifReq(params.X_HARTID_WIDTH, params.X_ID_WIDTH, params.X_NUM_RS, params.X_RFR_WIDTH, params.X_DUALREAD, params.X_DUALWRITE))
  val resp = Output(new CvxifResp(params.X_HARTID_WIDTH, params.X_ID_WIDTH, params.X_NUM_RS, params.X_RFR_WIDTH, params.X_DUALREAD, params.X_DUALWRITE, params.X_RFW_WIDTH))
}
