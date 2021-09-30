package net.codejava.Resolve.Model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/*
    Класс, представляющий группу
 */

@Getter
public class Group implements Comparable<Group> {
    private final List<Integer> groupMembers;
    private final List<Double> correlations;
    @Setter private List<List<Double>> groupCorrTable;
    @Setter private double maxGroupCorr;
    @Setter private double minGroupCorr;
    @Setter private Phase phases;
    private final int index;

    //Передаются члены группы, таблица корреляции членов, индекс в общей таблице
    public Group(List<Integer> groupMembers, List<Double> cors, int index) {
        this.groupMembers = groupMembers;
        this.correlations = cors;
        this.index = index;
    }

    //Проверка равенства групп по её членам
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Group) {
            return groupMembers.equals(((Group) obj).getGroupMembers());
        }
        else return false;
    }

    //Удаление члена из группы по индексу
    public void removeIdx(int i){
        groupMembers.remove(i);
        correlations.remove(i);
    }

    //ПОлучение корреляции по индексу
    public double getCorrsForIdx(int idx) {
        return correlations.get(idx);
    }

    //Удалить дублирования групп
    public void deleteDoubles(List<Group> allStations){
        for (Group tempGroup : allStations) {
            List<Integer> currGroup = new ArrayList<>(tempGroup.getGroupMembers());
            if (index != tempGroup.index) {
                for (int i = 0; i < groupMembers.size(); i++) {
                    int idx = currGroup.indexOf(groupMembers.get(i));
                    if (idx != -1) {
                        if (tempGroup.getCorrsForIdx(idx) >= correlations.get(i)) {
                            removeIdx(i);
                        } else {
                            currGroup.remove(idx);
                            tempGroup.removeIdx(idx);
                        }
                    }
                }
            }
        }
    }

    //Преобразование группы в строку
    public String getGroupString(){
        StringBuilder stringClusters = new StringBuilder();
        for (double station : getGroupMembers()) {
            stringClusters.append((int)station).append(" ");
        }
        stringClusters.append("\n");
        return stringClusters.toString();
    }

    //Преобразование фазы в строку
    public String getPhaseString(){
        StringBuilder stringPhase = new StringBuilder();
        for (double phase : getPhases().getPhase()) {
            stringPhase.append(Math.round(phase*1000)/1000.0).append(" ");
        }
        stringPhase.append("\n");
        return stringPhase.toString();
    }
    //Сравнение групп по количеству членов
    @Override
    public int compareTo(Group anotherGroupLine) {
        if(this.groupMembers.size() < anotherGroupLine.getGroupMembers().size()) return 1;
        if(this.groupMembers.equals(anotherGroupLine.getGroupMembers())) return 0;
        return -1;
    }
}
