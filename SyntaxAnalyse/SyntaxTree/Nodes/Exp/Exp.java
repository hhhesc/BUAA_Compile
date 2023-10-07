package SyntaxAnalyse.SyntaxTree.Nodes.Exp;

import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class Exp extends SyntaxTreeNode {
    public Exp(SyntaxTreeNode parent) {
        super(SyntaxNodeType.Exp, parent);
    }
}
