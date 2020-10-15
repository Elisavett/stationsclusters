package net.codejava.Resolve.Model;

import java.io.Serializable;

public class TypicalPhase extends Data implements Serializable {
    private final double[] data;

    public TypicalPhase(double[] corr) {
        this.data = corr;
    }

    public double[] getArray() {
        return data;
    }
}
