package IntermediatePresentation;

import TargetCode.GlobalData.Asciiz;

public class ConstString extends Value {

    private final String str;

    public ConstString(String strVal) {
        super(IRManager.getInstance().declareString(), new ValueType(strVal.length() + 1));
        str = strVal;
        IRManager.getModule().addConstString(this);
    }

    public String toString() {
        return reg + " = constant [ " +
                (str.length() + 1) + " x i8 ] c\"" + str + "\\00\"\n";
    }

    public void toMips() {
        new Asciiz(this, getName(), str.replaceAll("\n", "\\\\n"));
    }
}
