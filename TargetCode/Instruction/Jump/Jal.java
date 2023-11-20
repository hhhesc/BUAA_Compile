package TargetCode.Instruction.Jump;

import TargetCode.Instruction.MipsInstr;
import TargetCode.Label;

public class Jal extends MipsInstr {
    private final Label label;

    public Jal(Label label) {
        super();
        this.label = label;
    }

    public String toString() {
        return "jal " + label.getIdent() + "\n";
    }
}
