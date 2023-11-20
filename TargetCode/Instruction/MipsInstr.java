package TargetCode.Instruction;

import TargetCode.MipsManager;
import TargetCode.MipsStmt;

public class MipsInstr extends MipsStmt {
    public MipsInstr() {
        MipsManager.getFile().addInstr(this);
    }
}
