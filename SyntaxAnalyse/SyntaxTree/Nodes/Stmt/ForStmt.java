package SyntaxAnalyse.SyntaxTree.Nodes.Stmt;

import IntermediatePresentation.BasicBlock;
import IntermediatePresentation.ConstNumber;
import IntermediatePresentation.IRManager;
import IntermediatePresentation.Instruction.Br;
import IntermediatePresentation.Instruction.Store;
import IntermediatePresentation.Value;
import SyntaxAnalyse.SyntaxTree.Nodes.Exp.Cond;
import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class ForStmt extends Stmt {
    public ForStmt(SyntaxTreeNode parent) {
        super(SyntaxNodeType.ForStmt, parent);
    }

    public Value toIR() {
        Value lval = children.get(0).toIR();
        Value exp = children.get(2).toIR();

        if (exp instanceof ConstNumber) {
            symbolTableManager.setVal(getFirstLeafString(), Integer.parseInt(exp.getReg()));
        } else {
            symbolTableManager.setVal(getFirstLeafString(), null);
        }
        return new Store(exp, lval);
    }

}
