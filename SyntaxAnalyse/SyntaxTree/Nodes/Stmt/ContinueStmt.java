package SyntaxAnalyse.SyntaxTree.Nodes.Stmt;

import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class ContinueStmt extends SyntaxTreeNode {
    public ContinueStmt(SyntaxTreeNode parent) {
        super(SyntaxNodeType.ContinueStmt, parent);
    }
}
