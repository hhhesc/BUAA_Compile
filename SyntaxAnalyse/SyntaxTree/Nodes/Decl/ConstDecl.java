package SyntaxAnalyse.SyntaxTree.Nodes.Decl;

import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class ConstDecl extends SyntaxTreeNode {
    public ConstDecl(SyntaxTreeNode parent) {
        super(SyntaxNodeType.ConstDecl, parent);
    }
}
