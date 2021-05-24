package net.codejava.Resolve.PhaseCalc;
import net.codejava.Resolve.Clustering.CorrelationCalculation;
import net.codejava.Resolve.Model.Group;
import net.codejava.Resolve.Model.Phase;
import net.codejava.Resolve.Model.ResolveForm;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    public LinkedHashMap<Integer, Double> getFrequencySpector(){
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
    public static double getAvgTemp(double[] temp){
        double averageT = 0;
        for (double v : temp) {
            averageT += v;
        }
        return averageT / temp.length;
    }
    public static LinkedHashMap<String, Double> getTypicalTempsChart(List<Integer> group){
        LinkedHashMap<String, Double> temps = new LinkedHashMap<>();
        date.setTime(ResolveForm.startDate);
        Double[] typicals = getTypicalTemps(group);
        for (int j = 0; j < ResolveForm.TempData[0].length; j++) {
            date.add(dateDelta, 1);
            temps.put(dateFormat.format(date.getTime()), typicals[j]);
        }
        return temps;
    }
    public static Double[] getTypicalTemps(List<Integer> group){
        Double[] temps = new Double[ResolveForm.TempData[0].length];
        for (int j = 0; j < ResolveForm.TempData[0].length; j++) {
            double averageTemp = 0;
            for (Integer station : group) {
                averageTemp += ResolveForm.TempData[station][j];
            }
            temps[j] = Math.round(averageTemp/group.size()*1000)/1000.0;
        }
        return temps;
    }
    public static LinkedHashMap<String, Double> getTypicalPhase (Phase phase){
        LinkedHashMap<String, Double> chartData = new LinkedHashMap<>();
        date.setTime(ResolveForm.startDate);
        for(Double el : phase.getPhase()){
            date.add(dateDelta, 1);
            chartData.put(dateFormat.format(date.getTime()), Math.round(el*1000)/1000.0);
        }
        return chartData;
    }
    public static double getMax(Double[] values){
        double max = values[0];
        for (Double v : values) {
            if (v > max) max = v;
        }
        return max;
    }
    public static double getMin(Double[] values){
        double min = values[0];
        for (Double v : values) {
            if (v < min) min = v;
        }
        return min;
    }
    public static LinkedHashMap<String, Double> getClusterModel (Phase phase, List<Integer> group){
        LinkedHashMap<String, Double> chartData = new LinkedHashMap<>();
        List<Double> phaseMembs = phase.getPhase();
        int N = phaseMembs.size();

        List<Double> amplitude = new ArrayList<>(getTypicalAmplitudes(group).values());

        Double[] typicalTemps = getTypicalTemps(group);
        double sumT = 0;
        for (Double v : typicalTemps) {
            sumT += v;
        }
        double avgTypicalTemp = sumT / typicalTemps.length;
        double maxTypical = getMax(typicalTemps);
        double minTypical = Math.abs(getMin(typicalTemps));
        double maxAmpl = getMax(amplitude.toArray(new Double[N]));

        double coefficient = (maxTypical+minTypical)/(2*maxAmpl);

        date.setTime(ResolveForm.startDate);

        for(int i = 0; i < N; i++){
            date.add(dateDelta, 1);
            chartData.put(dateFormat.format(date.getTime()), avgTypicalTemp + coefficient * amplitude.get(i) * Math.cos(phaseMembs.get(i) + (2*Math.PI*i*ResolveForm.windowCenter/N) + Math.PI));
        }
        return chartData;
    }
    public static LinkedHashMap<String, Double> getTypicalAmplitudes (List<Integer> group){
        LinkedHashMap<String, Double> chartData = new LinkedHashMap<>();
        date.setTime(ResolveForm.startDate);
        for (int j = 0; j < ResolveForm.TempData[0].length; j++) {
            date.add(dateDelta, 1);
            double averageAmpl = 0;
            for (Integer station : group) {
                averageAmpl += ResolveForm.arrayAmplitude.get(station).getPhase().get(j);
            }
            chartData.put(dateFormat.format(date.getTime()), Math.round(averageAmpl/group.size()*1000)/1000.0);
        }
        return chartData;
    }
    //Таблица корреляции типовых фаз
    public static List<List<Double>> getGroupPhasesCorrTable() throws InterruptedException, ExecutionException {
        int processors = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(processors);
        List<CorrelationCalculation> corrThreadTasks = new ArrayList<>();
        List<Phase> groupTypicals = new ArrayList<>();
        for(Group g : ResolveForm.clusters){
            if(g.getGroupMembers().size() >= ResolveForm.minGroupSize) {
                groupTypicals.add(g.getPhases());
            }
        }
        for (int i = 0; i < groupTypicals.size(); i++) {
            CorrelationCalculation corrThread = new CorrelationCalculation(groupTypicals.get(i), i, groupTypicals);
            corrThreadTasks.add(corrThread);
        }
        //выполняем все задачи. главный поток ждет
        List<List<Double>> arrayCorr = ResolveForm.FutureToPlaneObj(executorService.invokeAll(corrThreadTasks));
        List<Double> missingElements = new ArrayList<>();
        arrayCorr.get(0).add(0, 0.);
        for(int i = 1; i < arrayCorr.size(); i++){
            for(int j = 0; j < i; j++){
                missingElements.add(arrayCorr.get(j).get(i));
            }
            missingElements.add(0.);
            arrayCorr.get(i).addAll(0, missingElements);
            missingElements.clear();
        }
        return arrayCorr;
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
            ResolveForm.averageTemps[i] = getAvgTemp(ResolveForm.TempData[i].clone());
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
            date.setTime(ResolveForm.startDate);
            graphData.put(0, 0.);
            for (int i = 1; i < real.length; i++) {
                date.add(dateDelta, 1);
                graphData.put(i, Math.round(100 * Math.sqrt(imag[i] * imag[i] + real[i] * real[i])) / 100.);
            }
            return graphData;
        }
    }
}
