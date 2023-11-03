package SyntaxAnalyse.SyntaxTree.Nodes.Utils;

import SyntaxAnalyse.SyntaxTree.Nodes.Stmt.ReturnStmt;
import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class Block extends SyntaxTreeNode {
    public Block(SyntaxTreeNode parent) {
        super(SyntaxNodeType.Block, parent);
    }

    public boolean notExistReturnStmt() {
        SyntaxTreeNode blockItem = children.get(children.size() - 2);
        if (blockItem.getChildren().size() == 0) {
            return true;
        }
        SyntaxTreeNode ret = blockItem.getChildren().get(0);
        return !(ret instanceof ReturnStmt);
    }


}
