package net.codejava.Resolve.PhaseCalc;
import net.codejava.Resolve.Model.ResolveForm;

import java.util.LinkedHashMap;

public class DataAnalysis{

    FrequencyAnalysis frequencyAnalysis;
    public DataAnalysis(double[] temp) {
        this.frequencyAnalysis = new FrequencyAnalysis(temp);
    }
    public LinkedHashMap<Integer, Double> getFrequencySpector(){
        return frequencyAnalysis.spectorCalculation();
    }
    public static LinkedHashMap<String, Double> getStationTemp(int station){
        double[] temp = ResolveForm.TempData[station].clone();
        LinkedHashMap<String, Double> temperatures = new LinkedHashMap<>();
        for(int i = 0; i < temp.length; i++)
        {
            temperatures.put(String.valueOf(i), Math.round(100*temp[i])/100.);
        }
        return temperatures;
    }
    public static double getStationAvgTemp(int station){
        double[] temp = ResolveForm.TempData[station].clone();
        double averageT = 0;
        for(int i = 0; i < temp.length; i++)
        {
            averageT += temp[i];
        }
        return averageT / temp.length;
    }
    public static double getStationSKO(double averageT, int station){
        double[] temp = ResolveForm.TempData[station].clone();
        double sko_temp = 0;
        for (double v : temp) {
            sko_temp += (v - averageT)*(v - averageT);
        }
        return Math.sqrt(sko_temp / (temp.length - 1));
    }
    public static LinkedHashMap<Integer, Double> getAllSKO(){
        ResolveForm.averageTemps = new double[ResolveForm.TempData.length];
        for(int i = 0; i < ResolveForm.TempData.length; i++)
        {
            ResolveForm.averageTemps[i] = getStationAvgTemp(i);
        }
        LinkedHashMap<Integer, Double> SKO = new LinkedHashMap<>();
        for(int i = 0; i < ResolveForm.averageTemps.length; i++)
        {
            SKO.put(i+1, getStationSKO(ResolveForm.averageTemps[i], i));
        }
        return SKO;
    }

    private static class FrequencyAnalysis extends PhaseCalculationAbstract {

        public FrequencyAnalysis(double[] temp) {

            this.temp = temp;
        }

        public LinkedHashMap<Integer, Double> spectorCalculation() {
            LoadFunction();
            FFTCalculation();
            LinkedHashMap<Integer, Double> graphData = new LinkedHashMap<>();
            graphData.put(1, 0.);
            for (int i = 1; i < real.length; i++) {
                graphData.put(i + 1, Math.round(100 * Math.sqrt(imag[i] * imag[i] + real[i] * real[i])) / 100.);
            }
            return graphData;
        }
    }
}
