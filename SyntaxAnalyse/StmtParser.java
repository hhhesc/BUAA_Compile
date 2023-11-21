package SyntaxAnalyse;

import ErrorHandler.CompileError.CompileException;
import ErrorHandler.ErrorManager;
import LexicalAnalyse.Lexer;
import LexicalAnalyse.Words.FormatString;
import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;

public class StmtParser extends Parser {
    public StmtParser(Lexer lexer) {
        super(lexer);
    }

    public void parse() throws CompileException {
        stmt();
    }

    protected void stmt() throws CompileException {
        try {
            record();
            lValAssignStmt();
            release();
        } catch (CompileException ignored) {
            back();
            try {
                record();
                buildIntermediateNode(SyntaxNodeType.Stmt);
                exp();
                buildDone();
                try {
                    record();
                    buildLeaf(";");
                    release();
                } catch (CompileException e) {
                    back();
                    ErrorManager.addError('i', lexer.getLastLineNumber());
                }
                release();
            } catch (CompileException e) {
                back();
                String next = lexer.getSrc();
                switch (next) {
                    case ";" -> {
                        buildIntermediateNode(SyntaxNodeType.Stmt);
                        buildLeaf(";");
                        buildDone();
                    }
                    case "{" -> blockStmt();
                    case "if" -> ifStmt();
                    case "for" -> theForStmt();
                    case "break" -> breakStmt();
                    case "continue" -> continueStmt();
                    case "return" -> returnStmt();
                    case "printf" -> printfStmt();
                }
            }
        }
    }

    public void blockStmt() throws CompileException {
        buildIntermediateNode(SyntaxNodeType.BlockStmt);
        block();
        buildDone();
    }

    private void lValAssignStmt() throws CompileException {
        try {
            record();
            lValAssignExpStmt();
            release();
        } catch (CompileException e) {
            back();
            lValAssignGetIntStmt();
        }
    }

    private void lValAssignExpStmt() throws CompileException {
        buildIntermediateNode(SyntaxNodeType.LValAssignExpStmt);
        lVal();
        buildLeaf("=");
        exp();
        try {
            record();
            buildLeaf(";");
            release();
        } catch (CompileException e) {
            back();
            ErrorManager.addError('i', lexer.getLastLineNumber());
        }
        buildDone();
    }

    private void lValAssignGetIntStmt() throws CompileException {
        buildIntermediateNode(SyntaxNodeType.LValAssignGetIntStmt);
        lVal();
        buildLeaf("=");
        buildLeaf("getint");
        buildLeaf("(");
        try {
            record();
            buildLeaf(")");
            release();
        } catch (CompileException e) {
            back();
            ErrorManager.addError('j', lexer.getLastLineNumber());
        }
        try {
            record();
            buildLeaf(";");
            release();
        } catch (CompileException e) {
            back();
            ErrorManager.addError('i', lexer.getLastLineNumber());
        }
        buildDone();
    }


    private void ifStmt() throws CompileException {
        buildIntermediateNode(SyntaxNodeType.IfStmt);
        buildLeaf("if");
        buildLeaf("(");
        cond();
        try {
            record();
            buildLeaf(")");
            release();
        } catch (CompileException e) {
            back();
            ErrorManager.addError('j', lexer.getLastLineNumber());
        }
        stmt();
        if (lexer.getSrc().equals("else")) {
            buildLeaf("else");
            stmt();
        }
        buildDone();
    }

    private void theForStmt() throws CompileException {
        buildIntermediateNode(SyntaxNodeType.TheForStmt);
        buildLeaf("for");
        buildLeaf("(");
        if (!lexer.getSrc().equals(";")) {
            forStmt();
        }
        buildLeaf(";");
        if (!lexer.getSrc().equals(";")) {
            cond();
        }
        buildLeaf(";");
        if (!(lexer.getSrc().equals(")") || lexer.getSrc().equals("{"))) {
            forStmt();
        }
        try {
            record();
            buildLeaf(")");
            release();
        } catch (CompileException e) {
            back();
            ErrorManager.addError('j', lexer.getLastLineNumber());
        }
        stmt();
        buildDone();
    }

    private void forStmt() throws CompileException {
        buildIntermediateNode(SyntaxNodeType.ForStmt);
        lVal();
        buildLeaf("=");
        exp();
        buildDone();
    }

    private void breakStmt() throws CompileException {
        buildIntermediateNode(SyntaxNodeType.BreakStmt);
        buildLeaf("break");
        try {
            record();
            buildLeaf(";");
            release();
        } catch (CompileException e) {
            back();
            ErrorManager.addError('i', lexer.getLastLineNumber());
        }
        buildDone();
    }

    private void continueStmt() throws CompileException {
        buildIntermediateNode(SyntaxNodeType.ContinueStmt);
        buildLeaf("continue");
        try {
            record();
            buildLeaf(";");
            release();
        } catch (CompileException e) {
            back();
            ErrorManager.addError('i', lexer.getLastLineNumber());
        }
        buildDone();
    }

    private void returnStmt() throws CompileException {
        buildIntermediateNode(SyntaxNodeType.ReturnStmt);
        buildLeaf("return");
        try {
            record();
            exp();
            release();
        } catch (CompileException e) {
            back();
        }
//        exp();
        try {
            record();
            buildLeaf(";");
            release();
        } catch (CompileException e) {
            back();
            ErrorManager.addError('i', lexer.getLastLineNumber());
        }
        buildDone();
    }

    private void printfStmt() throws CompileException {
        buildIntermediateNode(SyntaxNodeType.PrintfStmt);
        buildLeaf("printf");
        buildLeaf("(");
        formatString();
        while (lexer.getSrc().equals(",")) {
            buildLeaf(",");
            exp();
        }
        try {
            record();
            buildLeaf(")");
            release();
        } catch (CompileException e) {
            back();
            ErrorManager.addError('j', lexer.getLastLineNumber());
        }
        try {
            record();
            buildLeaf(";");
            release();
        } catch (CompileException e) {
            back();
            ErrorManager.addError('i', lexer.getLastLineNumber());
        }
        buildDone();
    }

    private void formatString() throws CompileException {
        buildIntermediateNode(SyntaxNodeType.FormatString);
        buildLeaf(FormatString.pattern);
        buildDone();
    }
}
