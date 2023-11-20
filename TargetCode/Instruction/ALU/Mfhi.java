package TargetCode.Instruction.ALU;

import TargetCode.Instruction.MipsInstr;
import TargetCode.Register;

public class Mfhi extends MipsInstr {
    private final Register dest;

    public Mfhi(Register dest) {
        super();
        this.dest = dest;
    }

    public String toString() {
        return "mfhi " + dest+"\n";
    }
}
