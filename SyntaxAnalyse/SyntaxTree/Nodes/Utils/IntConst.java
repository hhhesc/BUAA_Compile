package SyntaxAnalyse.SyntaxTree.Nodes.Utils;

import IntermediatePresentation.ConstNumber;
import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class IntConst extends SyntaxTreeNode {
    public IntConst(SyntaxTreeNode parent) {
        super(SyntaxNodeType.IntConst, parent);
    }

    public Integer getDim() {
        return 0;
    }

    public ConstNumber toIR() {
        return new ConstNumber(children.get(0).getWord().getSrcStr());
    }
}
