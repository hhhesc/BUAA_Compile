package IntermediatePresentation.Instruction;

import IntermediatePresentation.BasicBlock;
import IntermediatePresentation.ConstNumber;
import IntermediatePresentation.Value;
import TargetCode.Instruction.Jump.Branch;
import TargetCode.Instruction.Jump.J;
import TargetCode.MipsManager;
import TargetCode.RegisterManager;

public class CmpThenBr extends Instruction {
    private final String cond;

    public CmpThenBr(String op, Value oprand1, Value oprand2, BasicBlock ifTrue, BasicBlock ifFalse) {
        super("BR");
        oprand1 = turntoI32WhileBuilding(oprand1);
        use(oprand1);
        oprand2 = turntoI32WhileBuilding(oprand2);
        use(oprand2);
        use(ifTrue);
        use(ifFalse);

        cond = switch (op) {
            case "slt" -> "lt";
            case "sle" -> "le";
            case "sgt" -> "gt";
            case "sge" -> "ge";
            case "eq" -> "eq";
            case "ne" -> "ne";
            default -> "UNKOWNCTBOP";
        };
    }

    public String toString() {
        return "cmpThenBr " + cond + " v1:" + operandList.get(0).getReg() +
                ", v2:" + operandList.get(1).getReg() +
                ", label %" + operandList.get(2).getReg() + ", label %" + operandList.get(3).getReg() + "\n";

    }

    public void toMips() {
        super.toMips();

        Value oprand1 = operandList.get(0);
        Value oprand2 = operandList.get(1);


        BasicBlock ifTrue = (BasicBlock) operandList.get(2);
        BasicBlock ifFalse = (BasicBlock) operandList.get(3);
        if (isConst()) {
            int conVal = toConstNumber().getVal();
            if (conVal == 0) {
                new J(ifFalse.getBbMipsLabel());
            } else {
                new J(ifTrue.getBbMipsLabel());
            }
        } else if (oprand1 instanceof ConstNumber) {
            String oppsiteOp = switch (cond) {
                case "ge" -> "le";
                case "le" -> "ge";
                case "gt" -> "lt";
                case "lt" -> "gt";
                default -> cond;
            };
            new Branch(ifTrue.getBbMipsLabel(),
                    MipsManager.instance().getTempVarByRegister(oprand2, RegisterManager.k1),
                    ((ConstNumber) oprand1).getVal(), oppsiteOp);
            new J(ifFalse.getBbMipsLabel());
        } else if (oprand2 instanceof ConstNumber) {
            new Branch(ifTrue.getBbMipsLabel(),
                    MipsManager.instance().getTempVarByRegister(oprand1, RegisterManager.k1),
                    ((ConstNumber) oprand2).getVal(), cond);
            new J(ifFalse.getBbMipsLabel());
        } else {
            new Branch(ifTrue.getBbMipsLabel(),
                    MipsManager.instance().getTempVarByRegister(oprand1, RegisterManager.k1),
                    MipsManager.instance().getTempVarByRegister(oprand2, RegisterManager.k0), cond);
            new J(ifFalse.getBbMipsLabel());
        }
    }

    public BasicBlock getIfTrue() {
        return (BasicBlock) operandList.get(2);
    }

    public BasicBlock getIfFalse() {
        return (BasicBlock) operandList.get(3);
    }

    public BasicBlock getDest() {
        return null;
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

    public ConstNumber toConstNumber() {
        assert operandList.get(0) instanceof ConstNumber;
        assert operandList.get(1) instanceof ConstNumber;
        int val1 = ((ConstNumber) operandList.get(0)).getVal();
        int val2 = ((ConstNumber) operandList.get(1)).getVal();
        boolean res = switch (cond) {
            case "sgt", "gt" -> val1 > val2;
            case "sle","le" -> val1 <= val2;
            case "sge","ge" -> val1 >= val2;
            case "seq","eq" -> val1 == val2;
            case "sne","ne" -> val1 != val2;
            case "slt","lt" -> val1 < val2;
            default -> false;
        };
        if (res) {
            return new ConstNumber(1);
        } else {
            return new ConstNumber(0);
        }
    }

    public boolean isConst() {
        return operandList.get(0) instanceof ConstNumber && operandList.get(1) instanceof ConstNumber;
    }
}
