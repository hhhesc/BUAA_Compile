package TargetCode.Instruction.Memory;

import TargetCode.Instruction.MipsInstr;
import TargetCode.Register;

import java.util.ArrayList;
import java.util.Objects;

public class Sw extends MipsInstr {
    private final Register src;
    private final OffsetRegAddr offsetRegAddr;

    public Sw(Register src, int offset, Register dest) {
        super();
        this.src = src;
        offsetRegAddr = new OffsetRegAddr(offset, dest);
    }

    public Sw(Register src, String label, Register dest) {
        super();
        this.src = src;
        offsetRegAddr = new OffsetRegAddr(label, dest);
    }

    public String toString() {
        return "sw " + src + ", " + offsetRegAddr + "\n";
    }

    public Register getDest() {
        return offsetRegAddr.getReg();
    }

    public Integer getOffset() {
        return offsetRegAddr.getOffset();
    }

    public Register getSrc() {
        return src;
    }

    public String getLabel() {
        return offsetRegAddr.getLabel();
    }

    public OffsetRegAddr getOffsetRegAddr() {
        return offsetRegAddr;
    }

    public ArrayList<Register> operandRegs(){
        ArrayList<Register> ret = new ArrayList<>();
        ret.add(src);
        ret.add(offsetRegAddr.getReg());
        return ret;
    }
}
