package IntermediatePresentation.Instruction;

import IntermediatePresentation.ConstString;
import IntermediatePresentation.ValueType;
import TargetCode.Instruction.La;
import TargetCode.Instruction.Li;
import TargetCode.Instruction.Syscall;
import TargetCode.MipsManager;
import TargetCode.RegisterManager;

public class Putstr extends Instruction {
    ConstString string;
    int len;

    public Putstr(ConstString string, int len) {
        super("PUTSTR", ValueType.NULL);
        this.string = string;
        this.len = len;
    }
    //丑陋的补丁

    public ConstString getString() {
        return string;
    }

    public int getLen() {
        return len;
    }

    public String toString() {
        return "call void @putstr(i8* getelementptr inbounds ([ " + len + " x i8 ], [ " +
                len + " x i8 ]* " + string.getReg() + ", i64 0, i64 0))\n";
    }

    public void toMips() {
        super.toMips();
        new La(RegisterManager.a0, MipsManager.instance().getGlobalData(string));
        new Li(RegisterManager.v0, 4);
        new Syscall();
    }

    public boolean isUseless() {
        return false;
    }

    public boolean isDefInstr() {
        return false;
    }
}
