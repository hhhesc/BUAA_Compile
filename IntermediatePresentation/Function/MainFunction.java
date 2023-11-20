package IntermediatePresentation.Function;

import IntermediatePresentation.BasicBlock;
import IntermediatePresentation.IRManager;
import IntermediatePresentation.ValueType;
import TargetCode.Instruction.Jump.J;
import TargetCode.Instruction.Jump.Jal;
import TargetCode.Label;
import TargetCode.MipsManager;

public class MainFunction extends Function {
    public MainFunction() {
        super("@main", new Param(), ValueType.I32);
        IRManager.getModule().setMainFunction(this);
    }

    public void toMips() {
        Label mainLabel = new Label("main");
        Label end = new Label("end");

        MipsManager.instance().insertLabel(mainLabel);
        for (BasicBlock bb : bbs) {
            bb.toMips();
        }
        MipsManager.instance().insertLabel(end);
    }
}
