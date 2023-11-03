package ErrorHandler.SymbolTable;

import IntermediatePresentation.Array.ArrayInitializer;
import IntermediatePresentation.ConstNumber;
import IntermediatePresentation.Function.Function;
import IntermediatePresentation.IRManager;
import IntermediatePresentation.Instruction.GlobalDecl;
import IntermediatePresentation.Value;

import java.util.ArrayList;
import java.util.LinkedList;

public class SymbolTableManager {
    private static final SymbolTableManager INSTANCE = new SymbolTableManager();
    private final LinkedList<SymbolTable> symbolTableStack = new LinkedList<>();
    private SymbolTable curTable;
    private int cycleLevel = 0;

    private FuncElem presFunc = null;
    private boolean returnTypeWhenCheck = false;


    public static SymbolTableManager getInstance() {
        return INSTANCE;
    }

    private SymbolTableManager() {
        curTable = new SymbolTable();
        symbolTableStack.push(curTable);
    }

    public void enterBlock() {
        curTable = new SymbolTable();
        symbolTableStack.push(curTable);
    }

    public void exitBlock() {
        symbolTableStack.pop();
        curTable = symbolTableStack.get(0);
    }

    public void enterCycle() {
        cycleLevel++;
    }

    public void exitCycle() {
        cycleLevel--;
        IRManager.getInstance().exitCycle();
    }

    public void funcDeclEnd() {
        presFunc = null;
        exitBlock();
        IRManager.getInstance().resetBlockCount();
    }

    public boolean notInCycle() {
        return cycleLevel == 0;
    }

    public boolean notDeclaredInCurLevel(String ident) {
        return !curTable.hasDeclared(ident);
    }

    public boolean notDeclared(String ident) {
        for (SymbolTable table : symbolTableStack) {
            if (table.hasDeclared(ident)) {
                return false;
            }
        }
        return true;
    }

    public void varDecl(String ident, boolean isConst, int dim, ArrayList<Integer> lens) {
        curTable.varDecl(ident, isConst, dim, lens);
    }

    public void funcDecl(boolean isVoid, String ident, ArrayList<Integer> fParamDims) {
        curTable.funcDecl(isVoid, ident, fParamDims);
        presFunc = getFunction(ident);
        symbolTableStack.get(symbolTableStack.size() - 1).resetValForGlobalVar();
    }

    private FuncElem getFunction(String ident) {
        for (SymbolTable symbolTable : symbolTableStack) {
            if (symbolTable.getFunction(ident) != null) {
                return symbolTable.getFunction(ident);
            }
        }
        return null;
    }

    private VarElem getVar(String ident) {
        for (SymbolTable symbolTable : symbolTableStack) {
            if (symbolTable.getVar(ident) != null) {
                return symbolTable.getVar(ident);
            }
        }
        return null;
    }

    public void setVal(String ident, Integer val) {
        VarElem varElem = getVar(ident);
        if (varElem != null) {
            if (varElem.getIrValue() instanceof GlobalDecl && IRManager.getInstance().inGlobalDecl()
                    && varElem.getVal() != null) {
                varElem.setTempVal(val);
            } else {
                varElem.setVal(val);
            }
        }
    }

    public Integer getVal(String ident) {
        VarElem varElem = getVar(ident);
        if (varElem != null && (varElem.isConst() ||
                varElem.getIrValue() instanceof GlobalDecl && IRManager.getInstance().inGlobalDecl())) {
            return varElem.getVal();
        } else {
            return null;
        }
    }

    public void setArrayVal(String ident, Integer val, int index) {
        VarElem varElem = getVar(ident);
        if (varElem != null) {
            varElem.setArrayVal(val, index);
        }
    }

    public Integer getArrayVal(String ident, int index) {
        VarElem varElem = getVar(ident);
        if (varElem != null && varElem.getIrValue() instanceof GlobalDecl
                && IRManager.getInstance().inGlobalDecl()) {
            return varElem.getArrayVal(index);
        } else {
            return null;
        }
    }

    public void arrayInit(String ident, ArrayInitializer arrayInitializer) {
        ArrayList<Integer> elems = new ArrayList<>();
        for (Value v : arrayInitializer.getVals()) {
            elems.add(((ConstNumber) v).getVal());
        }
        VarElem varElem = getVar(ident);
        if (varElem != null) {
            varElem.arrayInit(elems);
        }
    }

    public Value getIRValue(String ident) {
        VarElem varElem = getVar(ident);
        if (varElem != null) {
            return varElem.getIrValue();
        } else {
            FuncElem funcElem = getFunction(ident);
            if (funcElem != null) {
                return funcElem.getFunctionIR();
            } else {
                return null;
            }
        }
    }

    public void setIRValue(String ident, Value reg) {
        VarElem varElem = getVar(ident);
        if (varElem != null) {
            varElem.setIrValue(reg);
        } else {
            FuncElem funcElem = getFunction(ident);
            if (funcElem != null) {
                funcElem.setFunctionIR((Function) reg);
            }
        }
    }

    public boolean funcIsVoid(String ident) {
        return getFunction(ident).isVoid();
    }

    public ArrayList<Integer> getFuncParamDims(String ident) {
        return getFunction(ident).getFParamDims();
    }

    public int getDim(String ident) {
        VarElem varElem = getVar(ident);
        return (varElem == null) ? 0 : varElem.getDim();
    }

    public boolean lastFuncIsVoid() {
        if (presFunc != null) {
            return presFunc.isVoid();
        } else {
            return returnTypeWhenCheck;
        }
    }

    public boolean varIsConst(String ident) {
        VarElem varElem = getVar(ident);
        if (varElem == null) {
            return false;
        } else {
            return varElem.isConst();
        }
    }


    public void setReturnCheckWhenRedcel(boolean isVoid) {
        returnTypeWhenCheck = isVoid;
    }

    public int getSecondDimLen(String ident) {
        VarElem varElem = getVar(ident);
        try {
            return varElem.getLens().get(1);
        } catch (NullPointerException e) {
            return 0;
        }
    }

}
