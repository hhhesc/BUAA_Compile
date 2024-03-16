package Optimizer;

import IntermediatePresentation.BasicBlock;
import IntermediatePresentation.Function.Function;
import IntermediatePresentation.Instruction.Instruction;
import IntermediatePresentation.Module;
import IntermediatePresentation.Value;

import java.util.ArrayList;
import java.util.HashMap;

public class GVN {
    //类似于mem2reg，只需要做一个hash替换即可（先前的SSA以及后续的GCM会处理正确性问题）
    private final Module module;
    private final HashMap<String, Value> globalValueNumberMap = new HashMap<>();

    public GVN() {
        this.module = Optimizer.instance().getModule();
    }

    public void optimize() {
        for (Function function : module.getAllFunctions()) {
            //全局编号是针对于函数而言的全局
            globalValueNumberMap.clear();
            numberForFunction(function);
        }
    }

    public void numberForFunction(Function function) {
        ArrayList<BasicBlock> dominTreePath = Optimizer.instance().bfsDominTreeArray(function.getEntranceBlock());
        for (BasicBlock block : dominTreePath) {
            ArrayList<Instruction> instructions = new ArrayList<>(block.getInstructionList());
            for (Instruction instruction : instructions) {
                ArrayList<String> hashs = instruction.GVNHash();
                if (hashs != null) {
                    boolean containsHash = false;
                    for (String hash : hashs) {
                        if (globalValueNumberMap.containsKey(hash)) {
                            containsHash = true;
                            //替换这个表达式，并维护use-def关系，类似于mem2reg时
                            instruction.beReplacedBy(globalValueNumberMap.get(hash));
                            block.removeInstruction(instruction);
                            instruction.destroy();
                            break;
                        }
                    }
                    if (!containsHash) {
                        globalValueNumberMap.put(hashs.get(0), instruction);
                    }
                }
            }
        }
    }
}
