package IntermediatePresentation;

import IntermediatePresentation.Function.Function;
import IntermediatePresentation.Function.MainFunction;
import IntermediatePresentation.Instruction.GlobalDecl;

import java.util.ArrayList;

public class Module extends Value {
    ArrayList<GlobalDecl> globalDecls = new ArrayList<>();
    ArrayList<Function> functions = new ArrayList<>();
    MainFunction mainFunction = null;

    public Module() {
        super("MODULE", ValueType.NULL);
    }

    public void addGobalDecl(GlobalDecl globalDecl) {
        globalDecls.add(globalDecl);
    }

    public void addFunction(Function function) {
        functions.add(function);
    }

    public void setMainFunction(MainFunction mainFunction) {
        this.mainFunction = mainFunction;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("declare i32 @getint()\n");
        sb.append("declare void @putint(i32)\n");
        sb.append("declare void @putch(i32)\n");
        sb.append("declare void @putstr(i8*)\n\n");
        for (GlobalDecl globalDecl : globalDecls) {
            sb.append(globalDecl.toString());
        }
        sb.append("\n");
        for (Function function : functions) {
            sb.append(function.toString());
        }
        sb.append(mainFunction.toString());
        return sb.toString();
    }
}
