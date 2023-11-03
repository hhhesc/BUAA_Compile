package SyntaxAnalyse.SyntaxTree.Nodes.Utils;

import IntermediatePresentation.ConstNumber;
import IntermediatePresentation.Value;
import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class Number extends SyntaxTreeNode {
    public Number(SyntaxTreeNode parent) {
        super(SyntaxNodeType.Number, parent);
    }

    public Integer getDim() {
        return 0;
    }

    public ConstNumber toIR() {
        return (ConstNumber) children.get(0).toIR();
    }
}
