package IntermediatePresentation.Instruction;

import IntermediatePresentation.BasicBlock;
import IntermediatePresentation.ConstNumber;
import IntermediatePresentation.Value;
import TargetCode.Instruction.Jump.Branch;
import TargetCode.Instruction.Jump.J;
import TargetCode.MipsManager;
import TargetCode.RegisterManager;

public class Br extends Instruction {

    public Br(Value cond, BasicBlock ifTrue, BasicBlock ifFalse) {
        super("BR");
        if (cond instanceof ConstNumber n) {
            int con = n.getVal();
            if (con == 0) {
                use(ifFalse);
            } else {
                use(ifTrue);
            }
        } else {
            use(cond);
            use(ifTrue);
            use(ifFalse);
        }
    }

    public Br(BasicBlock dest) {
        super("BR");
        use(dest);
    }

    public String toString() {
        Value cond = operandList.get(0);
        if (operandList.size() == 3) {
            return "br i1 " + cond.getReg() + ", label %" + operandList.get(1).getReg() +
                    ", label %" + operandList.get(2).getReg() + "\n";
        } else {
            return "br label %" + getDest().getReg() + "\n";
        }
    }

    public void toMips() {
        super.toMips();
        Value cond = operandList.get(0);

        if (operandList.size() == 3) {
            //br cond, label1, label2 -> bne label1,$zero,src | j label2
            BasicBlock ifTrue = (BasicBlock) operandList.get(1);
            BasicBlock ifFalse = (BasicBlock) operandList.get(2);
            if (cond instanceof ConstNumber n) {
                int conVal = n.getVal();
                if (conVal == 0) {
                    new J(ifFalse.getBbMipsLabel());
                } else {
                    new J(ifTrue.getBbMipsLabel());
                }
            } else {
                new Branch(ifTrue.getBbMipsLabel(), RegisterManager.zero,
                        MipsManager.instance().getTempVarByRegister(cond, RegisterManager.k0),"ne");
                new J(ifFalse.getBbMipsLabel());
            }
        } else {
            new J(getDest().getBbMipsLabel());
        }
    }

    public BasicBlock getIfTrue() {
        return (BasicBlock) operandList.get(1);
    }

    public BasicBlock getIfFalse() {
        return (BasicBlock) operandList.get(2);
    }

    public BasicBlock getDest() {
        if (operandList.get(0) instanceof BasicBlock) {
            return (BasicBlock) operandList.get(0);
        } else {
            return null;
        }
    }

    public Value getCond() {
        if (operandList.get(0) instanceof BasicBlock) {
            return null;
        } else {
            return operandList.get(0);
        }
    }

    public void redirectTo(BasicBlock originBlock, BasicBlock block) {
        for (int i = 0; i < operandList.size(); i++) {
            if (operandList.get(i).equals(originBlock)) {
                originBlock.removeUser(this);
                operandList.set(i, block);
                block.usedBy(this);
                return;
            }
        }
    }

    public boolean isUseless() {
        return false;
    }

    public boolean isDefInstr() {
        return false;
    }
}
