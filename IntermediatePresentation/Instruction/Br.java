package IntermediatePresentation.Instruction;

import IntermediatePresentation.BasicBlock;
import IntermediatePresentation.Value;
import TargetCode.Instruction.Jump.Bne;
import TargetCode.Instruction.Jump.J;
import TargetCode.MipsManager;
import TargetCode.RegisterManager;

public class Br extends Instruction {
    private Value cond;
    public BasicBlock ifTrue;
    private BasicBlock ifFalse;
    private BasicBlock dest = null;

    public Br(Value cond, BasicBlock ifTrue, BasicBlock ifFalse) {
        super("BR");
        this.cond = cond;
        use(cond);
        this.ifTrue = ifTrue;
        use(ifTrue);
        this.ifFalse = ifFalse;
        use(ifFalse);
    }

    public Br(BasicBlock dest) {
        super("BR");
        this.dest = dest;
        use(dest);
    }

    public String toString() {
        if (dest == null) {
            return "br i1 " + cond.getReg() + ", label %" + ifTrue.getReg() +
                    ", label %" + ifFalse.getReg() + "\n";
        } else {
            return "br label %" + dest.getReg() + "\n";
        }
    }

    public void toMips() {
        super.toMips();
        if (dest == null) {
            //br cond, label1, label2 -> bne label1,$zero,src | j label2
            new Bne(ifTrue.getBbMipsLabel(), RegisterManager.zero,
                    MipsManager.instance().getTempVarByRegister(cond,RegisterManager.k0));
            new J(ifFalse.getBbMipsLabel());
        } else {
            new J(dest.getBbMipsLabel());
        }
    }
}
