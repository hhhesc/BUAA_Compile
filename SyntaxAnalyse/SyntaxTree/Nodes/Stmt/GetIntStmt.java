package SyntaxAnalyse.SyntaxTree.Nodes.Stmt;

import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class GetIntStmt extends Stmt {
    public GetIntStmt(SyntaxTreeNode parent) {
        super(SyntaxNodeType.GetIntStmt, parent);
    }
}
