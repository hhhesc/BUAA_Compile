package IntermediatePresentation.Instruction;

import IntermediatePresentation.ConstNumber;
import IntermediatePresentation.Function.MainFunction;
import IntermediatePresentation.Value;
import IntermediatePresentation.ValueType;
import TargetCode.Instruction.Jump.Jr;
import TargetCode.Instruction.Li;
import TargetCode.Instruction.Move;
import TargetCode.Instruction.Syscall;
import TargetCode.MipsManager;
import TargetCode.Register;
import TargetCode.RegisterManager;

public class Ret extends Instruction {

    public Ret() {
        super("RET", ValueType.NULL);
    }

    public Ret(Value retValue) {
        super("RET", ValueType.I32);
        if (getBlock().getFunction() instanceof MainFunction) {
            use(new ConstNumber(0));
        } else {
            use(retValue);
        }
    }

    public String toString() {
        if (vType == ValueType.NULL) {
            return "ret void\n";
        } else {
            return "ret " + getTypeString() + " " + operandList.get(0).getReg() + "\n";
        }
    }

    public void toMips() {
        super.toMips();
        if (getBlock().getFunction() instanceof MainFunction) {
            //如果是主函数，那就根本不需要管他了
            new Li(RegisterManager.v0, 10);
            new Syscall();
        } else {
            if (vType != ValueType.NULL) {
                Register v0 = RegisterManager.v0;
                if (operandList.get(0) instanceof ConstNumber n) {
                    new Li(v0, n.getVal());
                } else {
                    MipsManager.instance().putTempVarIntoRegister(operandList.get(0), RegisterManager.v0);
                }
            }
            new Jr();
        }
    }

    public boolean isUseless() {
        return false;
    }

    public boolean isDefInstr() {
        return false;
    }

    public Value getRetValue() {
        return operandList.get(0);
    }
}
