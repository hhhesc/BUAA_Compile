package IntermediatePresentation.Function;

import IntermediatePresentation.Value;
import IntermediatePresentation.ValueType;

import java.util.ArrayList;
import java.util.Collections;

public class Param extends Value {
    ArrayList<Value> params = new ArrayList<>();

    public Param() {
        super("PARAM", ValueType.NULL);
    }

    public Param(Value... params) {
        super("PARAM",ValueType.NULL);
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
        if (params.size()>0) {
            return sb.substring(0,sb.length()-2);
        } else {
            return "";
        }
    }
}
