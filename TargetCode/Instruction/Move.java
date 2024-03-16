package TargetCode.Instruction;

import TargetCode.Register;

import java.util.ArrayList;

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

    public Register getSrc() {
        return this.src;
    }

    public Register getDst() {
        return this.dest;
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
