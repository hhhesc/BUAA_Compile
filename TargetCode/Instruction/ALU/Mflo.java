package TargetCode.Instruction.ALU;

import TargetCode.Instruction.MipsInstr;
import TargetCode.Register;

public class Mflo extends MipsInstr {
    private final Register dest;

    public Mflo(Register dest) {
        super();
        this.dest = dest;
    }

    public String toString() {
        return "mflo " + dest + "\n";
    }
}
