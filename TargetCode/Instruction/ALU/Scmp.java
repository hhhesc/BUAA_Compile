package TargetCode.Instruction.ALU;

import TargetCode.Instruction.MipsInstr;
import TargetCode.Register;

import java.util.Objects;

public class Scmp extends MipsInstr {
    private final String op;
    private final Register dest;
    private final Register oprand1;
    private Register oprand2Reg;
    private int oprand2Num;

    public Scmp(String op, Register dest, Register oprand1, Register oprand2) {
        super();
        this.op = op;
        this.dest = dest;
        this.oprand1 = oprand1;
        this.oprand2Reg = oprand2;
    }

    public Scmp(String op, Register dest, Register oprand1, int oprand2) {
        this.op = op;
        this.dest = dest;
        this.oprand1 = oprand1;
        this.oprand2Num = oprand2;
    }

    public String toString() {
        return op + " " + dest + ", " + oprand1 + ", " +
                Objects.requireNonNullElseGet(oprand2Reg, () -> oprand2Num) + "\n";
    }
}
