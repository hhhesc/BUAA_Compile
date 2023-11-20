package TargetCode.Instruction.Memory;

import TargetCode.Instruction.MipsInstr;
import TargetCode.MipsManager;
import TargetCode.Register;

public class Sw extends MipsInstr {
    private final Register src;
    private final int offset;
    private final Register dst;

    public Sw(Register src, int offset, Register dst) {
        super();
        this.src = src;
        this.offset = offset;
        this.dst = dst;
    }

    public String toString() {
        return "sw " + src + ", " + offset + "(" + dst + ")\n";
    }
}
