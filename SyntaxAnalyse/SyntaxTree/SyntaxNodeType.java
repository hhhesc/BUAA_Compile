package SyntaxAnalyse.SyntaxTree;

public enum SyntaxNodeType {
    CompUnit, Decl, FuncDef, MainFuncDef, ConstDecl, VarDecl, BType, ConstDef, Ident, ConstExp, ConstInitVal,
    VarDef, InitVal, Exp, FuncType, FuncFParams, Block, Leaf, FuncFParam, BlockItem, Stmt, LVal, Cond,
    IfStmt, TheForStmt, ForStmt, BreakStmt, ContinueStmt, FormatString, GetIntStmt, PrintfStmt, AddExp,
    LValAssignExpStmt, LValAssignGetIntStmt, ReturnStmt,
    LOrExp, PrimaryExp, Number, IntConst, UnaryExp, FuncRParams, UnaryOp, MulExp,RelExp,EqExp,LAndExp,
}
