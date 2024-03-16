package TargetCode.Instruction.Jump;

import TargetCode.Instruction.MipsInstr;
import TargetCode.MipsManager;
import TargetCode.Register;
import TargetCode.RegisterManager;

import java.util.ArrayList;

public class Jr extends MipsInstr {
    public Jr() {
        super();
    }

    public String toString() {
        return "jr $ra\n";
    }

    public ArrayList<Register> operandRegs(){
        ArrayList<Register> ret = new ArrayList<>();
        ret.add(RegisterManager.ra);
        return ret;
    }
}
