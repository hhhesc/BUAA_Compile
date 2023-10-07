package SyntaxAnalyse;

import CompileError.CompileException;
import LexicalAnalyse.Lexer;
import LexicalAnalyse.Words.IntConst;
import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;

public class ExpParser extends Parser {
    public ExpParser(Lexer lexer) {
        super(lexer);
    }

    public void parse() throws CompileException {
        exp();
    }

    public void parseConst() throws CompileException {
        constExp();
    }

    public void cond() throws CompileException {
        buildIntermediateNode(SyntaxNodeType.Cond);
        lOrExp();
        buildDone();
    }

    protected void exp() throws CompileException {
        buildIntermediateNode(SyntaxNodeType.Exp);
        addExp();
        buildDone();
    }

    protected void constExp() throws CompileException {
        buildIntermediateNode(SyntaxNodeType.ConstExp);
        addExp();
        buildDone();
    }

    private void primaryExp() throws CompileException {
        buildIntermediateNode(SyntaxNodeType.PrimaryExp);
        if (lexer.getSrc().equals("(")) {
            buildLeaf("(");
            exp();
            buildLeaf(")");
        } else if (lexer.getToken().equals("INTCON")) {
            number();
        } else {
            lVal();
        }
        buildDone();
    }

    private void unaryExp() throws CompileException {
        buildIntermediateNode(SyntaxNodeType.UnaryExp);
        try {
            record();
            if (lexer.getSrc().equals("+") || lexer.getSrc().equals("-") || lexer.getSrc().equals("!")) {
                unaryOp();
                unaryExp();
            } else {
                ident();
                buildLeaf("(");
                if (!lexer.getSrc().equals(")")) {
                    funcRParams();
                }
                buildLeaf(")");
            }
            release();
        } catch (CompileException e) {
            back();
            primaryExp();
        }
        buildDone();
    }

    private void mulExp() throws CompileException {
        buildIntermediateNode(SyntaxNodeType.MulExp);
        unaryExp();
        while (lexer.getSrc().matches("[\\*/%]")) {
            switch (lexer.getSrc()) {
                case "*" -> buildLeaf("*");
                case "/" -> buildLeaf("/");
                case "%" -> buildLeaf("%");
            }
            unaryExp();
        }
        buildDone();
    }

    private void addExp() throws CompileException {
        buildIntermediateNode(SyntaxNodeType.AddExp);
        mulExp();
        while (lexer.getSrc().equals("+") || lexer.getSrc().equals("-")) {
            if (lexer.getSrc().equals("+")) {
                buildLeaf("+");
            } else {
                buildLeaf("-");
            }
            mulExp();
        }
        buildDone();
    }

    private void relExp() throws CompileException {
        buildIntermediateNode(SyntaxNodeType.RelExp);
        addExp();
        while (lexer.getSrc().matches("<|>|<=|>=")) {
            switch (lexer.getSrc()) {
                case "<" -> buildLeaf("<");
                case ">" -> buildLeaf(">");
                case "<=" -> buildLeaf("<=");
                case ">=" -> buildLeaf(">=");
            }
            addExp();
        }
        buildDone();
    }

    private void eqExp() throws CompileException {
        buildIntermediateNode(SyntaxNodeType.EqExp);
        relExp();
        while (lexer.getSrc().equals("==") || lexer.getSrc().equals("!=")) {
            if (lexer.getSrc().equals("==")) {
                buildLeaf("==");
            } else {
                buildLeaf("!=");
            }
            relExp();
        }
        buildDone();
    }

    private void lAndExp() throws CompileException {
        buildIntermediateNode(SyntaxNodeType.LAndExp);
        eqExp();
        while (lexer.getSrc().equals("&&")) {
            buildLeaf("&&");
            eqExp();
        }
        buildDone();
    }

    private void unaryOp() throws CompileException {
        buildIntermediateNode(SyntaxNodeType.UnaryOp);
        switch (lexer.getSrc()) {
            case "+" -> buildLeaf("+");
            case "-" -> buildLeaf("-");
            case "!" -> buildLeaf("!");
        }
        buildDone();
    }

    private void funcRParams() throws CompileException {
        buildIntermediateNode(SyntaxNodeType.FuncRParams);
        exp();
        while (lexer.getSrc().equals(",")) {
            buildLeaf(",");
            exp();
        }
        buildDone();
    }

    private void number() throws CompileException {
        buildIntermediateNode(SyntaxNodeType.Number);
        intConst();
        buildDone();
    }

    private void intConst() throws CompileException {
        buildIntermediateNode(SyntaxNodeType.IntConst);
        buildLeaf(IntConst.pattern);
        buildDone();
    }

    protected void lOrExp() throws CompileException {
        buildIntermediateNode(SyntaxNodeType.LOrExp);
        lAndExp();
        while (lexer.getSrc().equals("||")) {
            buildLeaf("||");
            lAndExp();
        }
        buildDone();
    }
}
