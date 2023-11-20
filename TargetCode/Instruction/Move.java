package TargetCode.Instruction;

import TargetCode.Register;

public class Move extends MipsInstr {
    private final Register dest;
    private final Register src;

    public Move(Register dest, Register src) {
        this.dest = dest;
        this.src = src;
    }

    public String toString() {
        return "move " + dest + ", " + src + "\n";
    }
}
