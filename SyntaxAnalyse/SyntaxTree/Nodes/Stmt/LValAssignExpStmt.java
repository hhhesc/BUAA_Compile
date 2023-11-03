package SyntaxAnalyse.SyntaxTree.Nodes.Stmt;

import ErrorHandler.ErrorManager;
import IntermediatePresentation.ConstNumber;
import IntermediatePresentation.Instruction.GetElementPtr;
import IntermediatePresentation.Instruction.Load;
import IntermediatePresentation.Instruction.Store;
import IntermediatePresentation.Value;
import SyntaxAnalyse.SyntaxTree.Nodes.Utils.LVal;
import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class LValAssignExpStmt extends Stmt {
    public LValAssignExpStmt(SyntaxTreeNode parent) {
        super(SyntaxNodeType.LValAssignExpStmt, parent);
    }

    public void checkError() {
        LVal lVal = (LVal) children.get(0);
        if (lVal.isConst()) {
            ErrorManager.addError('h', getFirstLeafLineNumber());
        }
        super.checkError();
    }

    public Value toIR() {
        Value lval = children.get(0).toIR();
        Value exp = children.get(2).toIR();

        if (!(lval instanceof GetElementPtr)) {
            if (exp instanceof ConstNumber) {
                symbolTableManager.setVal(getFirstLeafString(), Integer.parseInt(exp.getReg()));
            } else {
                symbolTableManager.setVal(getFirstLeafString(), null);
            }
        }
        return new Store(exp, lval);
    }
}
