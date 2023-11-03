package SyntaxAnalyse.SyntaxTree.Nodes.Stmt;

import ErrorHandler.ErrorManager;
import IntermediatePresentation.IRManager;
import IntermediatePresentation.Instruction.Br;
import IntermediatePresentation.Value;
import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class ContinueStmt extends Stmt {
    public ContinueStmt(SyntaxTreeNode parent) {
        super(SyntaxNodeType.ContinueStmt, parent);
    }

    public void checkError() {
        if (symbolTableManager.notInCycle()) {
            ErrorManager.addError('m', getFirstLeafLineNumber());
        }
    }

    public Value toIR() {
        return new Br(IRManager.getInstance().getContinueTo());
    }
}
