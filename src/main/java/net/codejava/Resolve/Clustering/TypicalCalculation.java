package net.codejava.Resolve.Clustering;


import net.codejava.Resolve.Model.Group;
import net.codejava.Resolve.Model.Phase;
import net.codejava.Resolve.Model.TypicalPhase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class TypicalCalculation {
    ArrayList<ArrayList<Double>> phases = new ArrayList<>();
    ArrayList<Double> typical = new ArrayList<>();
    ArrayList<Integer> members = new ArrayList<>();
    int stationCount;
    List<Future<Phase>> arrayPhase;
    Group groupIndex;

    public TypicalCalculation(int stationCount, List<Future<Phase>> arrayPhase, Group groupIndex) {
        this.stationCount = stationCount;
        this.arrayPhase = arrayPhase;
        this.groupIndex = groupIndex;
    }

    public TypicalPhase run() throws ExecutionException, InterruptedException {
        loadGroup();
        loadPhase();
        calcTypical();
        return saveTypical();
    }

    private void loadGroup() {
        for (int i = 0; i < groupIndex.getArray().length; i++) {
            members.add(groupIndex.getArray()[i]);
        }
    }

    //Добавляет для каждой станции из группы members фазу, соответствующую этой станции
    private void loadPhase() throws ExecutionException, InterruptedException {
        int counter = 0;
        for (Integer member : members) {
            Phase phase = arrayPhase.get(member).get();
            phase.getArray();
            phases.add(new ArrayList<>());
            for (int i = 0; i < phase.getArray().length; i++) {
                phases.get(counter).add(phase.getArray()[i]);
            }
            counter++;
        }
    }

    private void calcTypical() {
        //Запись первого значения фаз в массив типовых
        typical.addAll(phases.get(0));
        //цикл, который идет до количества членов в группе
        for (int i = 1; i < members.size(); i++) {
            //Цикл до количества фаз каждой стации
            for (int j = 0; j < phases.get(i).size(); j++) {
                typical.set(j, typical.get(j) + phases.get(i).get(j));
            }
        }
        for (int i = 0; i < typical.size(); i++) {
            typical.set(i, typical.get(i) / members.size());
        }
    }

    private TypicalPhase saveTypical() {
        double[] typicalArr = new double[typical.size()];
        for (int i = 0; i < typical.size(); i++) {
            typicalArr[i] = typical.get(i);
        }
        return new TypicalPhase(typicalArr);
    }
}

