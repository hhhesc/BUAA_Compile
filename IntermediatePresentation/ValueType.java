package IntermediatePresentation;

public class ValueType {
    public static final ValueType I32 = new ValueType("i32");
    public static final ValueType PI32 = new ValueType("i32*");
    public static final ValueType I1 = new ValueType("i1");
    public static final ValueType NULL = new ValueType("void");
    public static final ValueType PI8 = new ValueType("i8*");
    public static final ValueType I8 = new ValueType("i8");
    public static final ValueType ARRAY = new ValueType("array");

    private final String type;
    private int len = 1;

    public ValueType(String type) {
        this.type = type;
    }

    public ValueType(int len) {
        this.type = "array";
        this.len = len;
    }

    public boolean equals(Object o) {
        if (o instanceof ValueType oType) {
            if (type.equals("array")) {
                return oType.toString().startsWith("[");
            } else {
                return type.equals(oType.toString());
            }
        } else {
            return false;
        }
    }

    public String toString() {
        if (!type.equals("array")) {
            return type;
        } else {
            return "[ " + len + " x i32 ]";
        }
    }

    public int getLength() {
        return len;
    }

    public String getRefTypeString() {
        if (type.equals("array")) {
            return "[ " + len + " x i32 ]";
        } else {
            return type.substring(0, type.length() - 1);
        }
    }

    public boolean isPointer() {
        return type.equals("i32*") || type.equals("i8*") || type.equals("array");
    }
}
