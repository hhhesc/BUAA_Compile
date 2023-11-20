package TargetCode.Instruction.Jump;

import TargetCode.Instruction.MipsInstr;

public class Jr extends MipsInstr {
    public Jr() {
        super();
    }

    public String toString() {
        return "jr $ra\n";
    }
}
