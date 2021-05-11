package net.codejava.Resolve.PhaseCalc;
import net.codejava.Resolve.Model.ResolveForm;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

public class DataAnalysis{

    FrequencyAnalysis frequencyAnalysis;
    private static int dateDelta = Calendar.MONTH;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static final Calendar date = Calendar.getInstance();
    public static Date dateForChart (){
        date.setTime(ResolveForm.startDate);
        date.add(dateDelta, ResolveForm.TempData[0].length/3);
        return date.getTime();
    }
    public DataAnalysis(double[] temp) {
        this();
        this.frequencyAnalysis = new FrequencyAnalysis(temp);
    }
    public DataAnalysis(){
        if(ResolveForm.dataType == 365){
            dateDelta = Calendar.DATE;
        }
    }
    public LinkedHashMap<String, Double> getFrequencySpector(){
        return frequencyAnalysis.spectorCalculation();
    }
    public static LinkedHashMap<String, Double> getStationTemp(int station){
        double[] temp = ResolveForm.TempData[station].clone();
        LinkedHashMap<String, Double> temperatures = new LinkedHashMap<>();
        date.setTime(ResolveForm.startDate);
        for (double v : temp) {
            date.add(dateDelta, 1);
            temperatures.put(dateFormat.format(date.getTime()), Math.round(100 * v) / 100.);
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
    public static LinkedHashMap<String, Double> getTypicalTemps(List<Integer> group){
        LinkedHashMap<String, Double> temps = new LinkedHashMap<>();
        date.setTime(ResolveForm.startDate);
        for (int j = 0; j < ResolveForm.TempData[0].length; j++) {
            double averageTemp = 0;
            for (Integer station : group) {
                averageTemp += ResolveForm.TempData[station][j];
            }
            date.add(dateDelta, 1);
            temps.put(dateFormat.format(date.getTime()), Math.round(averageTemp/group.size()*1000)/1000.0);
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

        public LinkedHashMap<String, Double> spectorCalculation() {
            LoadFunction();
            FFTCalculation();
            LinkedHashMap<String, Double> graphData = new LinkedHashMap<>();
            date.setTime(ResolveForm.startDate);
            graphData.put(dateFormat.format(date.getTime()), 0.);
            for (int i = 1; i < real.length; i++) {
                date.add(dateDelta, 1);
                graphData.put(dateFormat.format(date.getTime()), Math.round(100 * Math.sqrt(imag[i] * imag[i] + real[i] * real[i])) / 100.);
            }
            return graphData;
        }
    }
}
