package IntermediatePresentation.Instruction;

import IntermediatePresentation.ConstNumber;
import IntermediatePresentation.IRManager;
import IntermediatePresentation.Value;
import IntermediatePresentation.ValueType;
import TargetCode.Instruction.Li;
import TargetCode.Instruction.Memory.Lw;
import TargetCode.Instruction.Memory.Sw;
import TargetCode.Instruction.Move;
import TargetCode.MipsManager;
import TargetCode.Register;
import TargetCode.RegisterManager;

public class ZextTo extends Instruction {
    public ZextTo(Value v, ValueType type) {
        super(IRManager.getInstance().declareTempVar(), type);
        use(v);
    }

    public String toString() {
        return reg + " = zext " + operandList.get(0).getTypeString() + " " +
                operandList.get(0).getReg() + " to " + getTypeString() + "\n";
    }

    public void toMips() {
        super.toMips();
        Value v = operandList.get(0);
        Register vReg = RegisterManager.instance().getRegOf(v);
        Register register = RegisterManager.instance().getRegOf(this);

        if (register != null) {
            if (v instanceof ConstNumber n) {
                new Li(register, n.getVal());
            } else {
                MipsManager.instance().putTempVarIntoRegister(v, register);
            }
        } else {
            if (vReg != null) {
                MipsManager.instance().pushTempVar(this, vReg);
            } else {
                MipsManager.instance().pointToSameMemory(v, this);
            }
        }
    }
}
