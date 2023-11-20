package TargetCode.Instruction.ALU;

import TargetCode.Instruction.MipsInstr;
import TargetCode.MipsStmt;
import TargetCode.Register;
import TargetCode.RegisterManager;

public class Addi extends MipsInstr {
    private final Register dest;
    private final Register src;
    private final int imm;

    public Addi(Register dest, Register src, int imm) {
        super();
        this.dest = dest;
        this.src = src;
        this.imm = imm;
    }

    public String toString() {
        return "addi " + dest + ", " + src + ", " + imm + "\n";
    }
}
