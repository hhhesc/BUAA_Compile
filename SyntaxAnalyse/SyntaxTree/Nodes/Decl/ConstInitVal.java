package SyntaxAnalyse.SyntaxTree.Nodes.Decl;

import IntermediatePresentation.Array.ArrayInitializer;
import IntermediatePresentation.ConstNumber;
import IntermediatePresentation.Value;
import SyntaxAnalyse.SyntaxTree.Nodes.Exp.ConstExp;
import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

import java.util.ArrayList;

public class ConstInitVal extends SyntaxTreeNode {
    public ConstInitVal(SyntaxTreeNode parent) {
        super(SyntaxNodeType.ConstInitVal, parent);
    }

    public int getVal() {
        return ((ConstExp) children.get(0)).getVal();
    }

    public Value toIR() {
        //constInitVal必须返回constNumber
        if (children.size() == 1) {
            return children.get(0).toIR();
        } else {
            ArrayInitializer initializer = new ArrayInitializer(new ArrayList<>());
            for (SyntaxTreeNode child : children) {
                if (child instanceof ConstInitVal) {
                    Value v = child.toIR();
                    if (v instanceof ArrayInitializer aiv) {
                        initializer.merge(aiv);
                    } else {
                        initializer.add(v);
                    }
                }
            }
            return initializer;
        }
    }
}
