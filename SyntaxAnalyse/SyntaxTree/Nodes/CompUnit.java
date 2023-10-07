package SyntaxAnalyse.SyntaxTree.Nodes;

import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class CompUnit extends SyntaxTreeNode {
    public CompUnit(SyntaxTreeNode parent){
        super(SyntaxNodeType.CompUnit,parent);
    }
}
