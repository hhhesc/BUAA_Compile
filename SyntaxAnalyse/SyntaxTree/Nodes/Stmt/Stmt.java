package SyntaxAnalyse.SyntaxTree.Nodes.Stmt;

import IntermediatePresentation.BasicBlock;
import IntermediatePresentation.Value;
import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class Stmt extends SyntaxTreeNode {
    public Stmt(SyntaxNodeType type,SyntaxTreeNode parent) {
        super(type, parent);
    }

    public Value toIR(){
        return new BasicBlock();
    }
}
