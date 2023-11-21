package IntermediatePresentation.Instruction;

import IntermediatePresentation.ConstNumber;
import IntermediatePresentation.IRManager;
import IntermediatePresentation.Value;
import IntermediatePresentation.ValueType;
import TargetCode.Instruction.ALU.Add;
import TargetCode.Instruction.ALU.Addi;
import TargetCode.Instruction.ALU.Mflo;
import TargetCode.Instruction.ALU.Mult;
import TargetCode.Instruction.La;
import TargetCode.Instruction.Li;
import TargetCode.MipsManager;
import TargetCode.Register;
import TargetCode.RegisterManager;

public class GetElementPtr extends Instruction {
    private final Value elemIndex;

    //全部视作一维数组
    public GetElementPtr(Value ptr, Value elemIndex) {
        super(IRManager.getInstance().declareTempVar(), ValueType.PI32);
        this.elemIndex = turntoI32WhileBuilding(elemIndex);
        use(ptr);
        use(this.elemIndex);
    }

    public GetElementPtr(Value ptr, int elemIndex) {
        super(IRManager.getInstance().declareTempVar(), ValueType.PI32);
        this.elemIndex = new ConstNumber(elemIndex);
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
                    ptr.getReg() + ",i32 0, i32 " + elemIndex.getReg() + "\n";
        } else {
            return reg + " = getelementptr i32" + ", " + ptr.getTypeString() + " " +
                    ptr.getReg() + ",i32 " + elemIndex.getReg() + "\n";
        }
    }

    public void toMips() {
        super.toMips();
        Value ptr = operandList.get(0);

        Register mipsRegister = RegisterManager.instance().getRegOf(this);
        boolean noRegAllocated = mipsRegister == null;
        if (noRegAllocated) {
            mipsRegister = RegisterManager.t0;
        }

        //将地址放在k0中
        if (ptr instanceof GlobalDecl) {
            //全局变量地址是gAddr，第i个放在gAddr+4*i
            if (elemIndex instanceof ConstNumber n) {
                new La(RegisterManager.k0, MipsManager.instance().getGlobalData(ptr));
                new Addi(mipsRegister, RegisterManager.k0, n.getVal() * 4);
            } else {
                //offset = 4*k0
                MipsManager.instance().getTempVarByRegister(elemIndex, RegisterManager.k0);
                new Li(RegisterManager.k1, 4);
                new Mult(RegisterManager.k0, RegisterManager.k1);
                new Mflo(RegisterManager.k0);

                new La(RegisterManager.k1, MipsManager.instance().getGlobalData(ptr));
                new Add(mipsRegister, RegisterManager.k0, RegisterManager.k1);
            }
        } else if (ptr instanceof LocalDecl) {
            //局部变量地址是常数stackP+$sp
            if (elemIndex instanceof ConstNumber n) {
                int addr = MipsManager.instance().getLocalVarAddr(ptr) + n.getVal() * 4;
                new Addi(mipsRegister, RegisterManager.sp, addr);
            } else {
                MipsManager.instance().getTempVarByRegister(elemIndex, RegisterManager.k0);
                new Li(RegisterManager.k1, 4);
                new Mult(RegisterManager.k0, RegisterManager.k1);
                new Mflo(RegisterManager.k0);
                //TODO:换成sll
                new Add(RegisterManager.k0, RegisterManager.sp, RegisterManager.k0);

                new Addi(mipsRegister, RegisterManager.k0, MipsManager.instance().getLocalVarAddr(ptr));
            }
        } else {
            //数组作为函数参数，不需要以$sp为基地址寻址，地址直接是相应值
            Register ptrRegister = RegisterManager.instance().getRegOf(ptr);
            if (ptrRegister == null) {
                ptrRegister = RegisterManager.t1;
                MipsManager.instance().putDeclaredVarIntoRegister(ptr, ptrRegister);
            }
            if (elemIndex instanceof ConstNumber n) {
                new Addi(mipsRegister, ptrRegister, n.getVal() * 4);
            } else {
                MipsManager.instance().getTempVarByRegister(elemIndex, RegisterManager.k0);
                new Li(RegisterManager.k1, 4);
                new Mult(RegisterManager.k0, RegisterManager.k1);
                new Mflo(RegisterManager.k0);

                new Add(mipsRegister, RegisterManager.k0, ptrRegister);
            }
        }

        if (noRegAllocated) {
            MipsManager.instance().pushTempVar(this, mipsRegister);
        }
    }
}
