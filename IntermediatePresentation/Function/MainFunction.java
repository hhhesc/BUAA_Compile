package IntermediatePresentation.Function;

import IntermediatePresentation.BasicBlock;
import IntermediatePresentation.IRManager;
import IntermediatePresentation.User;
import IntermediatePresentation.ValueType;

import java.util.ArrayList;

public class MainFunction extends Function {
    public MainFunction() {
        super("@main", new Param(), ValueType.I32);
        IRManager.getModule().setMainFunction(this);
    }
}
