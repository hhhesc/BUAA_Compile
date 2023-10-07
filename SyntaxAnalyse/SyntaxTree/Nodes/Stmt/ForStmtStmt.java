package SyntaxAnalyse.SyntaxTree.Nodes.Stmt;

import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class ForStmtStmt extends SyntaxTreeNode {
    public ForStmtStmt(SyntaxTreeNode parent) {
        super(SyntaxNodeType.ForStmt, parent);
    }
}
