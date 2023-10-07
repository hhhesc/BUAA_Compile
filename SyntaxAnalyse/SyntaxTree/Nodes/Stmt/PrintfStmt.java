package SyntaxAnalyse.SyntaxTree.Nodes.Stmt;

import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class PrintfStmt extends SyntaxTreeNode {
    public PrintfStmt(SyntaxTreeNode parent) {
        super(SyntaxNodeType.PrintfStmt, parent);
    }
}
