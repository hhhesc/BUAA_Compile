package IntermediatePresentation.Array;

import IntermediatePresentation.BasicBlock;
import IntermediatePresentation.IRManager;
import IntermediatePresentation.User;
import IntermediatePresentation.Value;
import IntermediatePresentation.ValueType;

import java.util.ArrayList;

public class ArrayInitializer extends User {
    private ArrayList<Value> initVals = new ArrayList<>();
    private boolean isZeroInit = false;

    private int length;
    private BasicBlock block;

    public ArrayInitializer(ArrayList<Value> initVals) {
        super("ARRAY_INIT", ValueType.NULL);
        this.initVals = initVals;
        for (Value v : initVals) {
            use(v);
        }
        length = initVals.size();
        block = IRManager.getInstance().getCurBlock();
    }

    public ArrayInitializer(int len) {
        super("ARRAY_INIT", ValueType.NULL);
        //全部填0
        this.isZeroInit = true;
        length = len;
    }

    public ArrayList<Value> getVals() {
        return initVals;
    }

    public int getLength() {
        return length;
    }

    public void merge(ArrayInitializer other) {
        initVals.addAll(other.getVals());
        length += other.getLength();
    }

    public void add(Value v) {
        use(v);
        initVals.add(v);
        length++;
    }

    public String toString() {
        if (isZeroInit) {
            return "zeroinitializer";
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("[ ");
            for (Value v : initVals) {
                sb.append("i32 ").append(v).append(", ");
            }
            sb.delete(sb.length() - 2, sb.length());
            sb.append(" ]");
            return sb.toString();
        }
    }

    public BasicBlock getBlock() {
        return block;
    }
}
