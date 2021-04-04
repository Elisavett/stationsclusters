package net.codejava.Resolve.Clustering;

import net.codejava.Resolve.Model.Corr;
import net.codejava.Resolve.Model.Phase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


/**
 * Класс служит для расчета корреляции
 *
 * @version 1.0
 */
public class CorrelationCalculation implements Callable<Corr> {
    private final int firstIndex;
    private final double[] correlation;
    private double[] firstStation;
    private double[] secondStation;
    private Phase[] arrayPhase;

    @Override
    public Corr call() throws Exception {
        int counter = 0;
        for (int i = firstIndex + 1; i < arrayPhase.length; i++) {
            secondStation = loadStantion(i);
            correlation[counter] = correlationCalc();
            counter++;
        }
        Corr corr = new Corr(correlation);
        return corr;
    }

    public CorrelationCalculation(double[] station, int firstIndex, int totalNumber, List<Future<Phase>> arrayPhase) throws ExecutionException, InterruptedException {
        this.firstStation = station;
        this.firstIndex = firstIndex;
        this.correlation = new double[arrayPhase.size() - firstIndex - 1];
        this.arrayPhase = new Phase[arrayPhase.size()];
        for (int i = 0; i < arrayPhase.size(); i++){
            this.arrayPhase[i] = arrayPhase.get(i).get();
        }

    }

    public CorrelationCalculation(double[] station, int firstIndex, int totalNumber, Phase[] arrayPhase) throws ExecutionException, InterruptedException {
        this.firstStation = station;
        this.firstIndex = firstIndex;
        this.correlation = new double[arrayPhase.length - firstIndex - 1];
        this.arrayPhase = arrayPhase;
    }


    private double[] loadStantion(int index) throws ExecutionException, InterruptedException {
        return arrayPhase[index].getArray();
    }
//расчет взаимной корреляции между двумя хронологическими рядами
    private double correlationCalc() {
        double average1 = 0, average2 = 0;
        int num = firstStation.length;
        for (int i = 0; i < num; i++) {
            average1 += firstStation[i];
            average2 += secondStation[i];
        }
        if(average1 == 0 || average2 == 0)
            return 0;
        average1 = average1 / num;
        average2 = average2 / num;

        double part1 = 0;
        double part2 = 0;
        double part3 = 0;
        for (int i = 0; i < num; i++) {
            double dif1 = firstStation[i] - average1;
            double dif2 = secondStation[i] - average2;
            part1 += dif1 * dif2;
            part2 += dif1 * dif1;
            part3 += dif2 * dif2;
        }
        double part4 = Math.sqrt(part2 * part3);
        return part1 / part4;
    }



}
