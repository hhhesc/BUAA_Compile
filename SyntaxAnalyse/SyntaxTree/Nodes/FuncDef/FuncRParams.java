package SyntaxAnalyse.SyntaxTree.Nodes.FuncDef;

import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

import java.util.ArrayList;

public class FuncRParams extends SyntaxTreeNode {
    public FuncRParams(SyntaxTreeNode parent) {
        super(SyntaxNodeType.FuncRParams, parent);
    }

    public ArrayList<Integer> getParamDims() {
        ArrayList<Integer> dims = new ArrayList<>();
        for (SyntaxTreeNode child : children) {
            if (child.getDim() != null) dims.add(child.getDim());
        }
        return dims;
    }
}
