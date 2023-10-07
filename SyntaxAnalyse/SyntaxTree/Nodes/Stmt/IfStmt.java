package SyntaxAnalyse.SyntaxTree.Nodes.Stmt;

import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class IfStmt extends SyntaxTreeNode {
    public IfStmt(SyntaxTreeNode parent) {
        super(SyntaxNodeType.IfStmt, parent);
    }
}
