package IntermediatePresentation.Function;

import IntermediatePresentation.BasicBlock;
import IntermediatePresentation.IRManager;
import IntermediatePresentation.User;
import IntermediatePresentation.Value;
import IntermediatePresentation.ValueType;
import TargetCode.Label;
import TargetCode.MipsManager;

import java.util.ArrayList;

public class Function extends User {
    protected Param param;
    protected ArrayList<BasicBlock> bbs = new ArrayList<>();

    public static Function getint = new Function("@getint", new Param(), ValueType.I32);
    public static Function putint = new Function("@putint",
            new Param(new Value("i32", ValueType.I32)), ValueType.NULL);

    public Function(String name, Param param, ValueType type) {
        super(name, type);
        use(param);
        this.param = param;
        if (!(name.matches("(@main)|(@getint)|(@putint)|(@putch)|(@putstr)"))) {
            IRManager.getModule().addFunction(this);
        }

        IRManager.getInstance().setCurFunction(this);
        new BasicBlock();
    }

    public Function(String name, ValueType type) {
        super(name, type);
        if (!(name.matches("(@main)|(@getint)|(@putint)|(@putch)|(@putstr)"))) {
            IRManager.getModule().addFunction(this);
        }

        IRManager.getInstance().setCurFunction(this);
        new BasicBlock();
    }

    public void addBlock(BasicBlock bb) {
        bbs.add(bb);
    }

    public boolean isVoid() {
        return vType == ValueType.NULL;
    }

    public void setParam(Param param) {
        this.param = param;
        use(param);
    }

    public Param getParam() {
        return param;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        String type = isVoid() ? "void" : "i32";
        sb.append("define dso_local ").append(type).append(" ").append(reg);
        sb.append("(").append(param).append(")").append(" {\n");
        for (BasicBlock bb : bbs) {
            sb.append(bb);
        }
        sb.append("}\n\n");
        return sb.toString();
    }

    public void toMips() {
        super.toMips();
        Label label = new Label("function_" + reg.substring(1));
        MipsManager.instance().insertLabel(label);
        MipsManager.instance().tagFunctionWithLabel(this, label);
        param.toMips();
        for (BasicBlock bb : bbs) {
            bb.toMips();
        }
    }
}
