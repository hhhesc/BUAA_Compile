package SyntaxAnalyse.SyntaxTree.Nodes.Exp;

import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class ConstExp extends SyntaxTreeNode {
    public ConstExp(SyntaxTreeNode parent) {
        super(SyntaxNodeType.ConstExp, parent);
    }
}
