package IntermediatePresentation.Instruction;

import IntermediatePresentation.ConstNumber;
import IntermediatePresentation.User;
import IntermediatePresentation.Value;
import IntermediatePresentation.ValueType;
import TargetCode.Instruction.Li;
import TargetCode.Instruction.Memory.Lw;
import TargetCode.Instruction.Memory.Sw;
import TargetCode.Instruction.Move;
import TargetCode.MipsManager;
import TargetCode.Register;
import TargetCode.RegisterManager;

import java.util.ArrayList;

public class MoveIR extends Instruction {
    private final Value originPhi;

    public MoveIR(Phi dest, Value src) {
        super(dest.getReg(), ValueType.I32);
        originPhi = dest;
        dest.addMoveIr(this);
        use(src);
    }

    public String toString() {
        return "move " + reg + ", " + operandList.get(0).getReg() + "\n";
    }

    public Phi getOriginPhi() {
        return (Phi) originPhi;
    }

    public void toMips() {
        super.toMips();
        //move对应的value应该是phi对应的value，从而被其他指令所引用
        Register dest = RegisterManager.instance().getRegOf(originPhi);
        boolean noRegAllocated = dest == null;
        if (noRegAllocated) {
            dest = RegisterManager.k1;
        }

        Value src = operandList.get(0);
        if (src instanceof ConstNumber n) {
            new Li(dest, n.getVal());
        } else {
            Register srcRegister = MipsManager.instance().getTempVarByRegister(src, RegisterManager.k0);
            new Move(dest, srcRegister);
        }

        if (noRegAllocated) {
            new Sw(dest, MipsManager.instance().getLocalVarAddr(originPhi), RegisterManager.sp);
        }
    }

    public boolean isUseless() {
        return originPhi.isUseless() || operandList.get(0).equals(originPhi);
    }

    public ArrayList<User> getUserList() {
        //move的user是原phi指令的user
        return originPhi.getUserList();
    }

    public Value getSrc(){
        return operandList.get(0);
    }
}
