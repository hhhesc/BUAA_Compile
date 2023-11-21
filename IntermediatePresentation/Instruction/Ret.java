package IntermediatePresentation.Instruction;

import IntermediatePresentation.ConstNumber;
import IntermediatePresentation.Value;
import IntermediatePresentation.ValueType;
import TargetCode.Instruction.Jump.Jr;
import TargetCode.Instruction.Li;
import TargetCode.Instruction.Move;
import TargetCode.MipsManager;
import TargetCode.Register;
import TargetCode.RegisterManager;

public class Ret extends Instruction {
    private Value retValue;

    public Ret() {
        super("RET", ValueType.NULL);
    }

    public Ret(Value retValue) {
        super("RET", ValueType.I32);
        this.retValue = retValue;
    }

    public String toString() {
        if (vType == ValueType.NULL) {
            return "ret void\n";
        } else {
            return "ret " + getTypeString() + " " + retValue.getReg() + "\n";
        }
    }

    public void toMips() {
        super.toMips();
        if (vType != ValueType.NULL) {
            Register v0 = RegisterManager.v0;
            if (retValue instanceof ConstNumber n) {
                new Li(v0, n.getVal());
            } else {
                MipsManager.instance().getTempVarByRegister(retValue, RegisterManager.v0);
                //TODO:这里一定会放到v0里？？
            }
        }
        new Jr();
    }
}
