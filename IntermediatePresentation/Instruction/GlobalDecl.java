package IntermediatePresentation.Instruction;

import IntermediatePresentation.Array.ArrayInitializer;
import IntermediatePresentation.ConstNumber;
import IntermediatePresentation.IRManager;
import IntermediatePresentation.User;
import IntermediatePresentation.Value;
import IntermediatePresentation.ValueType;
import TargetCode.GlobalData.Word;

import java.util.ArrayList;

public class GlobalDecl extends Instruction {
    private boolean isConst = false;

    public GlobalDecl(Value val) {
        super(IRManager.getInstance().declareVar(), ValueType.PI32);
        use(val);
        IRManager.getModule().addGobalDecl(this);
        if (val instanceof ArrayInitializer aInit) {
            vType = new ValueType(aInit.getLength());
        }
    }

    public GlobalDecl(Value val, boolean isConst) {
        super(IRManager.getInstance().declareVar(), ValueType.PI32);
        use(val);
        IRManager.getModule().addGobalDecl(this);
        if (val instanceof ArrayInitializer aInit) {
            vType = new ValueType(aInit.getLength());
        }
        this.isConst = isConst;
    }

    public String toString() {
        Value init = operandList.get(0);
        if (init instanceof ArrayInitializer aInit) {
            return reg + " = dso_local global " + vType + " " + aInit + "\n ";
        } else {
            return reg + " = dso_local global i32 " + init + "\n";
        }
    }

    public void toMips() {
        Value init = operandList.get(0);
        if (init instanceof ArrayInitializer aInit) {
            ArrayList<Integer> vals = new ArrayList<>();
            if (aInit.getVals().size() == 0) {
                for (int i = 0; i < aInit.getLength(); i++) {
                    vals.add(0);
                }
            } else {
                for (Value v : aInit.getVals()) {
                    vals.add(0, ((ConstNumber) v).getVal());
                }
            }
            new Word(this, getName(), vals);
        } else {
            new Word(this, getName(), ((ConstNumber) init).getVal());
        }
    }

    public ArrayList<String> GVNHash() {
        return null;
    }

    public boolean isArray() {
        return operandList.get(0) instanceof ArrayInitializer;
    }

    public void beReplacedBy(Value v) {
        for (User user : userList) {
            ArrayList<Value> newOperandList = new ArrayList<>(user.getOperandList());
            for (int i = 0; i < user.getOperandList().size(); i++) {
                if (user.getOperandList().get(i).equals(this)) {
                    newOperandList.set(i, v);
                }
            }


            user.setOperandList(newOperandList);
            v.usedBy(user);

            if (user instanceof Store store) {
                store.setAddr(v);
            }
            if (user instanceof Load load) {
                load.setAddr(v);
            }
        }
    }

    public Value getInit() {
        return operandList.get(0);
    }

    public boolean isConst() {
        return isConst;
    }

    public int getConstValAtIndex(int index) {
        Value init = operandList.get(0);
        if (init instanceof ArrayInitializer arrayInitializer) {
            return ((ConstNumber) arrayInitializer.getVals().get(index)).getVal();
        } else {
            throw new RuntimeException();
        }
    }
}
