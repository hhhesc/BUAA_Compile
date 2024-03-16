package TargetCode.Instruction;

import TargetCode.Instruction.Jump.Branch;
import TargetCode.Instruction.Jump.J;
import TargetCode.Instruction.Jump.Jal;
import TargetCode.Instruction.Jump.Jr;
import TargetCode.MipsManager;
import TargetCode.MipsStmt;
import TargetCode.Register;

import java.util.ArrayList;

public class MipsInstr extends MipsStmt {
    public MipsInstr() {
        if (MipsManager.instance().autoInsert()) {
            MipsManager.getFile().addInstr(this);
        }
    }

    public Register putToRegister() {
        return null;
    }

    public ArrayList<Register> operandRegs() {
        return new ArrayList<>();
    }

    public void replaceRegisterWith(Register origin, Register newReg) {

    }

    public boolean isBranchInstr() {
        return (this instanceof Branch) || (this instanceof J) || (this instanceof Jal) || (this instanceof Jr);
    }
}
