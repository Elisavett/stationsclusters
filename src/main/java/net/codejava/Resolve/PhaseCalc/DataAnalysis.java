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

/*
    Класс для анализа данных
 */

public class DataAnalysis{

    FrequencyAnalysis frequencyAnalysis;
    private static int dateDelta = Calendar.MONTH;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    public DataAnalysis(double[] temp) {
        this();
        this.frequencyAnalysis = new FrequencyAnalysis(temp);
    }
    public DataAnalysis(){
        if(ResolveForm.dataType == 365){
            dateDelta = Calendar.DATE;
        }
    }

    //Частотный спектр
    public LinkedHashMap<Integer, Double> getFrequencySpector(){
        return frequencyAnalysis.spectorCalculation();
    }

    //Спектр по величине, по которой выполнялся рассчет (амплитуда / фаза)
    public static LinkedHashMap<Integer, Double> getCountableCharacterSpector(int groupNum){
        List<Double> character = ResolveForm.clusters.toArray(new Group[0])[groupNum].getPhases().getPhase();
        double[] typicalCharacter = new double[character.size()];
        for(int i = 0; i < character.size(); i++){
            typicalCharacter[i] = character.get(i);
        }
        FrequencyAnalysis frequencyAnalysis = new FrequencyAnalysis(typicalCharacter);

        return frequencyAnalysis.spectorCalculation();
    }

    //Спектр по величине, по которой не выполнялся рассчет (амплитуда / фаза)
    public static LinkedHashMap<Integer, Double> getSpecifiedCharacterSpector(List<Integer> group, List<Phase> specifiedChar){
        FrequencyAnalysis frequencyAnalysis = new FrequencyAnalysis(getTypicalForSpecified(group, specifiedChar));

        return frequencyAnalysis.spectorCalculation();
    }

    //Температура для станции
    public static LinkedHashMap<String, Double> getStationTemp(int station){
        Calendar date = Calendar.getInstance();
        double[] temp = ResolveForm.TempData[station].clone();
        LinkedHashMap<String, Double> temperatures = new LinkedHashMap<>();
        date.setTime(ResolveForm.startDate);
        for (double v : temp) {
            temperatures.put(dateFormat.format(date.getTime()), Math.round(100 * v) / 100.);
            date.add(dateDelta, 1);
        }
        return temperatures;
    }
    //Средняя температура
    public static double getAvgTemp(double[] temp){
        double averageT = 0;
        for (double v : temp) {
            averageT += v;
        }
        return averageT / temp.length;
    }

    //Типовая температура для графика
    public static LinkedHashMap<String, Double[]> getTypicalTempsChart(List<Integer> group){
        Calendar date = Calendar.getInstance();
        LinkedHashMap<String, Double[]> temps = new LinkedHashMap<>();
        date.setTime(ResolveForm.startDate);
        Double[][] typicals = getTypicalTemps(group);
        for (int j = 0; j < ResolveForm.TempData[0].length; j++) {
            temps.put(dateFormat.format(date.getTime()), new Double[]{typicals[0][j], typicals[1][j], typicals[2][j], typicals[3][j]});
            date.add(dateDelta, 1);}
        return temps;
    }

    //Рассчет типовой, средней, максимально, минимальной температур и СКО
    public static Double[][] getTypicalTemps(List<Integer> group){
        Double[] temps = new Double[ResolveForm.TempData[0].length];
        Double[] maxTemps = new Double[ResolveForm.TempData[0].length];
        Double[] minTemps = new Double[ResolveForm.TempData[0].length];
        Double[] skos = new Double[ResolveForm.TempData[0].length];
        for (int j = 0; j < ResolveForm.TempData[0].length; j++) {
            double averageTemp = 0;
            double max = ResolveForm.TempData[0][j];
            double min = ResolveForm.TempData[0][j];
            for (Integer station : group) {
                double element = ResolveForm.TempData[station][j];
                averageTemp += element;
                if(element > max) max = element;
                if(element < min) min = element;
            }
            temps[j] = Math.round(averageTemp/group.size()*1000)/1000.0;
            double sko_temp = 0;
            for (Integer station : group) {
                double element = ResolveForm.TempData[station][j];
                sko_temp += (element - temps[j])*(element - temps[j]);
            }
            skos[j] = Math.sqrt(sko_temp / (group.size() - 1));
            minTemps[j] = min;
            maxTemps[j] = max;
        }
        Double[][] tempMinMaxSko = new Double[4][];
        tempMinMaxSko[0] = temps;
        tempMinMaxSko[1] = minTemps;
        tempMinMaxSko[2] = maxTemps;
        tempMinMaxSko[3] = skos;
        return tempMinMaxSko;
    }

