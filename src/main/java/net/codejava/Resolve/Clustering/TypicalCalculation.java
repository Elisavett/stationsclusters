package net.codejava.Resolve.Clustering;


import net.codejava.Resolve.Model.Group;
import net.codejava.Resolve.Model.Phase;

import java.util.ArrayList;
import java.util.List;

/*
    Класс для рассчета типовой
 */

public class TypicalCalculation {
    ArrayList<ArrayList<Double>> phases = new ArrayList<>();
    ArrayList<Double> typical = new ArrayList<>();
    List<Integer> members = new ArrayList<>();
    int stationCount;
    List<Phase> arrayPhase;
    Group group;

    public TypicalCalculation(int stationCount, List<Phase> arrayPhase, Group group) {
        this.stationCount = stationCount;
        this.arrayPhase = arrayPhase;
        this.group = group;
    }

    public Phase run() {
        loadGroup();
        loadPhase();
        calcTypical();
        return saveTypical();
    }

    //Получение членов группы
    private void loadGroup() {
        members.addAll(group.getGroupMembers());
    }

    //Добавляет для каждой станции из группы members фазу, соответствующую этой станции
    private void loadPhase() {
        int counter = 0;
        for (Integer member : members) {
            List<Double> phase = arrayPhase.get(member).getPhase();
            phases.add(new ArrayList<>());
            for (Double aDouble : phase) {
                phases.get(counter).add(aDouble);
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

    private Phase saveTypical() {
        return new Phase(new ArrayList<>(typical));
    }
}

