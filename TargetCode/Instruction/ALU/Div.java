package TargetCode.Instruction.ALU;

import TargetCode.Instruction.MipsInstr;
import TargetCode.Register;

public class Div extends MipsInstr {
    private final Register oprand1;
    private final Register oprand2;

    public Div(Register oprand1, Register oprand2) {
        super();
        this.oprand1 = oprand1;
        this.oprand2 = oprand2;
    }

    public String toString() {
        return "div " + oprand1 + ", " + oprand2 + "\n";
    }
}
