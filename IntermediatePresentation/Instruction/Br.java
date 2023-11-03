package IntermediatePresentation.Instruction;

import IntermediatePresentation.BasicBlock;
import IntermediatePresentation.Value;

public class Br extends Instruction {
    private Value cond;
    public BasicBlock ifTrue;
    private BasicBlock ifFalse;
    private BasicBlock dest = null;

    public Br(Value cond, BasicBlock ifTrue, BasicBlock ifFalse) {
        super("BR");
        this.cond = cond;
        use(cond);
        this.ifTrue = ifTrue;
        use(ifTrue);
        this.ifFalse = ifFalse;
        use(ifFalse);
    }

    public Br(BasicBlock dest) {
        super("BR");
        this.dest = dest;
        use(dest);
    }

    public String toString() {
        if (dest == null) {
            return "br i1 " + cond.getReg() + ", label %" + ifTrue.getReg() +
                    ", label %" + ifFalse.getReg() + "\n";
        } else {
            return "br label %" + dest.getReg() + "\n";
        }
    }
}
