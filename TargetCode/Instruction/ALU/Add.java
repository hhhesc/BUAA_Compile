package TargetCode.Instruction.ALU;

import TargetCode.Instruction.MipsInstr;
import TargetCode.MipsManager;
import TargetCode.Register;

public class Add extends MipsInstr {
    private final Register dest;
    private final Register oprand1;
    private final Register oprand2;

    public Add(Register dest, Register oprand1, Register oprand2) {
        super();
        this.dest = dest;
        this.oprand1 = oprand1;
        this.oprand2 = oprand2;
    }

    public String toString() {
        return "add " + dest + ", " + oprand1 + ", " + oprand2 + "\n";
    }
}
