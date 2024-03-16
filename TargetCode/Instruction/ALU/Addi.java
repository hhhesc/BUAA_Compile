package TargetCode.Instruction.ALU;

import TargetCode.Instruction.MipsInstr;
import TargetCode.MipsStmt;
import TargetCode.Register;
import TargetCode.RegisterManager;

import java.util.ArrayList;

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
        return "addiu " + dest + ", " + src + ", " + imm + "\n";
    }

    public Register getSrc() {
        return src;
    }

    public Register getDest() {
        return dest;
    }

    public int getImm() {
        return imm;
    }

    public Register putToRegister() {
        return dest;
    }

    public ArrayList<Register> operandRegs(){
        ArrayList<Register> ret = new ArrayList<>();
        ret.add(src);
        return ret;
    }
}
