package SyntaxAnalyse.SyntaxTree.Nodes.Exp;

import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class Cond extends SyntaxTreeNode {
    public Cond(SyntaxTreeNode parent) {
        super(SyntaxNodeType.Cond, parent);
    }
}
