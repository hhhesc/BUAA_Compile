package IntermediatePresentation;

import java.util.HashMap;

public class Value {
    protected HashMap<String, Value> userList = new HashMap<>();
    protected ValueType vType;

    protected String reg;

    public Value(String reg, ValueType vType) {
        this.reg = reg;
        this.vType = vType;
    }

    public void usedBy(Value value) {
        userList.put(value.getReg(), value);
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

}
