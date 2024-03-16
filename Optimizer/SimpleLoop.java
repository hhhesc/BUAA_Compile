package Optimizer;

import IntermediatePresentation.BasicBlock;

import java.util.HashSet;

public class SimpleLoop {
    private final BasicBlock header;
    private final BasicBlock latch;
    private HashSet<BasicBlock> blocksInLoop;

    public SimpleLoop(BasicBlock header, BasicBlock latch) {
        this.header = header;
        this.latch = latch;
        genLoopBlocks();
    }

    private void genLoopBlocks() {
        blocksInLoop = Optimizer.instance().getCFG().mayPassingBy(header, latch, new HashSet<>());
        blocksInLoop.add(latch);
    }

    public HashSet<BasicBlock> getBlocksInLoop() {
        return blocksInLoop;
    }
}
