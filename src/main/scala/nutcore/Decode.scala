package nutcore

import chisel3._
import chisel3.util._
import nutcore.isa.{RVDInstr, RVFInstr}

trait HasInstrType {
  def InstrN  = "b0000".U
  def InstrI  = "b0100".U
  def InstrR  = "b0101".U
  def InstrS  = "b0010".U
  def InstrB  = "b0001".U
  def InstrU  = "b0110".U
  def InstrJ  = "b0111".U
  def InstrA  = "b1110".U
  def InstrSA = "b1111".U // Atom Inst: SC
  def InstrFR   = "b1001".U
  def InstrFI   = "b1010".U // flw/fld
  def InstrGtoF = "b1011".U
  def InstrFS   = "b1100".U
  def InstrFtoG = "b1101".U

//  def isrfWen(instrType : UInt): Bool = instrType(2)
  def isrfWen(instrType : UInt): Bool = Array(
    InstrI, InstrR, InstrU, InstrJ, InstrA, InstrSA, InstrFtoG
  ).map(_===instrType).reduce(_||_)

  def isfpWen(instrType: UInt): Bool = Array(
    InstrFI, InstrFR, InstrGtoF
  ).map(_===instrType).reduce(_||_)
}

// trait CompInstConst {
//   val RVCRegNumTable = Array(
//     BitPat("b000") -> 8.U,
//     BitPat("b001") -> 9.U,
//     BitPat("b010") -> 10.U,
//     BitPat("b011") -> 11.U,
//     BitPat("b100") -> 12.U,
//     BitPat("b101") -> 13.U,
//     BitPat("b110") -> 14.U,
//     BitPat("b111") -> 15.U
//   )
// }

object SrcType {
  def reg = "b00".U
  def pc  = "b01".U
  def imm = "b01".U
  def fp = "b10".U
  def apply() = UInt(2.W)
}

object FuType extends HasNutCoreConst {
  def num = 6
  def alu = "b000".U
  def lsu = "b001".U
  def mdu = "b010".U
  def csr = "b011".U
  def mou = "b100".U

  // FIXME
  //  def bru = if(IndependentBru) "b101".U else alu
  def bru = alu
  require(!IndependentBru)

  def fpu = "b101".U
  def apply() = UInt(log2Up(num).W)
}

object FuOpType {
  def apply() = UInt(7.W)
}

object Instructions extends HasInstrType with HasNutCoreParameter {
  def NOP = 0x00000013.U
  val DecodeDefault = List(InstrN, FuType.csr, CSROpType.jmp)
  def DecodeTable = RVIInstr.table ++ NutCoreTrap.table ++
    (if (HasMExtension) RVMInstr.table else Nil) ++
    (if (HasCExtension) RVCInstr.table else Nil) ++
    (if (HasFPU) RVFInstr.table ++ RVDInstr.table else Nil) ++
    Priviledged.table ++
    RVAInstr.table ++
    RVZicsrInstr.table ++ RVZifenceiInstr.table
}

object CInstructions extends HasInstrType with HasNutCoreParameter{
  def NOP = 0x00000013.U
  val DecodeDefault = List(RVCInstr.ImmNone, RVCInstr.DtCare, RVCInstr.DtCare, RVCInstr.DtCare)
  // val DecodeDefault = List(InstrN, FuType.csr, CSROpType.jmp)
  def CExtraDecodeTable = RVCInstr.cExtraTable
}
