package SyntaxAnalyse.SyntaxTree.Nodes.Stmt;

import ErrorHandler.ErrorManager;
import IntermediatePresentation.IRManager;
import IntermediatePresentation.Instruction.Load;
import IntermediatePresentation.Instruction.Ret;
import IntermediatePresentation.Value;
import SyntaxAnalyse.SyntaxTree.Nodes.Exp.Exp;
import SyntaxAnalyse.SyntaxTree.Nodes.Exp.LAndExp;
import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class ReturnStmt extends Stmt {
    public ReturnStmt(SyntaxTreeNode parent) {
        super(SyntaxNodeType.ReturnStmt, parent);
    }

    public void checkError() {
        // check Error f
        for (SyntaxTreeNode child : children) {
            if (child instanceof Exp) {
                if (symbolTableManager.lastFuncIsVoid()) {
                    ErrorManager.addError('f', getFirstLeafLineNumber());
                }
                break;
            }
        }

        super.checkError();
    }

    public Ret toIR() {
        if (children.get(1) instanceof Exp exp) {
            Value retVal = exp.toIR();
            if (retVal.isPointer()) {
                retVal = new Load(IRManager.getInstance().declareTempVar(), retVal);
            }
            return new Ret(retVal);
        } else {
            return new Ret();
        }
    }
}
