package Optimizer;

import IntermediatePresentation.BasicBlock;
import IntermediatePresentation.ConstNumber;
import IntermediatePresentation.Function.Function;
import IntermediatePresentation.Instruction.ALU;
import IntermediatePresentation.Instruction.Br;
import IntermediatePresentation.Instruction.Call;
import IntermediatePresentation.Instruction.GetElementPtr;
import IntermediatePresentation.Instruction.Icmp;
import IntermediatePresentation.Instruction.Instruction;
import IntermediatePresentation.Instruction.Load;
import IntermediatePresentation.Instruction.Shift;
import IntermediatePresentation.Instruction.ZextTo;
import IntermediatePresentation.User;
import IntermediatePresentation.Value;
import IntermediatePresentation.ValueType;

import java.util.ArrayList;

public class ConstFold {
    public void optimize() {
        for (Function f : Optimizer.instance().getModule().getAllFunctions()) {
            for (BasicBlock bb : f.getBlocks()) {
                boolean hasChanged = true;
                while (hasChanged) {
                    hasChanged = false;
                    ArrayList<Instruction> instructions = new ArrayList<>(bb.getInstructionList());
                    for (Instruction instruction : instructions) {
                        if (instruction instanceof GetElementPtr gep) {
                            if (gep.canGetConstNumber()) {
                                int n = gep.getStorageVal();
                                for (User user : gep.getUserList()) {
                                    if (user instanceof Load load) {
                                        load.beReplacedBy(new ConstNumber(n));
                                        load.destroy();
                                        load.getBlock().removeInstruction(load);
                                        hasChanged = true;
                                    }
                                }
                                //还可能作为参数，不能删
                                if (hasChanged) {
                                    continue;
                                }
                            }
                        }

                        if (instruction instanceof ALU alu) {
                            if (alu.isConst()) {
                                alu.beReplacedBy(alu.toConstNumber());
                                alu.getBlock().removeInstruction(alu);
                                alu.destroy();
                                hasChanged = true;
                                continue;
                            }

                            hasChanged |= switch (alu.getAluType()) {
                                case "mul" -> multOptimize(alu);
                                case "add" -> addOptimize(alu);
                                case "sub" -> subOptimize(alu);
                                case "sdiv" -> divOptimize(alu);
                                case "srem" -> remOptimize(alu);
                                default -> false;
                            };
                        } else if (instruction instanceof Icmp icmp) {
                            if (icmp.isConst()) {
                                icmp.beReplacedBy(icmp.toConstNumber());
                                icmp.getBlock().removeInstruction(icmp);
                                icmp.destroy();
                                hasChanged = true;
                            }
                        } else if (instruction instanceof Br br) {
                            if (br.getCond() != null && br.getCond() instanceof ConstNumber n) {
                                int index = br.getBlock().getInstructionList().indexOf(br);
                                Br newBr;
                                if (n.getVal() == 0) {
                                    newBr = new Br(br.getIfFalse());
                                } else {
                                    newBr = new Br(br.getIfTrue());
                                }
                                br.beReplacedBy(newBr);
                                br.getBlock().removeInstruction(br);
                                br.destroy();
                                br.getBlock().addInstructionAt(index, newBr);
                                hasChanged = true;
                            }
                        } else if (instruction instanceof Call call) {
                            Integer retVal = call.toConst();
                            if (retVal != null) {
                                call.beReplacedBy(new ConstNumber(retVal));
                                call.getBlock().removeInstruction(call);
                                call.destroy();
                            }
                        } else if (instruction instanceof ZextTo zextTo) {
                            if (zextTo.getOperandList().get(0) instanceof ConstNumber n) {
                                zextTo.beReplacedBy(n);
                                zextTo.getBlock().removeInstruction(zextTo);
                                zextTo.destroy();
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean multOptimize(ALU mult) {
        Value lOperand = mult.getOperand1();
        Value rOperand = mult.getOperand2();
        if (lOperand instanceof ConstNumber && rOperand instanceof ConstNumber) {
            return false;
        }
        //尝试用shift代替
        if (lOperand instanceof ConstNumber || rOperand instanceof ConstNumber) {
            Value operand;
            int imm;
            if (lOperand instanceof ConstNumber) {
                imm = ((ConstNumber) lOperand).getVal();
                operand = rOperand;
            } else {
                imm = ((ConstNumber) rOperand).getVal();
                operand = lOperand;
            }

            boolean negative = false;
            if (imm < 0) {
                negative = true;
                imm = -imm;
            }

            //构建shift指令
            String binString = Integer.toBinaryString(imm);
            //统计1的数目，如果多于2个就不能优化
            ArrayList<Integer> shiftLengths = new ArrayList<>();
            char[] charArray = binString.toCharArray();
            for (int i = 0; i < charArray.length; i++) {
                char c = binString.charAt(i);
                if (c == '1') {
                    shiftLengths.add(charArray.length - i - 1);
                    if (shiftLengths.size() > 2) {
                        return false;
                    }
                }
            }

            ArrayList<Instruction> shifts = new ArrayList<>();
            Instruction finalValue;
            if (shiftLengths.size() == 0) {
                //是乘以0,就直接给一个0
                finalValue = new ALU(new ConstNumber(0), "-", new ConstNumber(0));
                shifts.add(finalValue);
            } else if (shiftLengths.size() == 1) {
                //就一个移位就可以
                finalValue = new Shift(false, operand, new ConstNumber(shiftLengths.get(0)));
                shifts.add(finalValue);
            } else {
                //两个移位，和一个加法
                Value shift1 = operand;
                if (shiftLengths.get(0) != 0) {
                    shift1 = new Shift(false, operand, new ConstNumber(shiftLengths.get(0)));
                    shifts.add((Instruction) shift1);
                }
                Value shift2 = operand;
                if (shiftLengths.get(1) != 0) {
                    shift2 = new Shift(false, operand, new ConstNumber(shiftLengths.get(1)));
                    shifts.add((Instruction) shift2);
                }
                finalValue = new ALU(shift1, "+", shift2);
                shifts.add(finalValue);
            }

            //用这些指令替换
            ArrayList<Instruction> instrList = mult.getBlock().getInstructionList();
            if (negative) {
                ALU neg = new ALU(new ConstNumber(0), "-", finalValue);
                finalValue = neg;
                shifts.add(neg);
            }

            int index = instrList.indexOf(mult);
            mult.beReplacedBy(finalValue);
            mult.getBlock().removeInstruction(mult);
            mult.destroy();
            for (Instruction instr : shifts) {
                //因为有GCM，所以这里顺序其实无妨
                mult.getBlock().addInstructionAt(index, instr);
            }
            return true;
        }
        return false;
    }

    private boolean addOptimize(ALU add) {
        Value lOperand = add.getOperand1();
        Value rOperand = add.getOperand2();
        if (lOperand instanceof ConstNumber && rOperand instanceof ConstNumber) {
            return false;
        }

        if (lOperand instanceof ConstNumber || rOperand instanceof ConstNumber) {
            Value operand;
            int imm;
            if (lOperand instanceof ConstNumber) {
                imm = ((ConstNumber) lOperand).getVal();
                operand = rOperand;
            } else {
                imm = ((ConstNumber) rOperand).getVal();
                operand = lOperand;
            }

            //a+0=0+a=a
            if (imm == 0) {
                add.beReplacedBy(operand);
                add.getBlock().removeInstruction(add);
                add.destroy();
                return true;
            }

            //addi可以和使用它的addi或subi合并
            for (User user : add.getUserList()) {
                if (user instanceof ALU aluUser) {
                    if (aluUser.getAluType().equals("add")) {
                        //add des1,op,imm ; add des2,des1,n -> add des2,op,imm1+n
                        if (aluUser.getOperand1() instanceof ConstNumber n) {
                            ALU mergedAdd = new ALU(new ConstNumber(imm + n.getVal()), "+", operand);
                            replaceInstrWith(aluUser, mergedAdd);
                            return true;
                        } else if (aluUser.getOperand2() instanceof ConstNumber n) {
                            ALU mergedAdd = new ALU(new ConstNumber(imm + n.getVal()), "+", operand);
                            replaceInstrWith(aluUser, mergedAdd);
                            return true;
                        }
                    } else if (aluUser.getAluType().equals("sub")) {
                        //add des1,op,imm ; sub des2,des1,n -> add des2,op,imm-n
                        //add des1,op,imm ; sub des2,n,des1 -> sub des2,n-imm,op
                        if (aluUser.getOperand1() instanceof ConstNumber n) {
                            ALU mergedSub = new ALU(new ConstNumber(-imm + n.getVal()), "-", operand);
                            replaceInstrWith(aluUser, mergedSub);
                            return true;
                        } else if (aluUser.getOperand2() instanceof ConstNumber n) {
                            ALU mergedAdd = new ALU(new ConstNumber(imm - n.getVal()), "+", operand);
                            replaceInstrWith(aluUser, mergedAdd);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean subOptimize(ALU sub) {
        Value lOperand = sub.getOperand1();
        Value rOperand = sub.getOperand2();
        if (lOperand instanceof ConstNumber && rOperand instanceof ConstNumber) {
            return false;
        }
        //a-0=a
        if (rOperand instanceof ConstNumber) {
            Value operand;
            int imm;
            imm = ((ConstNumber) rOperand).getVal();
            operand = lOperand;


            if (imm == 0) {
                sub.beReplacedBy(operand);
                sub.getBlock().removeInstruction(sub);
                sub.destroy();
                return true;
            }

            //subi可以和使用它的addi或subi合并
            for (User user : sub.getUserList()) {
                if (user instanceof ALU aluUser) {
                    if (aluUser.getAluType().equals("add")) {
                        //sub des1,op,imm ; add des2,des1,n -> add des2,op,-imm1+n
                        if (aluUser.getOperand1() instanceof ConstNumber n) {
                            ALU mergedAdd = new ALU(new ConstNumber(-imm + n.getVal()), "+", operand);
                            replaceInstrWith(aluUser, mergedAdd);
                            return true;
                        } else if (aluUser.getOperand2() instanceof ConstNumber n) {
                            ALU mergedAdd = new ALU(new ConstNumber(-imm + n.getVal()), "+", operand);
                            replaceInstrWith(aluUser, mergedAdd);
                            return true;
                        }
                    } else if (aluUser.getAluType().equals("sub")) {
                        //sub des1,op,imm ; sub des2,des1,n -> sub des2,op,imm+n
                        //sub des1,op,imm ; sub des2,n,des1 -> sub des2,n+imm,op
                        if (aluUser.getOperand1() instanceof ConstNumber n) {
                            ALU mergedSub = new ALU(new ConstNumber(imm + n.getVal()), "-", operand);
                            replaceInstrWith(aluUser, mergedSub);
                            return true;
                        } else if (aluUser.getOperand2() instanceof ConstNumber n) {
                            ALU mergedSub = new ALU(operand, "-", new ConstNumber(imm + n.getVal()));
                            replaceInstrWith(aluUser, mergedSub);
                            return true;
                        }
                    }
                }
            }
        }

        if (lOperand instanceof ConstNumber) {
            Value operand;
            int imm;
            imm = ((ConstNumber) lOperand).getVal();
            operand = rOperand;

            //subi可以和使用它的addi或subi合并
            for (User user : sub.getUserList()) {
                if (user instanceof ALU aluUser) {
                    if (aluUser.getAluType().equals("add")) {
                        //sub des1,imm,op ; add des2,des1,n -> sub des2,imm+n,op
                        if (aluUser.getOperand1() instanceof ConstNumber n) {
                            ALU mergedAdd = new ALU(new ConstNumber(imm + n.getVal()), "-", operand);
                            replaceInstrWith(aluUser, mergedAdd);
                            return true;
                        } else if (aluUser.getOperand2() instanceof ConstNumber n) {
                            ALU mergedAdd = new ALU(new ConstNumber(imm + n.getVal()), "-", operand);
                            replaceInstrWith(aluUser, mergedAdd);
                            return true;
                        }
                    } else if (aluUser.getAluType().equals("sub")) {
                        //sub des1,imm,op ; sub des2,des1,n -> sub des2,imm-n,op
                        //sub des1,imm,op ; sub des2,n,des1 -> add des2,n-imm,op
                        if (aluUser.getOperand1() instanceof ConstNumber n) {
                            ALU mergedSub = new ALU(new ConstNumber(-imm + n.getVal()), "+", operand);
                            replaceInstrWith(aluUser, mergedSub);
                            return true;
                        } else if (aluUser.getOperand2() instanceof ConstNumber n) {
                            ALU mergedSub = new ALU(new ConstNumber(imm - n.getVal()), "-", operand);
                            replaceInstrWith(aluUser, mergedSub);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean divOptimize(ALU div) {
        Value lOperand = div.getOperand1();
        Value rOperand = div.getOperand2();
        if (lOperand instanceof ConstNumber && rOperand instanceof ConstNumber) {
            return false;
        }
        //0/a=0 ; a/1=a
        if (lOperand instanceof ConstNumber || rOperand instanceof ConstNumber) {
            Value operand;
            int imm;
            if (lOperand instanceof ConstNumber) {
                imm = ((ConstNumber) lOperand).getVal();
                operand = rOperand;
            } else {
                imm = ((ConstNumber) rOperand).getVal();
                operand = lOperand;
            }

            if (imm == 0 && lOperand instanceof ConstNumber) {
                div.beReplacedBy(new ConstNumber(0));
                div.getBlock().removeInstruction(div);
                div.destroy();
                return true;
            }
            if (imm == 1 && rOperand instanceof ConstNumber) {
                div.beReplacedBy(operand);
                div.getBlock().removeInstruction(div);
                div.destroy();
                return true;
            }
            if (imm == -1 && rOperand instanceof ConstNumber) {
                ALU neg = new ALU(new ConstNumber(0), "-", operand);
                int index = div.getBlock().getInstructionList().indexOf(div);
                div.beReplacedBy(neg);
                div.getBlock().removeInstruction(div);
                div.destroy();
                div.getBlock().addInstructionAt(index, neg);
                return true;
            }

            //取反
            boolean negative = imm < 0;
            if (imm < 0) {
                imm = -imm;
            }
            if (rOperand instanceof ConstNumber) {
                // 如果是2的幂次
                if (Integer.bitCount(imm) == 1) {
                    int size = Integer.toString(imm, 2).length();

                    Shift shift1 = new Shift(true, operand, new ConstNumber(31));
                    Shift shift2 = new Shift(true, shift1, new ConstNumber(33 - size));
                    shift2.setLogicalShiftRight(true);
                    ALU addu = new ALU(operand, "+", shift2);
                    Shift shift = new Shift(true, addu, new ConstNumber(size - 1));

                    Value finalInstr = shift;
                    int index = div.getBlock().getInstructionList().indexOf(div);

                    if (negative) {
                        ALU neg = new ALU(new ConstNumber(0), "-", finalInstr);
                        div.getBlock().addInstructionAt(index, neg);
                        finalInstr = neg;
                    }


                    div.beReplacedBy(finalInstr);
                    div.getBlock().removeInstruction(div);
                    div.destroy();
                    div.getBlock().addInstructionAt(index, shift);
                    div.getBlock().addInstructionAt(index, addu);
                    div.getBlock().addInstructionAt(index, shift2);
                    div.getBlock().addInstructionAt(index, shift1);
                    return true;
                }

                //乘法+移位优化

                long nc = ((long) 1 << 31) - (((long) 1 << 31) % imm) - 1;
                int l = 32;
                while (((long) 1 << l) <= nc * (imm - ((long) 1 << l) % imm)) {
                    l++;
                }
                long m = ((((long) 1 << l) + (long) imm - ((long) 1 << l) % imm) / (long) imm);
                l = l - 32;

                ArrayList<Instruction> newInstructions = new ArrayList<>();
                //计算xsign
                Shift signShift = new Shift(true, operand, new ConstNumber(31));
                signShift.setLogicalShiftRight(true);
                newInstructions.add(signShift);
                Instruction finalInstr;

                if (m < Integer.MAX_VALUE) {
                    //quotient = (signMulHi(dividend, multiplier) >> shift) - xsign(dividend);
                    ALU mult = new ALU(operand, "*", new ConstNumber((int) m));
                    mult.setMulFromHi(true);
                    newInstructions.add(mult);
                    Shift srl = new Shift(true, mult, new ConstNumber(l));
                    newInstructions.add(srl);
                    ALU fin = new ALU(srl, "+", signShift);
                    newInstructions.add(fin);
                    finalInstr = fin;
                } else {
                    //quotient = ((dividend + signMulHi(dividend, (sword) (multiplier - 0x80000000))) >> shift)
                    // - xsign(dividend);
                    long mSub32 = m - (1L << 32);
                    ALU mult = new ALU(operand, "*", new ConstNumber((int) mSub32));
                    mult.setMulFromHi(true);
                    newInstructions.add(mult);

                    ALU add = new ALU(operand, "+", mult);
                    newInstructions.add(add);
                    Shift srl = new Shift(true, add, new ConstNumber(l));
                    newInstructions.add(srl);
                    finalInstr = new ALU(srl, "+", signShift);
                    newInstructions.add(finalInstr);
                }
                //补符号位，因为C中负数是向上取整的，但移位不是
                if (negative) {
                    ALU neg = new ALU(new ConstNumber(0), "-", finalInstr);
                    newInstructions.add(neg);
                    finalInstr = neg;
                }

                int index = div.getBlock().getInstructionList().indexOf(div);
                div.beReplacedBy(finalInstr);
                div.getBlock().removeInstruction(div);
                div.destroy();
                for (int i = newInstructions.size() - 1; i >= 0; i--) {
                    div.getBlock().addInstructionAt(index, newInstructions.get(i));
                }
                return true;
            }
        }
        return false;
    }

    private boolean remOptimize(ALU rem) {
        Value lOperand = rem.getOperand1();
        Value rOperand = rem.getOperand2();
        if (lOperand instanceof ConstNumber && rOperand instanceof ConstNumber) {
            return false;
        }
        //0%a=0 ; a%+-1 = 0
        if (lOperand instanceof ConstNumber || rOperand instanceof ConstNumber) {
            Value operand;
            int imm;
            if (lOperand instanceof ConstNumber) {
                imm = ((ConstNumber) lOperand).getVal();
                operand = rOperand;
            } else {
                imm = ((ConstNumber) rOperand).getVal();
                operand = lOperand;
            }

            if (imm == 0 && lOperand instanceof ConstNumber) {
                rem.beReplacedBy(new ConstNumber(0));
                rem.getBlock().removeInstruction(rem);
                rem.destroy();
                return true;
            }
            if ((imm == 1 || imm == -1) && rOperand instanceof ConstNumber) {
                rem.beReplacedBy(new ConstNumber(0));
                rem.getBlock().removeInstruction(rem);
                rem.destroy();
                return true;
            }
        }

        if (rOperand instanceof ConstNumber) {
            //其他模常数的情况，转化为除法，即a%b = a-b*(a/b)，之后的通过再次遍历转为移位
            ALU div = new ALU(rem.getOperand1(), "/", rem.getOperand2());
            ALU mult = new ALU(div, "*", rem.getOperand2());
            ALU sub = new ALU(rem.getOperand1(), "-", mult);
            int index = rem.getBlock().getInstructionList().indexOf(rem);
            rem.beReplacedBy(sub);
            rem.getBlock().removeInstruction(rem);
            rem.destroy();
            rem.getBlock().addInstructionAt(index, sub);
            rem.getBlock().addInstructionAt(index, mult);
            rem.getBlock().addInstructionAt(index, div);
            return true;
        }
        return false;
    }

    public void replaceInstrWith(Instruction origin, Instruction newInstr) {
        int index = origin.getBlock().getInstructionList().indexOf(origin);
        origin.beReplacedBy(newInstr);
        origin.getBlock().removeInstruction(origin);
        origin.destroy();
        origin.getBlock().addInstructionAt(index, newInstr);
    }

}
