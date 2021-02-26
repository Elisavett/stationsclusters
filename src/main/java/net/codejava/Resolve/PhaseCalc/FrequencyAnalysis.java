package net.codejava.Resolve.PhaseCalc;

import net.codejava.Resolve.Model.Phase;

import java.util.LinkedHashMap;
import java.util.concurrent.Callable;

public class FrequencyAnalysis extends PhaseCalculationAbstract {

    public FrequencyAnalysis(double[] temp){
        this.temp = temp;
    }
    public LinkedHashMap<Integer, Double> spectorCalculation(){
        LoadFunction();
        FFTCalculation();
        LinkedHashMap<Integer, Double> graphData = new LinkedHashMap<>();
        graphData.put(1, 0.);
        for (int i = 1; i < real.length; i++) {
            graphData.put(i+1, Math.round(100*Math.sqrt(imag[i]*imag[i] + real[i]*real[i]))/100.);
        }
        return graphData;
    }
}
