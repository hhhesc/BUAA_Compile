package IntermediatePresentation;

import IntermediatePresentation.Instruction.Instruction;

import java.util.ArrayList;

public class BasicBlock extends Value {
    private final ArrayList<Instruction> instructionList = new ArrayList<>();

    public BasicBlock() {
        super(IRManager.getInstance().declareBlock(), ValueType.NULL);
        IRManager.getInstance().setCurBlock(this);
        IRManager.getInstance().addBBToCurFunction(this);
    }

    public void mergeBasicBlock(BasicBlock basicBlock) {
        instructionList.addAll(basicBlock.getInstructionList());
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(reg).append(":\n");
        for (Instruction instruction : instructionList) {
            sb.append("\t").append(instruction);
        }
        return sb.toString();
    }

    public void addInstruction(Instruction instruction) {
        instructionList.add(instruction);
    }

    public ArrayList<Instruction> getInstructionList() {
        return instructionList;
    }

    public void deleteInstruction() {
        instructionList.remove(instructionList.size() - 1);
    }
}
