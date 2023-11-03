package SyntaxAnalyse.SyntaxTree.Nodes.Exp;

import ErrorHandler.ErrorManager;
import IntermediatePresentation.ConstNumber;
import IntermediatePresentation.Function.Function;
import IntermediatePresentation.IRManager;
import IntermediatePresentation.Instruction.ALU;
import IntermediatePresentation.Instruction.Call;
import IntermediatePresentation.Instruction.Icmp;
import IntermediatePresentation.Instruction.Load;
import IntermediatePresentation.Instruction.ZextTo;
import IntermediatePresentation.Value;
import IntermediatePresentation.ValueType;
import SyntaxAnalyse.SyntaxTree.Nodes.FuncDef.FuncRParams;
import SyntaxAnalyse.SyntaxTree.Nodes.Utils.Ident;
import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

import java.util.ArrayList;

public class UnaryExp extends SyntaxTreeNode {
    public UnaryExp(SyntaxTreeNode parent) {
        super(SyntaxNodeType.UnaryExp, parent);
    }

    public void checkError() {
        for (SyntaxTreeNode child : children) {
            child.checkError();
        }

        if (children.get(0) instanceof Ident ident) {
            ArrayList<Integer> rParamDims = new ArrayList<>();
            for (SyntaxTreeNode child : children) {
                if (child instanceof FuncRParams) {
                    rParamDims = ((FuncRParams) child).getParamDims();
                    break;
                }
            }
            if (symbolTableManager.notDeclared(ident.getIdent().getSrcStr())) {
                return;
            }
            ArrayList<Integer> fParamDims = symbolTableManager.getFuncParamDims(ident.getIdent().getSrcStr());

            int paramSize = rParamDims.size();
            if (paramSize != fParamDims.size()) {
                ErrorManager.addError('d', ident.getIdent().getLineNumber());
                return;
            }

            if (!rParamDims.equals(fParamDims)) {
                ErrorManager.addError('e', ident.getIdent().getLineNumber());
            }
        }
    }

    public Integer getDim() {
        if (children.get(0) instanceof Ident) {
            children.get(0).checkError();
            String funcName = ((Ident) children.get(0)).getIdent().getSrcStr();
            if (symbolTableManager.notDeclared(funcName)) {
                return -1;
            }
            boolean isVoid = symbolTableManager.funcIsVoid(funcName);
            if (!isVoid) return 0;
            else return -1;
        }

        for (SyntaxTreeNode child : children) {
            if (child.getDim() != null) return child.getDim();
        }
        return null;
    }

    public Integer getVal() {
        if (children.get(0) instanceof UnaryOp op) {
            if (op.getFirstLeafString().equals("-")) {
                return -((UnaryExp) children.get(1)).getVal();
            } else if (op.getFirstLeafString().equals("+")) {
                return ((UnaryExp) children.get(1)).getVal();
            } else {
                return null;
            }
        } else if (children.get(0) instanceof PrimaryExp) {
            return ((PrimaryExp) children.get(0)).getVal();
        } else {
            //函数调用
            return null;
        }
    }

    public Value toIR() {
        String str = getFirstLeafString();
        if (children.size() == 1) {
            return children.get(0).toIR();
        } else if (str.equals("+")) {
            return children.get(1).toIR();
        } else if (str.equals("-")) {
            return new ALU(new ConstNumber("0"), "-", children.get(1).toIR());
        } else if (str.equals("!")) {
            return new Icmp("==", new ConstNumber("0"), children.get(1).toIR());
        } else {
            //函数调用
            ArrayList<Value> params = new ArrayList<>();
            for (SyntaxTreeNode child : children) {
                if (child instanceof FuncRParams) {
                    for (SyntaxTreeNode param : child.getChildren()) {
                        if (param instanceof Exp) {
                            params.add(param.toIR());
                        }
                    }
                }
            }

            Function function = (Function) symbolTableManager.getIRValue(str);
            ArrayList<Value> fParams = function.getParam().getParams();
            ArrayList<Value> fixedParams = new ArrayList<>();
            for (int i = 0; i < params.size(); i++) {
                Value v = params.get(i);
                ValueType paramType = fParams.get(i).getType();
                if (v.getType() != paramType) {
                    /*
                        如果参数需要指针，则传入的是数组指针；否则传入的是值
                        传值需要从内存中取值，而且可能需要类型转换
                        传数组，已经在子节点中处理过了，全部改为了指针
                     */
                    if (!paramType.isPointer()) {
                        if (v.isPointer()) {
                            v = new Load(IRManager.getInstance().declareTempVar(), v);
                        }
                        if (v.getType() != paramType) {
                            v = new ZextTo(v, paramType);
                        }
                    }
                }
                fixedParams.add(v);
            }


            return new Call(function, fixedParams);
        }
    }
}
