package net.codejava.Resolve.Model;

import java.io.Serializable;

public class Group extends Data implements Serializable {
    private final int[] data;

    public Group(int[] data) {
        this.data = data;
    }

    public int[] getArray() {
        return data;
    }
}
