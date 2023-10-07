package SyntaxAnalyse.SyntaxTree.Nodes.Exp;

import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class UnaryOp extends SyntaxTreeNode {
    public UnaryOp(SyntaxTreeNode parent) {
        super(SyntaxNodeType.UnaryOp, parent);
    }
}
