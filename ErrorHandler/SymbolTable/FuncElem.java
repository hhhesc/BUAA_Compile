package ErrorHandler.SymbolTable;

import IntermediatePresentation.Function.Function;

import java.util.ArrayList;

public class FuncElem extends SymbolTableElem {
    private final boolean isVoid;
    private final ArrayList<Integer> fParamDims;

    private Function functionIR;


    public FuncElem(boolean isVoid, ArrayList<Integer> fParamDims) {
        super();
        this.isVoid = isVoid;
        this.fParamDims = new ArrayList<>(fParamDims);
    }

    public boolean isVoid() {
        return isVoid;
    }

    public ArrayList<Integer> getFParamDims() {
        return fParamDims;
    }

    public void setFunctionIR(Function function) {
        this.functionIR = function;
    }

    public Function getFunctionIR() {
        return functionIR;
    }


}
