package TargetCode.Instruction.ALU;

import TargetCode.Instruction.MipsInstr;
import TargetCode.Register;

import java.util.ArrayList;

public class Sub extends MipsInstr {
    private final Register dest;
    private final Register oprand1;
    private final Register oprand2;

    public Sub(Register dest, Register oprand1, Register oprand2) {
        super();
        this.dest = dest;
        this.oprand1 = oprand1;
        this.oprand2 = oprand2;
    }

    public String toString() {
        return "subu " + dest + ", " + oprand1 + ", " + oprand2 + "\n";
    }

    public Register putToRegister() {
        return dest;
    }

    public ArrayList<Register> operandRegs(){
        ArrayList<Register> ret = new ArrayList<>();
        ret.add(oprand1);
        ret.add(oprand2);
        return ret;
    }
}
