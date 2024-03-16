package IntermediatePresentation.Function;

import IntermediatePresentation.BasicBlock;
import IntermediatePresentation.IRManager;
import IntermediatePresentation.Instruction.Instruction;
import IntermediatePresentation.Instruction.LocalDecl;
import IntermediatePresentation.Instruction.MoveIR;
import IntermediatePresentation.User;
import IntermediatePresentation.Value;
import IntermediatePresentation.ValueType;
import Optimizer.Optimizer;
import TargetCode.Label;
import TargetCode.MipsManager;
import TargetCode.RegisterManager;

import java.util.ArrayList;
import java.util.Comparator;

public class Function extends User {
    protected Param param;
    protected ArrayList<BasicBlock> bbs = new ArrayList<>();

    public static Function getint = new Function("@getint", new Param(), ValueType.I32);
    public static Function putint = new Function("@putint",
            new Param(new Value("i32", ValueType.I32)), ValueType.NULL);

    public Function(String name, Param param, ValueType type) {
        super(name, type);
        use(param);
        this.param = param;
        if (!(name.matches("(@main)|(@getint)|(@putint)|(@putch)|(@putstr)"))) {
            IRManager.getModule().addFunction(this);
        }

        IRManager.getInstance().setCurFunction(this);
        use(new BasicBlock());
    }

    public Function(String name, ValueType type) {
        super(name, type);
        if (!(name.matches("(@main)|(@getint)|(@putint)|(@putch)|(@putstr)"))) {
            IRManager.getModule().addFunction(this);
        }

        IRManager.getInstance().setCurFunction(this);
        use(new BasicBlock());
    }

    public void addBlock(BasicBlock bb) {
        bbs.add(bb);
        use(bb);
    }

    public void addBlockBefore(BasicBlock nextBlock, BasicBlock newBlock) {
        bbs.add(bbs.indexOf(nextBlock), newBlock);
        use(newBlock);
    }

    public void removeBlock(BasicBlock bb) {
        operandList.remove(bb);
        bbs.remove(bb);
    }

    public ArrayList<BasicBlock> getBlocks() {
        return bbs;
    }

    public BasicBlock getEntranceBlock() {
        return bbs.get(0);
    }

    public boolean isVoid() {
        return vType == ValueType.NULL;
    }

    public void setParam(Param param) {
        this.param = param;
        use(param);
    }

    public Param getParam() {
        return param;
    }


    public String toString() {
        StringBuilder sb = new StringBuilder();
        String type = isVoid() ? "void" : "i32";
        sb.append("define dso_local ").append(type).append(" ").append(reg);
        sb.append("(").append(param).append(")").append(" {\n");
        for (BasicBlock bb : bbs) {
            sb.append(bb);
        }
        sb.append("}\n\n");
        return sb.toString();
    }

    public void toMips() {
        super.toMips();
        RegisterManager.instance().setCurFunction(this);

        Label label = new Label("function_" + reg.substring(1));
        MipsManager.instance().insertLabel(label);
        MipsManager.instance().tagFunctionWithLabel(this, label);
        param.toMips();

        //move指令中的dest变量都是有可能未声明的，所以首先遍历move为其声明
        for (BasicBlock bb : bbs) {
            for (Instruction instruction : bb.getInstructionList()) {
                if (instruction instanceof MoveIR moveIR) {
                    if (MipsManager.instance().notInStack(moveIR.getOriginPhi())) {
                        MipsManager.instance().allocInStackBy(moveIR.getOriginPhi(), 1);
                    }
                }
            }
        }

        //遍历支配树进行编译
        if (Optimizer.instance().hasOptimized()) {
            for (BasicBlock bb : Optimizer.instance().bfsDominTreeArray(getEntranceBlock())) {
                bb.toMips();
            }
        } else {
            for (BasicBlock bb : getBlocks()) {
                bb.toMips();
            }
        }
        //清空栈指针
        MipsManager.instance().resetSp();
    }

    public void destroy() {
        for (BasicBlock b : bbs) {
            b.destroy();
        }
        bbs.clear();
    }
}
