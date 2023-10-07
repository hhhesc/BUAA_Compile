package SyntaxAnalyse.SyntaxTree.Nodes.Utils;

import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class Ident extends SyntaxTreeNode {
    public Ident(SyntaxTreeNode parent) {
        super(SyntaxNodeType.Ident, parent);
    }
}
