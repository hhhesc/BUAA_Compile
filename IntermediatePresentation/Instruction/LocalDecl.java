package IntermediatePresentation.Instruction;

import IntermediatePresentation.IRManager;
import IntermediatePresentation.ValueType;
import TargetCode.MipsManager;

public class LocalDecl extends Instruction {
    public LocalDecl() {
        super(IRManager.getInstance().declareVar(), ValueType.PI32);
    }

    public LocalDecl(int len) {
        super(IRManager.getInstance().declareVar(), ValueType.ARRAY);
        vType = new ValueType(len);
    }

    public String toString() {
        return reg + " = alloca " + vType.getRefTypeString() + "\n";
    }

    public void toMips() {
        /*
            分配内存，至于stroe之后Load啥的是优化需要考虑的事，目前不需要考虑寄存器
            局部变量分配到函数活动记录中，在参数区栈顶之上
         */
        MipsManager.instance().allocInStackBy(this, vType.getLength());
    }
}
