package TargetCode.Instruction.ALU;

import TargetCode.Instruction.MipsInstr;
import TargetCode.Register;

import java.util.ArrayList;

public class Mult extends MipsInstr {
    private final Register oprand1;
    private final Register oprand2;

    public Mult(Register oprand1, Register oprand2) {
        super();
        this.oprand1 = oprand1;
        this.oprand2 = oprand2;
    }

    public String toString() {
        return "mult " + oprand1 + ", " + oprand2 + "\n";
    }

    public ArrayList<Register> operandRegs(){
        ArrayList<Register> ret = new ArrayList<>();
        ret.add(oprand1);
        ret.add(oprand2);
        return ret;
    }
}
