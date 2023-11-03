package ErrorHandler.SymbolTable;

import IntermediatePresentation.Instruction.GlobalDecl;

import java.util.ArrayList;
import java.util.HashMap;

public class SymbolTable {
    HashMap<String, SymbolTableElem> elems = new HashMap<>();

    public boolean hasDeclared(String ident) {
        return elems.containsKey(ident);
    }

    public void varDecl(String ident, boolean isConst, int dim, ArrayList<Integer> lens) {
        elems.put(ident, new VarElem(isConst, dim, lens));
    }

    public void funcDecl(boolean isVoid, String ident, ArrayList<Integer> fParamDims) {
        elems.put(ident, new FuncElem(isVoid, fParamDims));
    }

    /*
        不同作用域下函数可以和变量同名但不会覆盖，例如以下程序不会报错：
        void a(){}
        int main() {
            int a;
            a();
            return 0;
        }
        所以查找同名时不必区分直接在当前表中找，有同名即报错;
        调用时需要区分函数和局部变量，以避免找到了不同层次的另一类型的标识符
     */

    public FuncElem getFunction(String ident) {
        SymbolTableElem elem = elems.getOrDefault(ident, null);
        if (elem == null) {
            return null;
        } else if (!(elem instanceof FuncElem)) {
            return null;
        } else {
            return (FuncElem) elem;
        }
    }

    public VarElem getVar(String ident) {
        SymbolTableElem elem = elems.getOrDefault(ident, null);
        if (elem == null) {
            return null;
        } else if (!(elem instanceof VarElem)) {
            return null;
        } else {
            return (VarElem) elem;
        }
    }

    public void resetValForGlobalVar(){
        for (SymbolTableElem elem:elems.values()){
            if (elem instanceof VarElem v && v.getIrValue() instanceof GlobalDecl){
                v.resetValForGlobalVar();
            }
        }
    }
}
