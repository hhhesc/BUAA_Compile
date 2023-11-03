package SyntaxAnalyse.SyntaxTree.Nodes.Utils;

import ErrorHandler.CompileError.CompileException;
import ErrorHandler.ErrorManager;
import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTree;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class FormatString extends SyntaxTreeNode {
    public FormatString(SyntaxTreeNode parent) {
        super(SyntaxNodeType.FormatString, parent);
    }

    public void checkError() {
        if (!isLegalFormatStr(children.get(0).getWord().getSrcStr())) {
            ErrorManager.addError('a', children.get(0).getWord().getLineNumber());
        }
    }

    public int getIntNumber() {
        String str = children.get(0).getWord().getSrcStr();
        char[] chars = str.toCharArray();
        int cnt = 0;
        for (int i = 0; i < chars.length - 1; i++) {
            if (chars[i] == '%' && chars[i + 1] == 'd') {
                cnt++;
            }
        }
        return cnt;
    }

    private boolean isLegalFormatStr(String formatstr) {
        if (!formatstr.startsWith("\"") || !formatstr.endsWith("\"")) {
            return false;
        }
        char[] charArray = formatstr.toCharArray();
        for (int i = 1; i < charArray.length - 1; i++) {
            int cVal = charArray[i];
            if (cVal == 37) {
                if (charArray[i + 1] != 'd') {
                    return false;
                } else {
                    i++;
                    continue;
                }
            }
            if (cVal <= 31 || cVal >= 34 && cVal <= 39 || cVal >= 127) {
                return false;
            } else if (cVal == 92 && charArray[i + 1] != 'n') {
                return false;
            }
        }
        return true;
    }
}
