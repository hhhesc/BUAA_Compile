package IntermediatePresentation.Instruction;

import IntermediatePresentation.ConstNumber;
import IntermediatePresentation.IRManager;
import IntermediatePresentation.Value;
import IntermediatePresentation.ValueType;

public class GetElementPtr extends Instruction {
    private final Value offset;

    //全部视作一维数组
    public GetElementPtr(Value ptr, Value offset) {
        super(IRManager.getInstance().declareTempVar(), ValueType.PI32);
        this.offset = turntoI32WhileBuilding(offset);
        use(ptr);
        use(this.offset);
    }

    public GetElementPtr(Value ptr, int offset) {
        super(IRManager.getInstance().declareTempVar(), ValueType.PI32);
        this.offset = new ConstNumber(offset);
        use(ptr);
    }

    public String toString() {
        Value ptr = operandList.get(0);
            /*
                想要从数组中取出一个数，则需要进行两次offset；而指针只偏移一次
                拿到的数据类型是i32*，即要取到的数的指针
             */
        if (ptr.getType().equals(ValueType.ARRAY)) {
            return reg + " = getelementptr " + ptr.getTypeString() + ", " + ptr.getTypeString() + "* " +
                    ptr.getReg() + ",i32 0, i32 " + offset.getReg() + "\n";
        } else {
            return reg + " = getelementptr i32" + ", " + ptr.getTypeString() + " " +
                    ptr.getReg() + ",i32 " + offset.getReg() + "\n";
        }
    }
}
