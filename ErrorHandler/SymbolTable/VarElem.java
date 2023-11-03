package ErrorHandler.SymbolTable;

import IntermediatePresentation.Instruction.GlobalDecl;
import IntermediatePresentation.Value;

import java.util.ArrayList;

public class VarElem extends SymbolTableElem {
    private final boolean isConst;
    private final int dim;
    private final ArrayList<Integer> lens;

    private Integer val = null;

    private Value irValue = null;

    private Integer globalVal = null;

    private ArrayList<Integer> elemList = new ArrayList<>();

    public VarElem(boolean isConst, int dim, ArrayList<Integer> lens) {
        super();
        this.isConst = isConst;
        this.dim = dim;
        this.lens = lens;
        if (dim == 1 && lens.size() == 1) {
            for (int i = 0; i < lens.get(0); i++) {
                elemList.add(0);
            }
        } else if (dim == 2 && lens.size() == 2) {
            for (int i = 0; i < lens.get(0) * lens.get(1); i++) {
                elemList.add(0);
            }
        }
    }

    public void arrayInit(ArrayList<Integer> elemList) {
        this.elemList = elemList;
    }

    public int getDim() {
        return dim;
    }

    public ArrayList<Integer> getLens() {
        return lens;
    }

    public boolean isConst() {
        return isConst;
    }

    public void setVal(Integer val) {
        this.val = val;
        this.globalVal = val;
    }

    public Integer getVal() {
        return val;
    }

    public void setArrayVal(Integer val, int index) {
        elemList.set(index, val);
    }

    public Integer getArrayVal(int index) {
        return elemList.get(index);
    }

    public void setIrValue(Value irValue) {
        this.irValue = irValue;
    }

    public Value getIrValue() {
        return irValue;
    }

    public void setTempVal(Integer val) {
        if (irValue instanceof GlobalDecl) {
            globalVal = this.val;
        }
        this.val = val;
    }

    public void resetValForGlobalVar() {
        if (irValue instanceof GlobalDecl) {
            this.val = globalVal;
        }
    }
}
