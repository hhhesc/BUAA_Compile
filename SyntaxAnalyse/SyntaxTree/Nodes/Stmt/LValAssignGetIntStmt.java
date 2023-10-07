package SyntaxAnalyse.SyntaxTree.Nodes.Stmt;

import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class LValAssignGetIntStmt extends SyntaxTreeNode {
    public LValAssignGetIntStmt(SyntaxTreeNode parent) {
        super(SyntaxNodeType.LValAssignGetIntStmt, parent);
    }
}
