package SyntaxAnalyse.SyntaxTree.Nodes.Stmt;

import IntermediatePresentation.Value;
import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class BlockStmt extends Stmt {
    public BlockStmt(SyntaxTreeNode parent) {
        super(SyntaxNodeType.BlockStmt, parent);
    }

    public void checkError() {
        symbolTableManager.enterBlock();
        super.checkError();
        symbolTableManager.exitBlock();
    }

    public Value toIR() {
        symbolTableManager.enterBlock();
        /*
            BasicBlock和Block不是一回事
            bb是不包含跳转的一段中间代码，Block是表示作用域限定的一段源代码
        */
        Value ret = children.get(0).toIR();
        symbolTableManager.exitBlock();
        return ret;
    }
}
