package net.codejava.Resolve.Model;

import java.io.Serializable;

public class Group extends Data implements Serializable {
    private final int[] data;
    private final double[] corrs;

    public Group(int[] data, double[] corrs) {
        this.data = data;
        this.corrs = corrs;
    }

    public int[] getArray() {
        return data;
    }

    public double[] getCorrs() {
        return corrs;
    }
}
