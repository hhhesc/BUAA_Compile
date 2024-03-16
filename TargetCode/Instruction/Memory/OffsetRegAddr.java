package TargetCode.Instruction.Memory;

import TargetCode.Register;

import java.util.Objects;

public class OffsetRegAddr {
    private final Integer offset;
    private final String label;

    private final Register reg;

    public OffsetRegAddr(int offset, Register reg) {
        this.offset = offset;
        label = null;
        this.reg = reg;
    }

    public OffsetRegAddr(String label, Register reg) {
        offset = null;
        this.label = label;
        this.reg = reg;
    }

    public String toString() {
        return Objects.requireNonNullElse(label, offset) + "(" + reg + ")";
    }

    public String getLabel() {
        return label;
    }

    public Integer getOffset() {
        return offset;
    }

    public Register getReg() {
        return reg;
    }

    public boolean equals(Object o) {
        if (!(o instanceof OffsetRegAddr oAddr)) {
            return false;
        }
        if (label == null) {
            return reg.equals(oAddr.getReg()) && offset.equals(oAddr.getOffset());
        } else {
            return reg.equals(oAddr.getReg()) && label.equals(oAddr.getLabel());
        }
    }

    public int hashCode() {
        return toString().hashCode();
    }
}
