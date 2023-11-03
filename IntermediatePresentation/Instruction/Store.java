package IntermediatePresentation.Instruction;

import IntermediatePresentation.IRManager;
import IntermediatePresentation.Value;
import IntermediatePresentation.ValueType;

public class Store extends Instruction {
    private final Value val;
    private final Value addr;

    public Store(Value val, Value addr) {
        super("STORE");
        if (val.isPointer()) {
            IRManager.getInstance().deleteInstruction();
            val = new Load(IRManager.getInstance().declareTempVar(), val);
            IRManager.getInstance().instrCreated(this);
        }
        if (val.getType() != ValueType.I32) {
            IRManager.getInstance().deleteInstruction();
            val = new ZextTo(val, ValueType.I32);
            IRManager.getInstance().instrCreated(this);
        }
        this.val = val;
        use(val);
        this.addr = addr;
        use(addr);
    }

    public String toString() {
        return "store i32 " + val.getReg() + ", i32* " + addr.getReg() + "\n";
    }
}
