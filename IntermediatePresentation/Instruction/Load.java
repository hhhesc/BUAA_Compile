package IntermediatePresentation.Instruction;

import IntermediatePresentation.Value;

public class Load extends Instruction {
    private final Value addr;

    public Load(String reg, Value addr) {
        super(reg, addr.getRefType());
        this.addr = addr;
        use(addr);
    }

    public String toString() {
        return reg + " = load " + addr.getRefType() + ", " + addr.getTypeString() + " " + addr.getReg() + "\n ";
    }
}