    //Типовая для величины, по которой выполнялся рассчет для графика
    public static LinkedHashMap<String, Double> getTypicalCountableCharacterChart (int groupNum){
        Calendar date = Calendar.getInstance();
        List<Double> character = ResolveForm.clusters.toArray(new Group[0])[groupNum].getPhases().getPhase();
        LinkedHashMap<String, Double> chartData = new LinkedHashMap<>();
        date.setTime(ResolveForm.startDate);
        for(Double el : character){
            chartData.put(dateFormat.format(date.getTime()), Math.round(el*1000)/1000.0);
            date.add(dateDelta, 1);
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

    //Модель для группы
    public static LinkedHashMap<String, Double> getClusterModel (Phase countableCharacter, List<Integer> group, double offset){
        Calendar date = Calendar.getInstance();
        LinkedHashMap<String, Double> chartData = new LinkedHashMap<>();
        List<Double> phase;
        List<Double> amplitude;
        if(ResolveForm.isForPhases){
            phase = countableCharacter.getPhase();
            amplitude = new ArrayList<>(getTypicalSpecifiedsForChart(group, ResolveForm.arrayAmplitude).values());
        }
        else{
            phase = new ArrayList<>(getTypicalSpecifiedsForChart(group, ResolveForm.arrayPhase).values());
            amplitude = countableCharacter.getPhase();
        }
        int N = phase.size();


        Double[] typicalTemps = getTypicalTemps(group)[0];
        double sumT = 0;
        for (Double v : typicalTemps) {
            sumT += v;
        }
        double avgTypicalTemp = sumT / typicalTemps.length;
        double maxTypical = getMax(typicalTemps);
        double minTypical = getMin(typicalTemps);
        double maxAmpl = getMax(amplitude.toArray(new Double[N]));

        double coefficient = (maxTypical-minTypical)/(2*maxAmpl);

        date.setTime(ResolveForm.startDate);

        for(int i = 0; i < N; i++){
            chartData.put(dateFormat.format(date.getTime()), avgTypicalTemp + coefficient * amplitude.get(i) * Math.cos(phase.get(i) + (2*Math.PI*i*ResolveForm.windowCenter/N) + Math.PI + offset*Math.PI/4));
            date.add(dateDelta, 1);
        }
        return chartData;
    }

    //Типовая для величины, по которой НЕ выполнялся рассчет, для графика
    public static LinkedHashMap<String, Double> getTypicalSpecifiedsForChart(List<Integer> group, List<Phase> specifiedCharacters){
        Calendar date = Calendar.getInstance();
        LinkedHashMap<String, Double> chartData = new LinkedHashMap<>();
        date.setTime(ResolveForm.startDate);
        double[] amplitude = getTypicalForSpecified(group, specifiedCharacters);
        for (int j = 0; j < ResolveForm.TempData[0].length; j++) {
            chartData.put(dateFormat.format(date.getTime()), amplitude[j]);
            date.add(dateDelta, 1);
        }
        return chartData;
    }

    //Типовые амплитуды
    public static double[] getTypicalAmplitudes(List<Integer> group){
        return getTypicalForSpecified(group, ResolveForm.arrayAmplitude);
    }
    public static double[] getTypicalForSpecified(List<Integer> group, List<Phase> specifiedCharacters){
        double[] charecter = new double[ResolveForm.TempData[0].length];
        for (int j = 0; j < ResolveForm.TempData[0].length; j++) {
            double averageCharacter = 0;
            for (Integer station : group) {
                averageCharacter += specifiedCharacters.get(station).getPhase().get(j);
            }
            charecter[j] = Math.round(averageCharacter/group.size()*1000)/1000.0;
        }
        return charecter;
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
        List<List<Double>> arrayCorr = ResolveForm.FutureToPlaneObj(executorService.invokeAll(corrThreadTasks));
        List<Double> missingElements = new ArrayList<>();
        arrayCorr.get(0).add(0, 1.);
        double maxCorr = arrayCorr.get(0).get(1);
        double minCorr = arrayCorr.get(0).get(1);
        for(int i = 1; i < arrayCorr.size(); i++){
            for(int j = 0; j < i; j++){
                missingElements.add(arrayCorr.get(j).get(i));
                if(arrayCorr.get(j).get(i) > maxCorr) maxCorr = arrayCorr.get(j).get(i);
                if(arrayCorr.get(j).get(i) < minCorr) minCorr = arrayCorr.get(j).get(i);
            }
            missingElements.add(1.);
            arrayCorr.get(i).addAll(0, missingElements);
            missingElements.clear();
        }
        ResolveForm.maxSystemCorr = maxCorr;
        ResolveForm.minSystemCorr = minCorr;
        return arrayCorr;
    }

    //СКО для станции
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

    //Класс частотного анализа
    private static class FrequencyAnalysis extends PhaseCalculationAbstract {

        public FrequencyAnalysis(double[] temp) {

            this.temp = temp;
        }

        public LinkedHashMap<Integer, Double> spectorCalculation() {
            LoadFunction();
            FFTCalculation();
            LinkedHashMap<Integer, Double> graphData = new LinkedHashMap<>();
            graphData.put(0, 0.);
            for (int i = 1; i < real.length; i++) {
                graphData.put(i, Math.round(100 * Math.sqrt(imag[i] * imag[i] + real[i] * real[i])) / 100.);
            }
            return graphData;
        }
    }
}
