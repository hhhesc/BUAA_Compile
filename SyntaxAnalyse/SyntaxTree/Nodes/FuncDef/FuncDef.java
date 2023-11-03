package SyntaxAnalyse.SyntaxTree.Nodes.FuncDef;

import ErrorHandler.ErrorManager;
import IntermediatePresentation.Function.Function;
import IntermediatePresentation.Function.Param;
import IntermediatePresentation.Instruction.Ret;
import IntermediatePresentation.Value;
import IntermediatePresentation.ValueType;
import SyntaxAnalyse.SyntaxTree.Nodes.Utils.Ident;
import LexicalAnalyse.Words.Word;
import SyntaxAnalyse.SyntaxTree.Nodes.Utils.Block;
import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

import java.util.ArrayList;
import java.util.Objects;

public class FuncDef extends SyntaxTreeNode {
    private boolean isVoid;
    private Word ident;

    public FuncDef(SyntaxTreeNode parent) {
        super(SyntaxNodeType.FuncDef, parent);
    }

    public void checkError() {
        isVoid = ((FuncType) children.get(0)).isVoid();
        ident = ((Ident) children.get(1)).getIdent();
        ArrayList<Integer> fParamDims = new ArrayList<>();
        for (SyntaxTreeNode child : children) {
            if (child instanceof FuncFParams fParamsNode) {
                fParamDims = fParamsNode.getParamDims();
                break;
            }
        }


        if (symbolTableManager.notDeclaredInCurLevel(ident.getSrcStr())) {
            symbolTableManager.funcDecl(isVoid, ident.getSrcStr(), fParamDims);
        } else {
            symbolTableManager.setReturnCheckWhenRedcel(isVoid);
            ErrorManager.addError('b', ident.getLineNumber());
        }

        symbolTableManager.enterBlock(); //函数声明在上一级，函数参数作为局部变量在下一级
        super.checkError();
        symbolTableManager.funcDeclEnd();

        Block block = (Block) children.get(children.size() - 1);
        if (getFirstLeafString().equals("int") && block.notExistReturnStmt()) {
            ErrorManager.addError('g',
                    block.getChildren().get(block.getChildren().size() - 1).getWord().getLineNumber());
        }
    }

    public Value toIR() {
        ValueType type = (getFirstLeafString().equals("int")) ? ValueType.I32 : ValueType.NULL;
        String name = children.get(1).getFirstLeafString();
        ArrayList<Integer> fParamDims = new ArrayList<>();
        for (SyntaxTreeNode child : children) {
            if (child instanceof FuncFParams fParamsNode) {
                fParamDims = fParamsNode.getParamDims();
                break;
            }
        }

        symbolTableManager.funcDecl(type == ValueType.NULL, name, fParamDims);
        symbolTableManager.enterBlock();
        Function f = null;
        for (SyntaxTreeNode child : children) {
            if (child instanceof FuncFParams) {
                f = new Function("@" + name, type);
                f.setParam((Param) child.toIR());
            }
        }

        if (f == null) {
            f = new Function("@" + name, new Param(), type);
        }
        symbolTableManager.setIRValue(name, f);
        children.get(children.size() - 1).toIR();
        //函数体(block)

        if (f.getType()==ValueType.NULL){
            new Ret();
        }
        symbolTableManager.funcDeclEnd();
        return f;
    }
}
