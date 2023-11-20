package TargetCode.GlobalData;

/*
    数据段的声明：
    .data
    <name>: .word init -> int型变量
    <name>: .space len -> 数组,len=4*arrayLen
    <name>: .asciiz str -> 字符串分配

    .text
    首先对数组进行初值赋值
 */

import IntermediatePresentation.Value;
import TargetCode.MipsManager;

public class DataDecl {
    protected final String spaceName;
    protected final Value v;

    public DataDecl(Value v, String spaceName) {
        this.spaceName = spaceName;
        this.v = v;
        MipsManager.instance().declGlobalData(v, spaceName);
        MipsManager.getFile().addData(this);
    }

    public String getName() {
        return spaceName;
    }
}
