package TargetCode.Instruction.Jump;

import TargetCode.Instruction.MipsInstr;
import TargetCode.Label;

public class J extends MipsInstr {
    private final Label label;

    public J(Label label) {
        super();
        this.label = label;
    }

    public String toString() {
        return "j " + label.getIdent() + "\n";
    }
}
