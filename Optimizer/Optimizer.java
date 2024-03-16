package Optimizer;

import IntermediatePresentation.BasicBlock;
import IntermediatePresentation.Function.Function;
import IntermediatePresentation.IRManager;
import IntermediatePresentation.Instruction.Call;
import IntermediatePresentation.Module;
import IntermediatePresentation.Value;
import TargetCode.MipsFile;
import TargetCode.MipsManager;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

public class Optimizer {

    private Module module;
    private DominAnalyzer dominAnalyzer;
    private MipsFile mipsFile;

    private FunctionOptimize functionOptimize;

    private RegisterDispatch registerDispatch;

    private RegAllocator allocator;

    private LoopAnalyze loopAnalyze;

    private Optimizer() {
    }

    private static final Optimizer INSTANCE = new Optimizer();

    public static Optimizer instance() {
        return INSTANCE;
    }

    public void optimizeModule(Module module) throws IOException {
        this.module = module;
        //取消自动插入，方便对指令进行调整
        IRManager.getInstance().setAutoInsert(false);

        //函数调用分析+内联
        FunctionOptimize functionOptimize = new FunctionOptimize();
        functionOptimize.analyze();
        this.functionOptimize = functionOptimize;

        GlobalDeclLocalization globalDeclLocalization = new GlobalDeclLocalization();
        globalDeclLocalization.optimize();

        //进行各类分析并删除无用代码
        analyze();

        functionOptimize.optimize();

        globalDeclLocalization.optimize();

        analyze();

        //Memory To Register
        Mem2Reg mem2Reg = new Mem2Reg();
        mem2Reg.optimize(module);

        //修改了图结构，重新分析
        analyze();

        BufferedWriter bw = new BufferedWriter(new FileWriter("ir_phi.txt"));
        bw.write(IRManager.getModule().toString());
        bw.flush();
        bw.close();


        //消除phi
        mem2Reg.phiToMove();

        //常量折叠和强度削弱
        ConstFold constFold = new ConstFold();
       constFold.optimize();

        analyze();

        //gvn只对move考虑了而没有对phi考虑，所以要在消除phi之后
        GVN gvn = new GVN();
        gvn.optimize();
        GCM gcm = new GCM();
        gcm.optimize();

        analyze();

        constFold.optimize();

        (new ToCmpThenBr()).optimize();

        //寄存器分配
        RegisterDispatch registerDispatch = new RegisterDispatch();
        registerDispatch.dispatch();
        this.registerDispatch = registerDispatch;

//        RegAllocator regAllocator = new RegAllocator();
//        regAllocator.allocate();
//        this.allocator = regAllocator;
    }


    public void optimizeMips(MipsFile mipsFile) {
        this.mipsFile = mipsFile;
        MipsManager.instance().setAutoInsert(false);
        //基本块排序
        BlockRequeue blockRequeue = new BlockRequeue();
        blockRequeue.optimize();
        //后端窥孔
        //必须先排序再窥孔，否则可能导致没有j指令，跳转出错
        PeepHole peepHole = new PeepHole();
        peepHole.optimizeMips();
        peepHole.optimizeMips();
        //块内指令合并
        InstructionMerge instructionMerge = new InstructionMerge();
        instructionMerge.optimize();
        peepHole.optimizeMips();
    }

    public void analyze() {
        //预处理跳转指令，以便流图分析
        DeadCode deadCode = new DeadCode(module);
        deadCode.scanJump();
        //流图分析
        ControlFlowGraph CFG = new ControlFlowGraph();
        CFG.analyze(module);
        //删除不可达块，以便支配分析和phi的插入;并删除无用指令
        deadCode.optimize(CFG);
        //重新进行流图分析
        CFG = new ControlFlowGraph();
        CFG.analyze(module);
        //支配分析
        DominAnalyzer dominAnalyzer = new DominAnalyzer();
        dominAnalyzer.analyze(CFG);
        this.dominAnalyzer = dominAnalyzer;

        //循环分析
        LoopAnalyze loopAnalyze = new LoopAnalyze();
        loopAnalyze.analyze();
        this.loopAnalyze = loopAnalyze;

        deadCode.optimize(CFG);

        CFG = new ControlFlowGraph();
        CFG.analyze(module);
        dominAnalyzer = new DominAnalyzer();
        dominAnalyzer.analyze(CFG);
        this.dominAnalyzer = dominAnalyzer;
    }

    public void dominAnalyze() {
        ControlFlowGraph CFG = new ControlFlowGraph();
        CFG.analyze(module);
        dominAnalyzer = new DominAnalyzer();
        dominAnalyzer.analyze(CFG);

        LoopAnalyze loopAnalyze = new LoopAnalyze();
        loopAnalyze.analyze();
        this.loopAnalyze = loopAnalyze;
    }

    public void dominAnalyze(Function f) {
        ControlFlowGraph CFG = new ControlFlowGraph();
        CFG.analyze(module);

        dominAnalyzer.analyze(CFG, f);

        LoopAnalyze loopAnalyze = new LoopAnalyze();
        loopAnalyze.analyze();
        this.loopAnalyze = loopAnalyze;
    }

    public void analyze(Function f) {
        DeadCode deadCode = new DeadCode(module);
        deadCode.scanJump();

        ControlFlowGraph CFG = new ControlFlowGraph();
        CFG.analyze(module);

        deadCode.optimize(CFG, f);

        CFG = new ControlFlowGraph();
        CFG.analyze(module);

        dominAnalyzer.analyze(CFG, f);

        LoopAnalyze loopAnalyze = new LoopAnalyze();
        loopAnalyze.analyze();
        this.loopAnalyze = loopAnalyze;

        deadCode.optimize(CFG, f);

        CFG = new ControlFlowGraph();
        CFG.analyze(module);
        dominAnalyzer.analyze(CFG, f);
    }

    public boolean hasOptimized() {
        return module != null;
    }

    public ControlFlowGraph getCFG() {
        return dominAnalyzer.getCFG();
    }

    public ArrayList<BasicBlock> bfsDominTreeArray(BasicBlock entry) {
        return dominAnalyzer.bfsDominTreeArray(entry);
    }

    public DominAnalyzer getDominAnalyzer() {
        return dominAnalyzer;
    }

    public Module getModule() {
        return module;
    }

    public MipsFile getMipsFile() {
        return mipsFile;
    }

    public boolean hasSideEffect(Function function) {
        return functionOptimize.hasSideEffect(function);
    }

    public ArrayList<Function> closurseWhenCall(Function function) {
        return functionOptimize.closurseWhenCall(function);
    }

    public HashSet<Value> activeValuesWhenCall(Call call) {
        if (registerDispatch == null) {
            return allocator.activeValuesWhenCall(call);
        } else {
            return registerDispatch.activeValuesWhenCall(call);
        }
    }

    public boolean hasDispatched() {
        return registerDispatch != null || allocator != null;
    }

    public LoopAnalyze getLoopAnalyze() {
        return loopAnalyze;
    }

    public boolean calledOnce(Function function) {
        return functionOptimize.calledOnce(function);
    }

    public FunctionOptimize getFunctionOptimize() {
        return functionOptimize;
    }
}
