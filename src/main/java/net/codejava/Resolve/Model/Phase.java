package net.codejava.Resolve.Model;

import java.io.Serializable;

public class Phase extends Data implements Serializable {
    private final double[] data;

    public Phase(double[] phase) {
        this.data = phase;
    }

    public double[] getArray() {
        return data;
    }
}
