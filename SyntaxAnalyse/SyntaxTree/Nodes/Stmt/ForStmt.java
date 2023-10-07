package SyntaxAnalyse.SyntaxTree.Nodes.Stmt;

import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class ForStmt extends SyntaxTreeNode {
    public ForStmt(SyntaxTreeNode parent) {
        super(SyntaxNodeType.TheForStmt, parent);
    }
}
