package SyntaxAnalyse.SyntaxTree.Nodes.Utils;

import IntermediatePresentation.ConstNumber;
import IntermediatePresentation.Instruction.ALU;
import IntermediatePresentation.Instruction.GetElementPtr;
import IntermediatePresentation.Value;
import SyntaxAnalyse.SyntaxTree.Nodes.Exp.Exp;
import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class LVal extends SyntaxTreeNode {
    //左值表达式 LVal → Ident {'[' Exp ']'} 1.普通变量 2.一维数组 3.二维数组
    private String ident = null;

    public LVal(SyntaxTreeNode parent) {
        super(SyntaxNodeType.LVal, parent);
    }

    public Integer getDim() {
        ident = ((Ident) children.get(0)).getIdent().getSrcStr();
        int dim = symbolTableManager.getDim(ident);
        int arrayDim;
        if (children.size() == 1) {
            arrayDim = 0;
        } else if (children.size() == 4) {
            arrayDim = 1;
        } else {
            arrayDim = 2;
        }
        return dim - arrayDim;
    }

    public boolean isConst() {
        ident = ((Ident) children.get(0)).getIdent().getSrcStr();
        if (ident == null) {
            ident = ((Ident) children.get(0)).getIdent().getSrcStr();
        }
        return symbolTableManager.varIsConst(ident);
    }

    public Integer getVal() {
        Exp firstDim = null, secondDim = null;
        for (SyntaxTreeNode child : children) {
            if (child instanceof Exp && firstDim == null) {
                firstDim = (Exp) child;
            } else if (child instanceof Exp) {
                secondDim = (Exp) child;
            }
        }

        ident = ((Ident) children.get(0)).getIdent().getSrcStr();
        if (firstDim == null) {
            return symbolTableManager.getVal(ident);
        } else if (secondDim == null) {
            try {
                int index = firstDim.getVal();
                return symbolTableManager.getArrayVal(ident, index);
            } catch (NullPointerException e) {
                return null;
            }
        } else {
            try {
                int secondDimLen = symbolTableManager.getSecondDimLen(ident);
                int index = secondDimLen * firstDim.getVal() + secondDim.getVal();
                return symbolTableManager.getArrayVal(ident, index);
            } catch (NullPointerException e) {
                return null;
            }
        }
    }

    public Value toIR() {
        /*
            firstDim和secondDIm的定义有点乱了
            声明: int a[dfd][dsd]
            使用: a[ufd][usd] -> a+ufd*dsd+usd
                 a[ufd] -> a+ufd*dsd
         */
        String ident = children.get(0).getFirstLeafString();
        Value v = symbolTableManager.getIRValue(ident);
        Exp firstDim = null, secondDim = null;
        for (SyntaxTreeNode child : children) {
            if (child instanceof Exp && firstDim == null) {
                firstDim = (Exp) child;
            } else if (child instanceof Exp) {
                secondDim = (Exp) child;
            }
        }

        int lvalDim = symbolTableManager.getDim(ident);
        if (firstDim == null) {
            if (lvalDim != 0) {
                //已经都转为一维数组了，且没有取值操作，不需要区分维数
                return new GetElementPtr(v, new ConstNumber(0));
            } else {
                return v;
            }
        } else if (secondDim == null) {
            if (lvalDim == 2) {
                int secondDimLen = symbolTableManager.getSecondDimLen(ident);
                Value offset = new ALU(new ConstNumber(secondDimLen), "*", firstDim.toIR());
                return new GetElementPtr(v, offset);
            } else {
                return new GetElementPtr(v, firstDim.toIR());
            }
        } else {
            int secondDimLen = symbolTableManager.getSecondDimLen(ident);
            Value offset = new ALU(new ConstNumber(secondDimLen), "*", firstDim.toIR());
            offset = new ALU(offset, "+", secondDim.toIR());
            return new GetElementPtr(v, offset);
        }
    }
}
