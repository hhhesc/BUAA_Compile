package IntermediatePresentation.Instruction;

import IntermediatePresentation.User;
import IntermediatePresentation.Value;
import TargetCode.Instruction.Memory.Lw;
import TargetCode.MipsManager;
import TargetCode.Register;
import TargetCode.RegisterManager;

import java.util.ArrayList;

public class Load extends Instruction {
    //只有getAddr且并没有operand时才可以访问这个addr，其余都要用operandList.get(0)代替
    private Value addr;

    public Load(String reg, Value addr) {
        super(reg, addr.getRefType());
        this.addr = addr;
        use(addr);
    }

    public Value getAddr() {
        if (operandList.size() == 0) {
            return addr;
        } else {
            return operandList.get(0);
        }
    }

    public String toString() {
        Value addr = operandList.get(0);
        return reg + " = load " + addr.getRefType() + ", " + addr.getTypeString() + " " + addr.getReg() + "\n";
    }

    public void toMips() {
        /*
            如果分配了寄存器，就放在那里；否则压入栈中
         */
        super.toMips();

        Value addr = operandList.get(0);

        Register mipsRegister = RegisterManager.instance().getRegOf(this);
        boolean noRegAllocated = mipsRegister == null;
        if (noRegAllocated) {
            mipsRegister = RegisterManager.k0;
        }

        if (addr instanceof LocalDecl || addr instanceof GlobalDecl) {
            MipsManager.instance().putDeclaredVarIntoRegister(addr, mipsRegister);
        } else {
            //addr就是一个实际的内存地址，其值是一个临时变量
            new Lw(mipsRegister, 0, MipsManager.instance().getTempVarByRegister(addr, RegisterManager.k1));
        }

        if (noRegAllocated) {
            MipsManager.instance().pushTempVar(this, mipsRegister);
        }
    }

    public ArrayList<String> GVNHash() {
        return null;
    }

    public ArrayList<User> getUserList() {
        //use这个指令的，以及，对该地址的所有store指令都可以看作是其user
        ArrayList<User> users = new ArrayList<>(userList);
        for (User user : operandList.get(0).getUserList()) {
            if (user instanceof Store) {
                users.add(user);
            }
        }
        return users;
    }

    public void setAddr(Value addr){
        this.addr = addr;
    }
}
