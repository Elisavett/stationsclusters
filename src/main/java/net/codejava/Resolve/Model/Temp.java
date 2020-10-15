package net.codejava.Resolve.Model;

import java.io.Serializable;

public class Temp extends Data implements Serializable {
    private final double[] data;

    public Temp(double[] temp) {
        this.data = temp;
    }

    public double[] getArray() {
        return data;
    }
}
