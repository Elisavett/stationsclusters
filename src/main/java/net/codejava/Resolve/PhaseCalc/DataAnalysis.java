package net.codejava.Resolve.PhaseCalc;
import net.codejava.Resolve.Model.ResolveForm;

import java.util.LinkedHashMap;
import java.util.List;

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
    public static double getStationAvgTemp(double[] temp){
        double averageT = 0;
        for (double v : temp) {
            averageT += v;
        }
        return averageT / temp.length;
    }
    public static LinkedHashMap<Integer, Double> getTypicalTemps(List<Integer> group){
        LinkedHashMap<Integer, Double> temps = new LinkedHashMap<>();
        for (int j = 0; j < ResolveForm.TempData[0].length; j++) {
            double averageTemp = 0;
            for (Integer station : group) {
                averageTemp += ResolveForm.TempData[station][j];
            }
            temps.put(j, Math.round(averageTemp/group.size()*1000)/1000.0);
        }
        return temps;
    }
    public static double getStationSKO(double averageT, double[] temp){
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
            ResolveForm.averageTemps[i] = getStationAvgTemp(ResolveForm.TempData[i].clone());
        }
        LinkedHashMap<Integer, Double> SKO = new LinkedHashMap<>();
        for(int i = 0; i < ResolveForm.averageTemps.length; i++)
        {
            SKO.put(i+1, getStationSKO(ResolveForm.averageTemps[i], ResolveForm.TempData[i]));
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
