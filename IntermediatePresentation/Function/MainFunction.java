package IntermediatePresentation.Function;

import IntermediatePresentation.BasicBlock;
import IntermediatePresentation.IRManager;
import IntermediatePresentation.Instruction.Instruction;
import IntermediatePresentation.Instruction.LocalDecl;
import IntermediatePresentation.Instruction.MoveIR;
import IntermediatePresentation.ValueType;
import Optimizer.Optimizer;
import TargetCode.Label;
import TargetCode.MipsManager;
import TargetCode.RegisterManager;

import java.util.ArrayList;
import java.util.Comparator;

public class MainFunction extends Function {
    public MainFunction() {
        super("@main", new Param(), ValueType.I32);
        IRManager.getModule().setMainFunction(this);
    }

    public void toMips() {
        RegisterManager.instance().setCurFunction(this);
        Label mainLabel = new Label("main");

        MipsManager.instance().insertLabel(mainLabel);

        for (BasicBlock bb : bbs) {
            for (Instruction instruction : bb.getInstructionList()) {
                if (instruction instanceof MoveIR moveIR) {
                    if (MipsManager.instance().notInStack(moveIR.getOriginPhi())) {
                        MipsManager.instance().allocInStackBy(moveIR.getOriginPhi(), 1);
                    }
                }
            }
        }
        if (Optimizer.instance().hasOptimized()) {
            for (BasicBlock bb : Optimizer.instance().bfsDominTreeArray(getEntranceBlock())) {
                bb.toMips();
            }
        } else {
            for (BasicBlock bb : getBlocks()) {
                bb.toMips();
            }
        }
    }
}
