package TargetCode.Instruction.ALU;

import TargetCode.Instruction.MipsInstr;
import TargetCode.Register;

import java.util.ArrayList;

public class Neg extends MipsInstr {
    private final Register dest;
    private final Register src;
    public Neg(Register dest, Register src){
        super();
        this.dest = dest;
        this.src = src;
    }

    public String toString() {
        return "neg " + dest + ", " + src + "\n";
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
