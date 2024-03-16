package TargetCode.Instruction;

import TargetCode.Register;
import TargetCode.RegisterManager;

public class Syscall extends MipsInstr {
    public Syscall() {
        super();
    }

    public String toString() {
        return "syscall\n";
    }

    public Register putToRegister() {
        return RegisterManager.v0;
    }

}
