package TargetCode.Instruction;

import TargetCode.Register;

public class Li extends MipsInstr {
    private final Register register;
    private final int imm;

    public Li(Register register, int imm) {
        super();
        this.register = register;
        this.imm = imm;
    }

    public String toString() {
        return "li " + register + ", " + imm + "\n";
    }

    public Register putToRegister() {
        return register;
    }

    public Register getDest() {
        return register;
    }

    public int getImm() {
        return imm;
    }
}
