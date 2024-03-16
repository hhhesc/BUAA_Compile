package Optimizer;

import IntermediatePresentation.BasicBlock;
import IntermediatePresentation.Function.Function;
import IntermediatePresentation.Module;

import java.util.ArrayList;
import java.util.HashSet;

public class LoopAnalyze {
    private final Module module;

    private final HashSet<SimpleLoop> loops = new HashSet<>();

    public LoopAnalyze() {
        this.module = Optimizer.instance().getModule();
        for (Function f : module.getAllFunctions()) {
            for (BasicBlock b : f.getBlocks()) {
                b.setLoopDepth(0);
            }
        }
    }

    public void analyze() {
        //如果a domin b而b->a，则说明存在一个简单循环
        for (Function f : module.getAllFunctions()) {
            for (BasicBlock a : f.getBlocks()) {
                for (BasicBlock b : Optimizer.instance().getDominAnalyzer().getDominees(a)) {
                    HashSet<BasicBlock> children = Optimizer.instance().getCFG().getChildren(b);
                    if (children == null) {
                        continue;
                    }
                    if (children.contains(a)) {
                        SimpleLoop loop = new SimpleLoop(a, b);
                        if (!loops.contains(loop)) {
                            loops.add(loop);
                            for (BasicBlock block : loop.getBlocksInLoop()) {
                                block.setLoopDepth(block.getLoopDepth() + 1);
                            }
                        }
                    }
                }
            }
        }
    }

    public HashSet<SimpleLoop> getLoops() {
        return loops;
    }
}
