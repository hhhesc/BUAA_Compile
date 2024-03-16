package IntermediatePresentation;

import java.util.ArrayList;

public class User extends Value {
    protected ArrayList<Value> operandList = new ArrayList<>();

    public User(String regName, ValueType VType) {
        super(regName, VType);
    }

    public void use(Value value) {
        operandList.add(value);
        value.usedBy(this);
    }

    public void removeOperand(Value value) {
        operandList.remove(value);
    }

    public ArrayList<Value> getOperandList() {
        return operandList;
    }

    public BasicBlock getBlock() {
        return null;
    }

    public void setOperandList(ArrayList<Value> operandList) {
        this.operandList = operandList;
    }
}
