package SyntaxAnalyse.SyntaxTree.Nodes.Stmt;

import IntermediatePresentation.BasicBlock;
import IntermediatePresentation.IRManager;
import IntermediatePresentation.Value;
import SyntaxAnalyse.SyntaxTree.Nodes.Exp.Cond;
import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class IfStmt extends Stmt {
    public IfStmt(SyntaxTreeNode parent) {
        super(SyntaxNodeType.IfStmt, parent);
    }

    public Value toIR() {
        Cond cond = null;
        Stmt stmt1 = null, stmt2 = null;
        for (SyntaxTreeNode child : children) {
            if (child instanceof Cond) {
                cond = (Cond) child;
            } else if (child instanceof Stmt && stmt1 == null) {
                stmt1 = (Stmt) child;
            } else if (child instanceof Stmt) {
                stmt2 = (Stmt) child;
            }
        }
        if (stmt1 == null) {
            return null;
        }
        assert cond != null;
        //准备子表达式

        BasicBlock ifStmtBlock = IRManager.getInstance().getCurBlock();
        BasicBlock followBlock = new BasicBlock();
        IRManager.getInstance().setCurBlock(ifStmtBlock);
        //设置后继块

        BasicBlock ifTrue = new BasicBlock();
        if (stmt2 == null) {
            IRManager.getInstance().setCurBlock(ifStmtBlock);
            cond.condToIR(ifTrue, followBlock);
            IRManager.getInstance().setCurBlock(ifTrue);
            stmt1.toIRThenBrTo(followBlock);
        } else {
            BasicBlock ifFalse = new BasicBlock();

            IRManager.getInstance().setCurBlock(ifStmtBlock);
            cond.condToIR(ifTrue, ifFalse);
            IRManager.getInstance().setCurBlock(ifTrue);
            stmt1.toIRThenBrTo(followBlock);
            IRManager.getInstance().setCurBlock(ifFalse);
            stmt2.toIRThenBrTo(followBlock);
        }

        IRManager.getInstance().setCurBlock(followBlock);
        return null;
    }
}
