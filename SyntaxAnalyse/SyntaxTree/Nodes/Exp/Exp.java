package SyntaxAnalyse.SyntaxTree.Nodes.Exp;

import IntermediatePresentation.ConstNumber;
import IntermediatePresentation.Value;
import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class Exp extends SyntaxTreeNode {
    public Exp(SyntaxTreeNode parent) {
        super(SyntaxNodeType.Exp, parent);
    }

    public Integer getDim() {
        for (SyntaxTreeNode child : children) {
            if (child.getDim() != null) return child.getDim();
        }
        return null;
    }

    public Integer getVal() {
        return ((AddExp) children.get(0)).getVal();
    }

    public Value toIR() {
        try {
            int val = getVal();
            return new ConstNumber(val);
        } catch (NullPointerException e) {
            return children.get(0).toIR();
        }
    }
}
