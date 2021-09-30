package net.codejava.Resolve.Clustering;

import net.codejava.Resolve.Model.Group;

import java.util.ArrayList;
import java.util.List;

/*
    Класс для выделения групп
 */
public class GroupAllocation {
    double corrDOWN;
    double corrUP;
    List<List<Double>> corrTable;
    List<List<Integer>> groups;
    List<List<Double>> groupsCorrs;
    int stationCount;

    //Кластеризация - группы вычисляем
    public GroupAllocation(int stationCount, double corrDOWN, double corrUP, List<List<Double>> arrayCorr)
    {
        this();
        this.stationCount = stationCount;
        this.corrDOWN = corrDOWN;
        this.corrUP = corrUP;
        this.corrTable = arrayCorr;
    }
    //Классификация - знаем группы
    public GroupAllocation(List<List<Integer>> groups, List<List<Double>> corrs) {
        this();
        this.groups = groups;
        this.groupsCorrs = corrs;
    }
    public GroupAllocation(){}

    //Группы для кластеризации
    public List<Group> clustersCalc() {
        allocateGroups();
        return saveGroups();
    }

    //Группы для классификации
    public List<Group> classesCalc() {
        return saveGroups();
    }

    //Выделение групп
    private void allocateGroups() {
        groups = new ArrayList<>();
        groupsCorrs = new ArrayList<>();
        try {
            for (int i = 0; i < corrTable.size(); i++) {
                groups.add(new ArrayList<>());
                groupsCorrs.add(new ArrayList<>());
                for (int j = 0; j < corrTable.size(); j++) {
                    if (i == j) {
                        groups.get(i).add(j);
                        groupsCorrs.get(i).add(0.0);
                    }
                    else {
                        if (j > i) {
                            if (CorrelationInLimits(i, j)) {
                                groups.get(i).add(j);
                                groupsCorrs.get(i).add(corrTable.get(i).get(j - i - 1));
                            }
                        } else {
                            if (CorrelationInLimits(j, i)) {
                                groups.get(i).add(j);
                                groupsCorrs.get(i).add(corrTable.get(j).get(i - j - 1));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
    //Проверка попадает ли корреляция в заданные границы
    private boolean CorrelationInLimits(int i, int j){
        return corrTable.get(i).get(Math.abs(j - i) - 1) >= corrDOWN && corrTable.get(i).get(Math.abs(j - i) - 1) <= corrUP;
    }

    //Преобразование массива групп в класс групп
    private List<Group> saveGroups() {
        List<Group> returnGroups = new ArrayList<>();
        for (int i = 0; i < groups.size(); i++) {

            returnGroups.add(new Group(groups.get(i), groupsCorrs.get(i), i));
        }
        return returnGroups;
    }

}

