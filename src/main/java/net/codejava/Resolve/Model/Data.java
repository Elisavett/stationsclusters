package net.codejava.Resolve.Model;

public abstract class Data {
    private final double[] data;

    public Data(double[] data) {
        this.data = data;
    }

    public Data() {
        this.data = null;
    }
}
