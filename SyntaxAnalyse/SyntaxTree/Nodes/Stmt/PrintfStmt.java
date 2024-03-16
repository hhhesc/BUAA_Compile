package SyntaxAnalyse.SyntaxTree.Nodes.Stmt;

import ErrorHandler.ErrorManager;
import IntermediatePresentation.ConstNumber;
import IntermediatePresentation.ConstString;
import IntermediatePresentation.Function.Function;
import IntermediatePresentation.IRManager;
import IntermediatePresentation.Instruction.Call;
import IntermediatePresentation.Instruction.GetElementPtr;
import IntermediatePresentation.Instruction.Instruction;
import IntermediatePresentation.Instruction.Load;
import IntermediatePresentation.Instruction.Putstr;
import IntermediatePresentation.Instruction.ZextTo;
import IntermediatePresentation.Value;
import IntermediatePresentation.ValueType;
import SyntaxAnalyse.SyntaxTree.Nodes.Exp.Exp;
import SyntaxAnalyse.SyntaxTree.Nodes.Utils.FormatString;
import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

import javax.imageio.event.IIOReadProgressListener;
import java.util.ArrayList;

//'printf''('FormatString{','Exp}')'';'
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
        StringBuilder sb = new StringBuilder();

        ArrayList<Value> exps = new ArrayList<>();
        for (SyntaxTreeNode child : children) {
            if (child instanceof Exp) {
                exps.add(child.toIR());
            }
        }

        for (int i = 1; i < chars.length - 1; i++) {
            char c = chars[i];
            if (c == '%') {
                if (!sb.isEmpty()) {
                    ConstString str = new ConstString(sb.toString());
                    int len = sb.length() + 1;
                    new Putstr(str, len);
                    sb = new StringBuilder();
                }
                i++;

                Value exp = exps.get(expIndex);
                if (exp.isPointer()) {
                    exp = new Load(IRManager.getInstance().declareTempVar(), exp);
                }
                if (exp.getType() != ValueType.I32) {
                    exp = new ZextTo(exp, ValueType.I32);
                }
                new Call(Function.putint, exp);
                expIndex++;
            } else if (c == '\\') {
                sb.append("\n");
                i++;
            } else {
                sb.append(c);
            }
        }
        if (!sb.isEmpty()) {
            ConstString str = new ConstString(sb.toString());
            int len = sb.length() + 1;
            new Putstr(str, len);
        }
        return null;
    }

}
