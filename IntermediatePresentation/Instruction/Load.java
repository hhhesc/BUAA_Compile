package IntermediatePresentation.Instruction;

import IntermediatePresentation.Value;
import TargetCode.Instruction.Memory.Lw;
import TargetCode.MipsManager;
import TargetCode.Register;
import TargetCode.RegisterManager;

public class Load extends Instruction {
    private final Value addr;

    public Load(String reg, Value addr) {
        super(reg, addr.getRefType());
        this.addr = addr;
        use(addr);
    }

    public String toString() {
        return reg + " = load " + addr.getRefType() + ", " + addr.getTypeString() + " " + addr.getReg() + "\n";
    }

    public void toMips() {
        /*
            如果分配了寄存器，就放在那里；否则压入栈中
         */
        super.toMips();

        Register mipsRegister = RegisterManager.instance().getRegOf(this);
        boolean noRegAllocated = mipsRegister == null;
        if (noRegAllocated) {
            mipsRegister = RegisterManager.k0;
        }

        if (addr instanceof LocalDecl || addr instanceof GlobalDecl) {
            //TODO:前四个函数参数会被load吗
            MipsManager.instance().putDeclaredVarIntoRegister(addr, mipsRegister);
        } else {
            //addr就是一个实际的内存地址，其值是一个临时变量
            MipsManager.instance().getTempVarByRegister(addr, RegisterManager.k1);
            new Lw(mipsRegister, 0, RegisterManager.k1);
        }

        if (noRegAllocated) {
            MipsManager.instance().pushTempVar(this, mipsRegister);
        }
    }
}
