package SyntaxAnalyse.SyntaxTree.Nodes.Stmt;

import ErrorHandler.ErrorManager;
import IntermediatePresentation.ConstNumber;
import IntermediatePresentation.Function.Function;
import IntermediatePresentation.IRManager;
import IntermediatePresentation.Instruction.Call;
import IntermediatePresentation.Instruction.Load;
import IntermediatePresentation.Instruction.ZextTo;
import IntermediatePresentation.Value;
import IntermediatePresentation.ValueType;
import SyntaxAnalyse.SyntaxTree.Nodes.Exp.Exp;
import SyntaxAnalyse.SyntaxTree.Nodes.Utils.FormatString;
import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

import javax.imageio.event.IIOReadProgressListener;
import java.util.ArrayList;

public class PrintfStmt extends Stmt {
    public PrintfStmt(SyntaxTreeNode parent) {
        super(SyntaxNodeType.PrintfStmt, parent);
    }

    public void checkError() {
        super.checkError();

        int cnt = ((FormatString) children.get(2)).getIntNumber();
        for (SyntaxTreeNode child : children) {
            if (child instanceof Exp) {
                cnt--;
            }
        }
        if (cnt != 0) {
            ErrorManager.addError('l', getFirstLeafLineNumber());
        }
    }

    public Value toIR() {
        String formatString = children.get(2).getFirstLeafString();
        char[] chars = formatString.toCharArray();
        int expIndex = 0;
        for (int i = 1; i < chars.length - 1; i++) {
            char c = chars[i];
            if (c == '%') {
                i++;
                while (expIndex < children.size() && !(children.get(expIndex) instanceof Exp)) {
                    expIndex++;
                }
                Value exp = children.get(expIndex).toIR();
                if (exp.isPointer()) {
                    exp = new Load(IRManager.getInstance().declareTempVar(), exp);
                }
                if (exp.getType() != ValueType.I32) {
                    exp = new ZextTo(exp, ValueType.I32);
                }
                new Call(Function.putint, exp);
                expIndex++;
            } else if (c == '\\') {
                i++;
                new Call(Function.putch, new ConstNumber('\n'));
            } else {
                ConstNumber n = new ConstNumber(c);
                new Call(Function.putch, n);

            }
        }
        return null;
    }

}
