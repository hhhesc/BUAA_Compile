package IntermediatePresentation.Function;

import IntermediatePresentation.Value;
import IntermediatePresentation.ValueType;
import TargetCode.Instruction.Memory.Lw;
import TargetCode.Instruction.Memory.Sw;
import TargetCode.MipsManager;
import TargetCode.RegisterManager;

import java.util.ArrayList;
import java.util.Collections;

public class Param extends Value {
    ArrayList<Value> params = new ArrayList<>();

    public Param() {
        super("PARAM", ValueType.NULL);
    }

    public Param(Value... params) {
        super("PARAM", ValueType.NULL);
        Collections.addAll(this.params, params);
    }

    public void addParam(Value param) {
        params.add(param);
    }

    public ArrayList<Value> getParams() {
        return params;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Value param : params) {
            sb.append(param.getTypeString()).append(" ").append(param).append(", ");
        }
        if (params.size() > 0) {
            return sb.substring(0, sb.length() - 2);
        } else {
            return "";
        }
    }

    public void toMips() {
        //处理形参，这里不在call中处理，因为那里是实参
        int paramNumber = params.size();

        for (int i = 0; i < 4; i++) {
            if (i == paramNumber) {
                break;
            }
            RegisterManager.instance().setRegOf(params.get(i), RegisterManager.instance().getParamRegister(i));
            MipsManager.instance().allocEmptyStackSpace();
        }

        if (paramNumber >= 5) {
            for (int i = 4; i < paramNumber; i++) {
                MipsManager.instance().pushValue(params.get(i));
            }
        }
    }
}
