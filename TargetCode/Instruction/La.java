package TargetCode.Instruction;

import TargetCode.Register;
import TargetCode.RegisterManager;

public class La extends MipsInstr {
    private final Register dest;
    private final String addr;

    public La(Register dest, String addr) {
        super();
        this.dest = dest;
        this.addr = addr;
    }

    public String toString() {
        return "la " + dest + ", " + addr + "\n";
    }

    public Register putToRegister() {
        return dest;
    }

    public String getLabel() {
        return addr;
    }
}
