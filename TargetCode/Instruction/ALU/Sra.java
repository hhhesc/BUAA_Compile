package TargetCode.Instruction.ALU;

import TargetCode.Instruction.MipsInstr;
import TargetCode.Register;

import java.util.ArrayList;

public class Sra extends MipsInstr {
    private final Register oprand1;
    private final Register oprand2;
    private final int imm;

    public Sra(Register oprand1, Register oprand2, int imm) {
        super();
        this.oprand1 = oprand1;
        this.oprand2 = oprand2;
        this.imm = imm;
    }

    public String toString() {
        return "sra " + oprand1 + ", " + oprand2 + ", " + imm + "\n";
    }

    public Register putToRegister() {
        return oprand1;
    }

    public ArrayList<Register> operandRegs(){
        ArrayList<Register> ret = new ArrayList<>();
        ret.add(oprand2);
        return ret;
    }
}
