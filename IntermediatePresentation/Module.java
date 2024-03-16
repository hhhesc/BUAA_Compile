package IntermediatePresentation;

import IntermediatePresentation.Function.Function;
import IntermediatePresentation.Function.MainFunction;
import IntermediatePresentation.Instruction.GlobalDecl;
import TargetCode.MipsFile;
import TargetCode.MipsManager;

import java.util.ArrayList;

public class Module extends Value {
    private final ArrayList<GlobalDecl> globalDecls = new ArrayList<>();
    private final ArrayList<ConstString> constStrings = new ArrayList<>();
    private final ArrayList<Function> functions = new ArrayList<>();
    private MainFunction mainFunction = null;

    public Module() {
        super("MODULE", ValueType.NULL);
    }

    public void addGobalDecl(GlobalDecl globalDecl) {
        globalDecls.add(globalDecl);
    }

    public void addConstString(ConstString constString) {
        constStrings.add(constString);
    }

    public void addFunction(Function function) {
        functions.add(function);
    }

    public void setMainFunction(MainFunction mainFunction) {
        this.mainFunction = mainFunction;
    }

    public ArrayList<Function> getFunctions() {
        return functions;
    }

    public MainFunction getMainFunction() {
        return mainFunction;
    }

    public ArrayList<Function> getAllFunctions() {
        ArrayList<Function> allFunctions = new ArrayList<>(functions);
        allFunctions.add(mainFunction);
        return allFunctions;
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
        for (ConstString constString : constStrings) {
            sb.append(constString.toString());
        }
        sb.append("\n");
        sb.append(mainFunction.toString());
        for (Function function : functions) {
            sb.append(function.toString());
        }
        return sb.toString();
    }

    public MipsFile toMipsFile() {
        for (GlobalDecl globalDecl : globalDecls) {
            globalDecl.toMips();
        }
        for (ConstString constString : constStrings) {
            constString.toMips();
        }

        for (Function function : functions) {
            function.toMips();
        }
        mainFunction.toMips();
        return MipsManager.getFile();
    }

    public ArrayList<GlobalDecl> getGlobalDecls() {
        return new ArrayList<>(globalDecls);
    }

    public void removeGlobalDecl(GlobalDecl globalDecl) {
        globalDecls.remove(globalDecl);
    }
}
