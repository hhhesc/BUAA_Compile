package TargetCode.Instruction.Memory;

import TargetCode.Instruction.MipsInstr;
import TargetCode.MipsManager;
import TargetCode.Register;

public class Lw extends MipsInstr {
    private final Register dest;
    private final int offset;
    private final Register src;

    public Lw(Register dest, int offset, Register src) {
        super();
        this.src = src;
        this.offset = offset;
        this.dest = dest;
    }

    public String toString() {
        return "lw " + dest + ", " + offset + "(" + src + ")\n";
    }
}
