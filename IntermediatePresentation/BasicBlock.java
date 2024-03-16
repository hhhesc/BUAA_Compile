package IntermediatePresentation;

import ErrorHandler.SymbolTable.SymbolTableManager;
import IntermediatePresentation.Function.Function;
import IntermediatePresentation.Instruction.Br;
import IntermediatePresentation.Instruction.Instruction;
import IntermediatePresentation.Instruction.MoveIR;
import IntermediatePresentation.Instruction.Phi;
import TargetCode.Label;
import TargetCode.MipsManager;

import java.util.ArrayList;

public class BasicBlock extends Value {
    private ArrayList<Instruction> instructionList = new ArrayList<>();
    private static int bbCounter = -2;
    private final Label bbMipsLabel;

    //代码生成之后，优化阶段加入的基本块也就只有消除phi的时候加入的中间块，这个不需要做GCM，所以默认为-1即可
    private int loopDepth;

    public BasicBlock() {
        super(IRManager.getInstance().declareBlock(), ValueType.NULL);
        IRManager.getInstance().setCurBlock(this);
        IRManager.getInstance().addBBToCurFunction(this);
        bbMipsLabel = new Label("b" + bbCounter);
//        loopDepth = SymbolTableManager.getInstance().getCycleLevel();
        loopDepth = 0;
        bbCounter++;
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
        instruction.setBlock(this);
    }

    public void addInstrAtEntry(Instruction instruction) {
        instructionList.add(0, instruction);
        instruction.setBlock(this);
    }

    public void addInstructionBeforeBranch(Instruction instruction) {
        //最后一个语句一定是跳转指令
        instructionList.add(instructionList.size() - 1, instruction);
        instruction.setBlock(this);
    }

    public void addInstructionBeforeMove(Instruction instruction) {
        int idx = -1;
        for (int i = 0; i < instructionList.size(); i++) {
            if (instructionList.get(i) instanceof MoveIR) {
                idx = i;
                break;
            }
        }
        if (idx == -1) {
            addInstructionBeforeBranch(instruction);
        } else {
            instructionList.add(idx, instruction);
        }
        instruction.setBlock(this);
    }

    public void addInstructionAt(int index, Instruction instruction) {
        instructionList.add(index, instruction);
        instruction.setBlock(this);
    }

    public void redirectTo(BasicBlock originBlock, BasicBlock nextBlock) {
        //修改末尾的跳转方向为新的nextBlock
        Instruction instruction = getLastInstruction();
        if (instruction instanceof Br br) {
            br.redirectTo(originBlock, nextBlock);
        }
        //修改原本的后继块中phi指令的来源
        for (Instruction instr : originBlock.instructionList) {
            if (instr instanceof Phi phi && phi.operandList.contains(this)) {
                phi.redirectFrom(this, nextBlock);
            }
        }
    }

    public ArrayList<Instruction> getInstructionList() {
        return instructionList;
    }

    public void deleteInstruction() {
        instructionList.remove(instructionList.size() - 1);
    }

    public void removeInstruction(Instruction instruction) {
        instructionList.remove(instruction);
    }

    public void updataInstructionList(ArrayList<Instruction> instructions) {
        instructionList = instructions;
    }

    public Instruction getLastInstruction() {
        return instructionList.get(instructionList.size() - 1);
    }

    public void toMips() {
        MipsManager.instance().tagBBWithLabel(bbMipsLabel);
        for (Instruction instruction : instructionList) {
            instruction.toMips();
        }
    }

    public Label getBbMipsLabel() {
        return bbMipsLabel;
    }

    public Function getFunction() {
        for (Value v : userList) {
            if (v instanceof Function f) {
                return f;
            }
        }
        return null;
    }

    public void destroy() {
        for (Instruction instruction : instructionList) {
            instruction.destroy();
        }
    }

    public int getLoopDepth() {
        return loopDepth;
    }

    public void setLoopDepth(int loopDepth) {
        this.loopDepth = loopDepth;
    }
}
