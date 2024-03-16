package IntermediatePresentation;

import IntermediatePresentation.Instruction.Phi;

import java.util.ArrayList;

public class Value {
    protected ArrayList<User> userList = new ArrayList<>();
    protected ValueType vType;

    protected String reg;

    public Value(String reg, ValueType vType) {
        this.reg = reg;
        this.vType = vType;
    }

    public void usedBy(User value) {
        userList.add(value);
    }

    public String getReg() {
        return reg;
    }

    public String toString() {
        return reg;
    }

    public ValueType getType() {
        return vType;
    }

    public ValueType getRefType() {
        if (vType == ValueType.PI8) {
            return ValueType.I8;
        } else if (vType == ValueType.PI32) {
            return ValueType.I32;
        } else {
            return ValueType.NULL;
        }
    }

    public boolean isPointer() {
        return vType == ValueType.PI8 || vType == ValueType.PI32 || vType.equals(ValueType.ARRAY);
    }

    public String getTypeString() {
        return vType.toString();
    }

    public void toMips() {
    }

    public String getName() {
        return reg.substring(1);
    }

    public ArrayList<User> getUserList() {
        return new ArrayList<>(userList);
    }

    public void removeUser(User user) {
        userList.remove(user);
    }

    public void beReplacedBy(Value v) {
        for (User user : userList) {
            ArrayList<Value> newOperandList = new ArrayList<>(user.operandList);
            for (int i = 0; i < user.operandList.size(); i++) {
                if (user.operandList.get(i).equals(this)) {
                    newOperandList.set(i, v);
                }
            }
            user.operandList = newOperandList;
            v.usedBy(user);
        }
    }

    public boolean isUseless() {
        return userList.size() == 0;
    }
}
