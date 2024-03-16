package IntermediatePresentation.Instruction;

import IntermediatePresentation.BasicBlock;
import IntermediatePresentation.User;
import IntermediatePresentation.Value;
import IntermediatePresentation.ValueType;

import java.util.ArrayList;
import java.util.HashSet;

public class Phi extends Instruction {
    private final LocalDecl originAddr;
    private final HashSet<MoveIR> toMoveIrs = new HashSet<>();

    private Phi phiTmp = null;

    public Phi(LocalDecl originAddr, String reg) {
        super(reg, ValueType.I32);
        this.originAddr = originAddr;
        if (!reg.endsWith("tmp")) {
            phiTmp = new Phi(new LocalDecl(), reg + "_tmp");
        }
    }

    public Phi getPhiTmp() {
        return phiTmp;
    }


    public void addCond(Value val, BasicBlock label) {
        use(val);
        use(label);
    }

    public ArrayList<BasicBlock> getSrcBlockWhen(Value value) {
        ArrayList<BasicBlock> sources = new ArrayList<>();
        for (int i = 0; i < operandList.size(); i += 2) {
            if (operandList.get(i).equals(value)) {
                sources.add((BasicBlock) operandList.get(i + 1));
            }
        }
        return sources;
    }

    public LocalDecl getPhiAddr() {
        return originAddr;
    }

    public Value valueFromBlock(BasicBlock label) {
        assert operandList.size() % 2 == 0;
        for (int i = 1; i < operandList.size(); i += 2) {
            Value v = operandList.get(i);
            if (v.equals(label)) {
                return operandList.get(i - 1);
            }
        }
        return null;
    }

    public String toString() {
        int size = operandList.size();
        assert size % 2 == 0;
        StringBuilder sb = new StringBuilder();
        sb.append(reg).append(" = ");
        sb.append("phi i32 ");
        for (int i = 0; i < size; i += 2) {
            sb.append("[ ").append(operandList.get(i).getReg()).append(", %")
                    .append(operandList.get(i + 1).getReg()).append("], ");
        }
        sb.delete(sb.length() - 2, sb.length());
        sb.append("\n");
        return sb.toString();
    }

    public void redirectFrom(BasicBlock originBlock, BasicBlock midBlock) {
        originBlock.removeUser(this);
        int idx = operandList.indexOf(originBlock);
        operandList.set(idx, midBlock);
        midBlock.usedBy(this);
    }

    public void removeOperand(Value value) {
        if (!operandList.contains(value)) {
            return;
        }
        if (value instanceof BasicBlock) {
            operandList.remove(operandList.indexOf(value) - 1);
            operandList.remove(value);
        } else if (value instanceof LocalDecl) {
            operandList.remove(value);
        } else {
            operandList.remove(operandList.indexOf(value) + 1);
            operandList.remove(value);
        }
    }

    public void addMoveIr(MoveIR moveIR) {
        toMoveIrs.add(moveIR);
    }

    public HashSet<MoveIR> getMoveIrs() {
        return toMoveIrs;
    }
}
