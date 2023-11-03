package SyntaxAnalyse.SyntaxTree.Nodes.Utils;

import IntermediatePresentation.Instruction.Instruction;
import IntermediatePresentation.Value;
import SyntaxAnalyse.SyntaxTree.Nodes.Stmt.Stmt;
import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class BlockItem extends SyntaxTreeNode {
    public BlockItem(SyntaxTreeNode parent) {
        super(SyntaxNodeType.BlockItem, parent);
    }

    public Value toIR() {
        return children.get(0).toIR();
    }
}
