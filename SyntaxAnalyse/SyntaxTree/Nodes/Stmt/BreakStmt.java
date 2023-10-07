package SyntaxAnalyse.SyntaxTree.Nodes.Stmt;

import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class BreakStmt extends SyntaxTreeNode {
    public BreakStmt(SyntaxTreeNode parent){
        super(SyntaxNodeType.BreakStmt,parent);
    }
}
