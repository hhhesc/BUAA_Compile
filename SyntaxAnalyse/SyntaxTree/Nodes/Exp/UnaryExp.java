package SyntaxAnalyse.SyntaxTree.Nodes.Exp;

import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class UnaryExp extends SyntaxTreeNode {
    public UnaryExp(SyntaxTreeNode parent) {
        super(SyntaxNodeType.UnaryExp, parent);
    }
}
