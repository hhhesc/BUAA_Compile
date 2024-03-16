package Optimizer;

import IntermediatePresentation.BasicBlock;
import IntermediatePresentation.Function.Function;
import IntermediatePresentation.Function.MainFunction;
import IntermediatePresentation.Instruction.GlobalDecl;
import IntermediatePresentation.Instruction.LocalDecl;
import IntermediatePresentation.Instruction.Store;
import IntermediatePresentation.Module;
import IntermediatePresentation.User;
import IntermediatePresentation.Value;

public class GlobalDeclLocalization {
    private Module module;

    public GlobalDeclLocalization() {
        module = Optimizer.instance().getModule();
    }

    public void optimize() {
        for (GlobalDecl globalDecl : module.getGlobalDecls()) {
            if (globalDecl.isArray()) {
                //如果是全局数组，做不做初始化其实都没啥区别了
                continue;
            }
            Function usedFunction = null;
            boolean canLocalize = true;
            for (User user : globalDecl.getUserList()) {
                BasicBlock block = user.getBlock();
                if (block != null) {
                    Function func = block.getFunction();
                    if (usedFunction == null) {
                        usedFunction = func;
                    } else if (!usedFunction.equals(func)) {
                        canLocalize = false;
                        break;
                    }
                }
            }

            //说明仅被一个函数使用，且该函数最多被调用一次，则可以局部化
            if (canLocalize && usedFunction instanceof MainFunction) {
                BasicBlock firstBlock = usedFunction.getEntranceBlock();
                //局部化在最开始就可以做，不需要考虑phi等
                LocalDecl localDecl = new LocalDecl();
                Value init = globalDecl.getInit();
                Store initStore = new Store(init, localDecl);
                firstBlock.addInstructionAt(0, initStore);
                firstBlock.addInstructionAt(0, localDecl);
                globalDecl.beReplacedBy(localDecl);
                module.removeGlobalDecl(globalDecl);
                globalDecl.destroy();
            }
        }
    }
}
