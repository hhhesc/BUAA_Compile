package IntermediatePresentation.Instruction;

import IntermediatePresentation.ConstNumber;
import IntermediatePresentation.IRManager;
import IntermediatePresentation.Value;
import IntermediatePresentation.ValueType;
import TargetCode.Instruction.ALU.Add;
import TargetCode.Instruction.ALU.Addi;
import TargetCode.Instruction.ALU.Sll;
import TargetCode.Instruction.La;
import TargetCode.MipsManager;
import TargetCode.Register;
import TargetCode.RegisterManager;

public class GetElementPtr extends Instruction {

    //全部视作一维数组
    public GetElementPtr(Value ptr, Value elemIndex) {
        super(IRManager.getInstance().declareTempVar(), ValueType.PI32);
        use(ptr);
        use(turntoI32WhileBuilding(elemIndex));
    }

    public GetElementPtr(Value ptr, int elemIndex) {
        super(IRManager.getInstance().declareTempVar(), ValueType.PI32);
        use(ptr);
        use(new ConstNumber(elemIndex));
    }

    public String toString() {
        Value ptr = operandList.get(0);
        Value elemIndex = operandList.get(1);
            /*
                想要从数组中取出一个数，则需要进行两次offset；而指针只偏移一次
                拿到的数据类型是i32*，即要取到的数的指针
             */
        if (ptr.getType().equals(ValueType.ARRAY)) {
            return reg + " = getelementptr " + ptr.getTypeString() + ", " + ptr.getTypeString() + "* " +
                    ptr.getReg() + ",i32 0, i32 " + elemIndex.getReg() + "\n";
        } else {
            return reg + " = getelementptr i32" + ", " + ptr.getTypeString() + " " +
                    ptr.getReg() + ",i32 " + elemIndex.getReg() + "\n";
        }
    }

    public void toMips() {
        super.toMips();
        Value ptr = operandList.get(0);
        Value elemIndex = operandList.get(1);

        Register mipsRegister = RegisterManager.instance().getRegOf(this);
        boolean noRegAllocated = mipsRegister == null;
        if (noRegAllocated) {
            mipsRegister = RegisterManager.k0;
        }

        //将地址放在k0中
        if (ptr instanceof GlobalDecl) {
            //全局变量地址是gAddr，第i个放在gAddr+4*i
            if (elemIndex instanceof ConstNumber n) {
                new La(RegisterManager.k0, MipsManager.instance().getGlobalData(ptr));
                new Addi(mipsRegister, RegisterManager.k0, n.getVal() * 4);
            } else {
                //offset = 4*k0
                Register register = MipsManager.instance().getTempVarByRegister(elemIndex, RegisterManager.k0);
                new Sll(RegisterManager.k0, register, 2);

                new La(RegisterManager.k1, MipsManager.instance().getGlobalData(ptr));
                new Add(mipsRegister, RegisterManager.k0, RegisterManager.k1);
            }
        } else if (ptr instanceof LocalDecl) {
            //局部变量地址是常数stackP+$sp
            if (elemIndex instanceof ConstNumber n) {
                int addr = MipsManager.instance().getLocalVarAddr(ptr) + n.getVal() * 4;
                new Addi(mipsRegister, RegisterManager.sp, addr);
            } else {
                Register register = MipsManager.instance().getTempVarByRegister(elemIndex, RegisterManager.k0);
                new Sll(RegisterManager.k0, register, 2);
                new Add(RegisterManager.k0, RegisterManager.sp, RegisterManager.k0);

                new Addi(mipsRegister, RegisterManager.k0, MipsManager.instance().getLocalVarAddr(ptr));
            }
        } else {
            //数组作为函数参数，不需要以$sp为基地址寻址，地址直接是相应值
            if (elemIndex instanceof ConstNumber n) {
                new Addi(mipsRegister, MipsManager.instance().getTempVarByRegister(ptr,
                        RegisterManager.k1), n.getVal() * 4);
            } else {
                Register register = MipsManager.instance().getTempVarByRegister(elemIndex, RegisterManager.k0);
                new Sll(RegisterManager.k0, register, 2);

                new Add(mipsRegister, RegisterManager.k0, MipsManager.instance().getTempVarByRegister(ptr,
                        RegisterManager.k1));
            }
        }

        if (noRegAllocated) {
            MipsManager.instance().pushTempVar(this, mipsRegister);
        }
    }

    public Value getPtr() {
        return operandList.get(0);
    }

    public Value getElemIndex() {
        return operandList.get(1);
    }

    public boolean canGetConstNumber() {
        Value ptr = getPtr();
        return ptr instanceof GlobalDecl globalDecl && globalDecl.isConst() && getElemIndex() instanceof ConstNumber;
    }

    public int getStorageVal() {
        return ((GlobalDecl) getPtr()).getConstValAtIndex(((ConstNumber) getElemIndex()).getVal());
    }
}
