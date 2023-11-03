package SyntaxAnalyse.SyntaxTree.Nodes.Decl;

import ErrorHandler.ErrorManager;
import IntermediatePresentation.Array.ArrayInitializer;
import IntermediatePresentation.ConstNumber;
import IntermediatePresentation.IRManager;
import IntermediatePresentation.Instruction.GetElementPtr;
import IntermediatePresentation.Instruction.GlobalDecl;
import IntermediatePresentation.Instruction.LocalDecl;
import IntermediatePresentation.Instruction.Store;
import IntermediatePresentation.Value;
import IntermediatePresentation.ValueType;
import LexicalAnalyse.Words.Word;
import SyntaxAnalyse.SyntaxTree.Nodes.Exp.ConstExp;
import SyntaxAnalyse.SyntaxTree.Nodes.Utils.Ident;
import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

import java.util.ArrayList;

public class VarDef extends SyntaxTreeNode {
    private Word ident = null;
    private int dim;

    public VarDef(SyntaxTreeNode parent) {
        super(SyntaxNodeType.VarDef, parent);
    }

    public void checkError() {
        ident = ((Ident) children.get(0)).getIdent();
        ArrayList<Integer> lens = new ArrayList<>();
        int cnt = 0;
        for (SyntaxTreeNode child : children) {
            if (child.isLeaf() && child.getWord().getSrcStr().equals("[")) {
                cnt++;
            } else if (child instanceof ConstExp) {
                lens.add(((ConstExp) child).getVal());
            }
        }
        dim = cnt;

        if (symbolTableManager.notDeclaredInCurLevel(ident.getSrcStr())) {
            symbolTableManager.varDecl(ident.getSrcStr(), false, dim, lens);
        } else {
            ErrorManager.addError('b', ident.getLineNumber());
        }

        super.checkError();
    }

    public Value toIR() {
        ident = ((Ident) children.get(0)).getIdent();
        String ident = null;
        int firstDim = 0;
        int secondDim = 0;
        InitVal initVal = null;

        for (SyntaxTreeNode child : children) {
            if (child instanceof Ident) {
                ident = child.getFirstLeafString();
            } else if (child instanceof ConstExp && firstDim == 0) {
                firstDim = ((ConstExp) child).getVal();
            } else if (child instanceof ConstExp && secondDim == 0) {
                secondDim = ((ConstExp) child).getVal();
            } else if (child instanceof InitVal) {
                initVal = (InitVal) child;
            }
        }

        if (firstDim == 0) {
            if (initVal != null) {
                try {
                    int val = ((InitVal) children.get(2)).getVal();

                    symbolTableManager.varDecl(ident, false,
                            0, new ArrayList<>());
                    symbolTableManager.setVal(ident, val);

                    //可以求出常数形式的初始值
                    if (IRManager.getInstance().inGlobalDecl()) {
                        symbolTableManager.setIRValue(ident,
                                new GlobalDecl(new ConstNumber(val)));
                    } else {
                        LocalDecl localDecl = new LocalDecl();
                        new Store(new ConstNumber(val), localDecl);
                        symbolTableManager.setIRValue(ident, localDecl);
                    }
                } catch (NullPointerException e) {

                    symbolTableManager.varDecl(ident, false,
                            0, new ArrayList<>());
                    if (IRManager.getInstance().inGlobalDecl()) {
                        symbolTableManager.setIRValue(ident,
                                new GlobalDecl(children.get(2).toIR()));
                    } else {
                        LocalDecl localDecl = new LocalDecl();
                        new Store(initVal.toIR(), localDecl);
                        symbolTableManager.setIRValue(ident, localDecl);
                    }
                }
            } else {
                symbolTableManager.varDecl(ident, false, 0, new ArrayList<>());
                if (IRManager.getInstance().inGlobalDecl()) {
                    symbolTableManager.setIRValue(ident, new GlobalDecl(new ConstNumber(0)));
                } else {
                    symbolTableManager.setIRValue(ident, new LocalDecl());
                }
            }
        } else if (secondDim == 0) {
            ArrayList<Integer> lens = new ArrayList<>();
            lens.add(firstDim);
            symbolTableManager.varDecl(ident, false, 1, lens);
            if (IRManager.getInstance().inGlobalDecl()) {
                //只有全局声明的数组需要在符号表中存储其值
                GlobalDecl globalDecl;
                if (initVal == null) {
                    globalDecl = new GlobalDecl(new ArrayInitializer(firstDim));
                } else {
                    ArrayInitializer arrayInitializer = (ArrayInitializer) initVal.toIR();
                    globalDecl = new GlobalDecl(arrayInitializer);
                    symbolTableManager.arrayInit(ident,arrayInitializer);
                }
                symbolTableManager.setIRValue(ident, globalDecl);
            } else {
                LocalDecl localDecl = new LocalDecl(firstDim);
                if (initVal != null) {
                    ArrayList<Value> initVals = ((ArrayInitializer) (initVal.toIR())).getVals();
                    for (int i = 0; i < initVals.size(); i++) {
                        Value addr = new GetElementPtr(localDecl, i);
                        new Store(initVals.get(i), addr);
                    }
                }
                symbolTableManager.setIRValue(ident, localDecl);
            }
        } else {
            ArrayList<Integer> lens = new ArrayList<>();
            lens.add(firstDim);
            lens.add(secondDim);
            symbolTableManager.varDecl(ident, false, 2, lens);
            if (IRManager.getInstance().inGlobalDecl()) {
                GlobalDecl globalDecl;
                if (initVal == null) {
                    globalDecl = new GlobalDecl(new ArrayInitializer(firstDim * secondDim));
                } else {
                    ArrayInitializer arrayInitializer = (ArrayInitializer) initVal.toIR();
                    globalDecl = new GlobalDecl(arrayInitializer);
                    symbolTableManager.arrayInit(ident,arrayInitializer);
                }
                symbolTableManager.setIRValue(ident, globalDecl);
            } else {
                LocalDecl localDecl = new LocalDecl(firstDim * secondDim);
                if (initVal != null) {
                    ArrayList<Value> initVals = ((ArrayInitializer) (initVal.toIR())).getVals();
                    for (int i = 0; i < initVals.size(); i++) {
                        Value addr = new GetElementPtr(localDecl, i);
                        new Store(initVals.get(i), addr);
                    }
                }
                symbolTableManager.setIRValue(ident, localDecl);
            }
        }
        return null;
    }
}
