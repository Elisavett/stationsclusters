package net.codejava.Resolve.Clustering;

import net.codejava.Resolve.Model.Phase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;


/**
 * Класс служит для расчета корреляции очередной фазы с остальными фазами
 */
public class CorrelationCalculation implements Callable<List<Double>> {
    private final int firstIndex;
    private final List<Double> correlation;
    private final List<Double> firstStation;
    private List<Double> secondStation;
    private final Phase[] arrayPhase;

    public CorrelationCalculation(Phase station, int firstIndex, List<Phase> arrayPhase) {
        this.firstStation = station.getPhase();
        this.firstIndex = firstIndex;
        this.correlation = new ArrayList<>();
        this.arrayPhase = new Phase[arrayPhase.size()];
        //Из листа в массив
        for (int i = 0; i < arrayPhase.size(); i++){
            this.arrayPhase[i] = arrayPhase.get(i);
        }
    }

    @Override
    public List<Double> call(){
        //В цикле выбирается фаза для рассчета и выполняется рассчет корреляции
        for (int i = firstIndex + 1; i < arrayPhase.length; i++) {
            secondStation = loadStantion(i);
            correlation.add(correlationCalc());
        }
        return new ArrayList<>(correlation);
    }

    //Получить фазу станции
    private List<Double> loadStantion(int index){
        return arrayPhase[index].getPhase();
    }

    //расчет взаимной корреляции между двумя хронологическими рядами
    private double correlationCalc() {
        double average1 = 0, average2 = 0;
        int num = firstStation.size();
        for (int i = 0; i < num; i++) {
            average1 += firstStation.get(i);
            average2 += secondStation.get(i);
        }
        if(average1 == 0 || average2 == 0)
            return 0;
        average1 = average1 / num;
        average2 = average2 / num;

        double part1 = 0;
        double part2 = 0;
        double part3 = 0;
        for (int i = 0; i < num; i++) {
            double dif1 = firstStation.get(i) - average1;
            double dif2 = secondStation.get(i) - average2;
            part1 += dif1 * dif2;
            part2 += dif1 * dif1;
            part3 += dif2 * dif2;
        }
        double part4 = Math.sqrt(part2 * part3);
        return Math.round(part1 / part4*1000)/1000.0;
    }



}
