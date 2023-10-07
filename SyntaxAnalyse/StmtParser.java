package SyntaxAnalyse;

import CompileError.CompileException;
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
        buildIntermediateNode(SyntaxNodeType.Stmt);
        try {
            record();
            lValAssignStmt();
            release();
        } catch (CompileException ignored) {
            back();
            try {
                record();
                exp();
                buildLeaf(";");
                release();
            } catch (CompileException ignoredTwice) {
                back();
                String next = lexer.getSrc();
                switch (next) {
                    case ";" -> buildLeaf(";");
                    case "{" -> block();
                    case "if" -> ifStmt();
                    case "for" -> theForStmt();
                    case "break" -> breakStmt();
                    case "continue" -> continueStmt();
                    case "return" -> returnStmt();
                    case "printf" -> printfStmt();
                }
            }
        }
        buildDone();
    }

    private void lValAssignStmt() throws CompileException {
        buildIntermediateNode(SyntaxNodeType.LValAssignExpStmt);
        lVal();
        buildLeaf("=");
        if (lexer.getSrc().equals("getint")) {
            buildLeaf("getint");
            buildLeaf("(");
            buildLeaf(")");
        } else {
            exp();
        }
        buildLeaf(";");
        buildDone();
    }


    private void ifStmt() throws CompileException {
        buildIntermediateNode(SyntaxNodeType.IfStmt);
        buildLeaf("if");
        buildLeaf("(");
        cond();
        buildLeaf(")");
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
            forStmtStmt();
        }
        buildLeaf(";");
        if (!lexer.getSrc().equals(";")) {
            cond();
        }
        buildLeaf(";");
        if (!lexer.getSrc().equals(")")) {
            forStmtStmt();
        }
        buildLeaf(")");
        stmt();
        buildDone();
    }

    private void forStmtStmt() throws CompileException {
        buildIntermediateNode(SyntaxNodeType.ForStmt);
        lVal();
        buildLeaf("=");
        exp();
        buildDone();
    }

    private void breakStmt() throws CompileException {
        buildIntermediateNode(SyntaxNodeType.BreakStmt);
        buildLeaf("break");
        buildLeaf(";");
        buildDone();
    }

    private void continueStmt() throws CompileException {
        buildIntermediateNode(SyntaxNodeType.ContinueStmt);
        buildLeaf("continue");
        buildLeaf(";");
        buildDone();
    }

    private void returnStmt() throws CompileException {
        buildIntermediateNode(SyntaxNodeType.ReturnStmt);
        buildLeaf("return");
        if (!lexer.getSrc().equals(";")) {
            exp();
        }
        buildLeaf(";");
        buildDone();
    }

    private void printfStmt() throws CompileException {
        buildIntermediateNode(SyntaxNodeType.PrintfStmt);
        buildLeaf("printf");
        buildLeaf("(");
        formatString();
        while (!lexer.getSrc().equals(")")) {
            buildLeaf(",");
            exp();
        }
        buildLeaf(")");
        buildLeaf(";");
        buildDone();
    }

    private void formatString() throws CompileException {
        buildIntermediateNode(SyntaxNodeType.FormatString);
        buildLeaf(FormatString.pattern);
        buildDone();
    }
}
