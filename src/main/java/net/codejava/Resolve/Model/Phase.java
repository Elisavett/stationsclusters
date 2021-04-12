package net.codejava.Resolve.Model;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class Phase{
    @Getter private final List<Double> phase;

    public Phase(List<Double> phase) {
        this.phase = phase;
    }
    public Phase(double[] phase){
        this.phase = new ArrayList<>();
        for(double phasePart : phase){
            this.phase.add(phasePart);
        }
    }
}
