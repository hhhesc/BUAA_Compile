package TargetCode.Instruction.ALU;

import TargetCode.Instruction.MipsInstr;
import TargetCode.Register;

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
        return "sub " + dest + ", " + oprand1 + ", " + oprand2 + "\n";
    }
}
