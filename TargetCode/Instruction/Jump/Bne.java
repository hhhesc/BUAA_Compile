package TargetCode.Instruction.Jump;

import TargetCode.Instruction.MipsInstr;
import TargetCode.Label;
import TargetCode.Register;

public class Bne extends MipsInstr {
    private final Label dest;
    private final Register operand1;
    private final Register operand2;

    public Bne(Label dest, Register operand1, Register operand2) {
        super();
        this.dest = dest;
        this.operand1 = operand1;
        this.operand2 = operand2;
    }

    public String toString() {
        return "bne " + operand1 + ", " + operand2 + ", " + dest.getIdent() + "\n";
    }
}
