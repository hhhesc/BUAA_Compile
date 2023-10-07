package SyntaxAnalyse.SyntaxTree.Nodes.Decl;

import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class Decl extends SyntaxTreeNode {
    public Decl(SyntaxTreeNode parent) {
        super(SyntaxNodeType.Decl, parent);
    }
}
