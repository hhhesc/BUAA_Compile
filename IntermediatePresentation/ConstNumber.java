package IntermediatePresentation;

public class ConstNumber extends Value {
    public ConstNumber(String val) {
        super(val, ValueType.I32);
    }

    public ConstNumber(int val) {
        super(Integer.toString(val), ValueType.I32);
    }

    public ConstNumber(int val, ValueType type) {
        super(Integer.toString(val), type);
    }

    public int getVal() {
        return Integer.parseInt(reg);
    }
}
