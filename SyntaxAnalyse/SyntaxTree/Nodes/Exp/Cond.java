package SyntaxAnalyse.SyntaxTree.Nodes.Exp;

import IntermediatePresentation.BasicBlock;
import IntermediatePresentation.Value;
import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class Cond extends SyntaxTreeNode {
    public Cond(SyntaxTreeNode parent) {
        super(SyntaxNodeType.Cond, parent);
    }

    public void condToIR(BasicBlock ifTrue, BasicBlock ifFalse) {
        ((LOrExp) children.get(0)).condToIR(ifTrue, ifFalse);
    }
}
