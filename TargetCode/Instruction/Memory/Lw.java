package TargetCode.Instruction.Memory;

import TargetCode.Instruction.MipsInstr;
import TargetCode.Register;

import java.util.ArrayList;

public class Lw extends MipsInstr {
    private final OffsetRegAddr offsetRegAddr;
    private final Register dest;

    public Lw(Register dest, int offset, Register src) {
        super();
        this.dest = dest;
        offsetRegAddr = new OffsetRegAddr(offset, src);
    }

    public Lw(Register dest, String label, Register src) {
        super();
        this.dest = dest;
        offsetRegAddr = new OffsetRegAddr(label, src);
    }

    public String toString() {
        return "lw " + dest + ", " + offsetRegAddr + "\n";
    }

    public Register getDest() {
        return dest;
    }

    public Integer getOffset() {
        return offsetRegAddr.getOffset();
    }

    public Register getSrc() {
        return offsetRegAddr.getReg();
    }

    public String getLabel() {
        return offsetRegAddr.getLabel();
    }

    public OffsetRegAddr getOffsetRegAddr() {
        return offsetRegAddr;
    }

    public Register putToRegister() {
        return dest;
    }

    public ArrayList<Register> operandRegs(){
        ArrayList<Register> ret = new ArrayList<>();
        ret.add(offsetRegAddr.getReg());
        return ret;
    }
}
