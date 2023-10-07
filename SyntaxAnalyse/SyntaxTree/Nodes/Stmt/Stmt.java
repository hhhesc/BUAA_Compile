package SyntaxAnalyse.SyntaxTree.Nodes.Stmt;

import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class Stmt extends SyntaxTreeNode {
    public Stmt(SyntaxTreeNode parent) {
        super(SyntaxNodeType.Stmt, parent);
    }
}
