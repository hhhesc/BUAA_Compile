package SyntaxAnalyse.SyntaxTree.Nodes.Stmt;

import ErrorHandler.ErrorManager;
import IntermediatePresentation.Function.Function;
import IntermediatePresentation.Instruction.Call;
import IntermediatePresentation.Instruction.GetElementPtr;
import IntermediatePresentation.Instruction.Store;
import IntermediatePresentation.Value;
import SyntaxAnalyse.SyntaxTree.Nodes.Utils.LVal;
import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

import java.util.ArrayList;

// LVal '=' 'getint''('')'';'
public class LValAssignGetIntStmt extends Stmt {
    public LValAssignGetIntStmt(SyntaxTreeNode parent) {
        super(SyntaxNodeType.LValAssignGetIntStmt, parent);
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
        String ident = getFirstLeafString();
        symbolTableManager.setVal(ident, null);
        new Store(new Call(Function.getint, new ArrayList<>()), lval);
        return null;
    }
}
