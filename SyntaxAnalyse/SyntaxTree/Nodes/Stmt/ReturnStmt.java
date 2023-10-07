package SyntaxAnalyse.SyntaxTree.Nodes.Stmt;

import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class ReturnStmt extends SyntaxTreeNode {
    public ReturnStmt(SyntaxTreeNode parent) {
        super(SyntaxNodeType.ReturnStmt, parent);
    }
}
