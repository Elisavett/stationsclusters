package net.codejava.Resolve.Model;

import lombok.Getter;

import java.util.List;

/*
    Класс корреляции
 */

public class Corr{
    @Getter private final List<Double> correlationArray;

    public Corr(List<Double> corr) {
        this.correlationArray = corr;
    }
}
