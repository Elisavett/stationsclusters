package net.codejava.Resolve.Model;

import java.io.Serializable;

public class Corr extends Data implements Serializable {
    private final double[] data;

    public Corr(double[] corr) {
        this.data = corr;
    }

    public double[] getArray() {
        return data;
    }
}
