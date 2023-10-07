package SyntaxAnalyse.SyntaxTree.Nodes.Exp;

import SyntaxAnalyse.Parser;
import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class PrimaryExp extends SyntaxTreeNode {
    public PrimaryExp(SyntaxTreeNode parent) {
        super(SyntaxNodeType.PrimaryExp, parent);
    }

}
