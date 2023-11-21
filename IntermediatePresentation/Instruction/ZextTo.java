package IntermediatePresentation.Instruction;

import IntermediatePresentation.IRManager;
import IntermediatePresentation.Value;
import IntermediatePresentation.ValueType;
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
        //TODO:什么时候会用到zext??
        Value v = operandList.get(0);
        Register vReg = RegisterManager.instance().getRegOf(v);
        if (vReg != null) {
            RegisterManager.instance().setRegOf(this, vReg);
        } else {
            //直接让他们指向同一个地址
            MipsManager.instance().pointToSameMemory(operandList.get(0), this);
        }
    }
}
