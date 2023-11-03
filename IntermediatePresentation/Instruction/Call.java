package IntermediatePresentation.Instruction;

import IntermediatePresentation.Function.Function;
import IntermediatePresentation.Function.Param;
import IntermediatePresentation.IRManager;
import IntermediatePresentation.Value;

import java.util.ArrayList;

public class Call extends Instruction {
    private ArrayList<Value> params = new ArrayList<>();
    private final Function function;

    public Call(Function function, ArrayList<Value> params) {
        super("CALL", function.getType());
        this.function = function;
        if (function.isVoid()) {
            use(function);
        } else {
            reg = IRManager.getInstance().declareTempVar();
            use(function);
        }

        for (Value v : params) {
            use(v);
            this.params.add(v);
        }
    }

    public Call(Function function, Value... params) {
        super("CALL");
        this.function = function;
        if (function.isVoid()) {
            use(function);
        } else {
            reg = IRManager.getInstance().declareTempVar();
            use(function);
        }

        for (Value v : params) {
            use(v);
            this.params.add(v);
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (function.isVoid()) {
            sb.append("call void ");
        } else {
            sb.append(reg).append(" = call ").append(function.getTypeString()).append(" ");
        }
        sb.append(function.getReg()).append("(");

        for (Value param : params) {
            sb.append(param.getTypeString()).append(" ");
            sb.append(param.getReg()).append(", ");
        }
        if (params.size() != 0) {
            sb = new StringBuilder(sb.substring(0, sb.length() - 2));
        }
        sb.append(")\n");
        return sb.toString();
    }
}
