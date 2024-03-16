package SyntaxAnalyse.SyntaxTree.Nodes.Stmt;

import IntermediatePresentation.BasicBlock;
import IntermediatePresentation.IRManager;
import IntermediatePresentation.Instruction.Br;
import IntermediatePresentation.Value;
import SyntaxAnalyse.SyntaxTree.Nodes.Exp.Cond;
import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class TheForStmt extends Stmt {
    public TheForStmt(SyntaxTreeNode parent) {
        super(SyntaxNodeType.TheForStmt, parent);
    }

    public void checkError() {
        symbolTableManager.enterCycle();
        super.checkError();
        symbolTableManager.exitCycle();
    }

    public Value toIR() {
        symbolTableManager.enterCycle();
        ForStmt init = null, incr = null;
        Cond cond = null;
        Stmt stmt = null;
        int paraPos = 0;
        for (SyntaxTreeNode child : children) {
            if (child instanceof ForStmt && paraPos == 0) {
                init = ((ForStmt) child);
            } else if (child instanceof ForStmt && paraPos == 2) {
                incr = ((ForStmt) child);
            } else if (child instanceof Cond) {
                cond = ((Cond) child);
            } else if (child.isLeaf() && child.getFirstLeafString().equals(";")) {
                paraPos++;
            } else if (child instanceof Stmt && paraPos == 2) {
                stmt = ((Stmt) child);
            }
        }

        //准备所需的子节点

        if (init != null) {
            init.toIR();
        }

        BasicBlock curBlock = IRManager.getInstance().getCurBlock();
        BasicBlock condBlock = new BasicBlock();
        //follow不是循环内部的块
        BasicBlock followBlock = new BasicBlock();
        followBlock.setLoopDepth(followBlock.getLoopDepth() - 1);
        BasicBlock stmtBlock = new BasicBlock();

        symbolTableManager.enterBlock();
        IRManager.getInstance().addBreakTo(followBlock);

        IRManager.getInstance().setCurBlock(curBlock);
        new Br(condBlock);

        IRManager.getInstance().setCurBlock(condBlock);
        if (cond != null) {
            cond.condToIR(stmtBlock, followBlock);
        } else {
            new Br(stmtBlock);
        }

        if (incr != null) {
            BasicBlock incrBlock = new BasicBlock();
            IRManager.getInstance().addContinueTo(incrBlock);

            IRManager.getInstance().setCurBlock(stmtBlock);
            if (stmt != null) {
                stmt.toIRThenBrTo(incrBlock);
            } else {
                new Br(incrBlock);
            }

            IRManager.getInstance().setCurBlock(incrBlock);
            incr.toIR();
            if (cond != null) {
                cond.condToIR(stmtBlock, followBlock);
            } else {
                new Br(stmtBlock);
            }
        } else {
            IRManager.getInstance().addContinueTo(condBlock);
            IRManager.getInstance().setCurBlock(stmtBlock);
            if (stmt != null) {
                stmt.toIR();
            }

            if (cond != null) {
                cond.condToIR(stmtBlock, followBlock);
            } else {
                new Br(stmtBlock);
            }
        }

        IRManager.getInstance().setCurBlock(followBlock);
        symbolTableManager.exitBlock();
        symbolTableManager.exitCycle();
        return null;
    }
}
