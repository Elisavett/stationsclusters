package net.codejava.Resolve.Clustering;

import net.codejava.Resolve.Model.Corr;
import net.codejava.Resolve.Model.Group;

import java.util.ArrayList;
import java.util.List;

public class GroupAllocation {
    double corrDOWN;
    double corrUP;
    List<List<Double>> corrTable;
    List<List<Integer>> groups;
    List<List<Double>> groupsCorrs;
    int stationCount;
    List<Corr> arrayCorr;

    public GroupAllocation(int stationCount, double corrDOWN, double corrUP, List<Corr> arrayCorr)
    {
        this();
        this.stationCount = stationCount;
        this.corrDOWN = corrDOWN;
        this.corrUP = corrUP;
        this.arrayCorr = arrayCorr;
    }
    public GroupAllocation(List<List<Integer>> groups, List<List<Double>> corrs) {
        this();
        this.groups = groups;
        this.groupsCorrs = corrs;
    }
    public GroupAllocation(){}

    public List<Group> clustersCalc() {
        loadCorrelationTable();
        allocateGroups();
        return saveGroups();
    }
    public List<Group> classesCalc() {
        return saveGroups();
    }

    private void loadCorrelationTable(){
        corrTable = new ArrayList<>();
        for (int i = 0; i < stationCount; i++) {
            Corr corr = arrayCorr.get(i);
            corrTable.add(corr.getCorrelationArray());
        }
    }

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
    private boolean CorrelationInLimits(int i, int j){
        return corrTable.get(i).get(Math.abs(j - i) - 1) >= corrDOWN && corrTable.get(i).get(Math.abs(j - i) - 1) <= corrUP;
    }

    private List<Group> saveGroups() {
        //создаем лист задач

        List<Group> returnGroups = new ArrayList<>();
        for (int i = 0; i < groups.size(); i++) {
            List<List<Double>> groupCorrTable = new ArrayList<>();
            for(int member1 : groups.get(i)){
                List<Double> memberCorrs = new ArrayList<>();
                for(int member2 : groups.get(i)){
                    if (member1 > member2) {
                        memberCorrs.add(Math.round(corrTable.get(member2).get(member1 - member2 - 1)*1000)/1000.0);
                    }
                    else if (member1 < member2){
                        memberCorrs.add(Math.round(corrTable.get(member1).get(member2 - member1 - 1)*1000)/1000.0);
                    }
                    else{
                        memberCorrs.add(1.);
                    }
                }
                groupCorrTable.add(memberCorrs);
            }
            returnGroups.add(new Group(groups.get(i), groupsCorrs.get(i), groupCorrTable, i));
        }
        return returnGroups;
    }
}

