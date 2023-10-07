package SyntaxAnalyse.SyntaxTree.Nodes.Stmt;

import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class LValAssignExpStmt extends SyntaxTreeNode {
    public LValAssignExpStmt(SyntaxTreeNode parent){
        super(SyntaxNodeType.LValAssignExpStmt,parent);
    }
}
