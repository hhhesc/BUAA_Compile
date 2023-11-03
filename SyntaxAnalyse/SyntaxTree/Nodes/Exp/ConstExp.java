package SyntaxAnalyse.SyntaxTree.Nodes.Exp;

import IntermediatePresentation.ConstNumber;
import IntermediatePresentation.Instruction.ALU;
import IntermediatePresentation.Value;
import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class ConstExp extends SyntaxTreeNode {
    public ConstExp(SyntaxTreeNode parent) {
        super(SyntaxNodeType.ConstExp, parent);
    }

    public Integer getVal() {
        return ((AddExp) children.get(0)).getVal();
    }

    public Value toIR() {
        Value v = children.get(0).toIR();
        if (!(v instanceof ConstNumber)) {
            return ((ALU) v).toConstNumber();
        }
        return v;
    }
}
