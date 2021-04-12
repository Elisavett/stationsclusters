package net.codejava.Resolve.Model;

import lombok.Getter;

import java.util.List;

public class Corr{
    @Getter private final List<Double> correlationArray;

    public Corr(List<Double> corr) {
        this.correlationArray = corr;
    }
}
