package IntermediatePresentation;

import java.util.ArrayList;

public class User extends Value{
    protected ArrayList<Value> operandList = new ArrayList<>();

    public User(String regName,ValueType VType) {
        super(regName,VType);
    }

    public void use(Value value) {
        operandList.add(value);
        value.usedBy(this);
    }
}
