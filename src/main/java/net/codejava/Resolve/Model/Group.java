package net.codejava.Resolve.Model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

@Getter
public class Group implements Comparable<Group> {
    private final List<Integer> groupMembers;
    private final List<Double> correlations;
    @Setter private Phase phases;
    private final int index;

    public Group(List<Integer> groupMembers, List<Double> cors, int index) {
        this.groupMembers = groupMembers;
        this.correlations = cors;
        this.index = index;
    }
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Group) {
            return groupMembers.equals(((Group) obj).getGroupMembers());
        }
        else return false;
    }
    public void removeIdx(int i){
        groupMembers.remove(i);
        correlations.remove(i);
    }

    public double getCorrsForIdx(int idx) {
        return correlations.get(idx);
    }

    public void deleteDoubles(TreeSet<Group> allStations){
        for (Group tempGroup : allStations) {
            List<Integer> currGroup = new ArrayList<>(tempGroup.getGroupMembers());
            if (index != tempGroup.index) {
                for (int i = 0; i < groupMembers.size(); i++) {
                    int idx = currGroup.indexOf(groupMembers.get(i));
                    if (idx != -1) {
                        if (tempGroup.getCorrsForIdx(idx) > correlations.get(i)) {
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
/*
    @Override
    public int compareTo(Group anotherGroupLine) {
        return Integer.compare(this.getGroupMembers().get(0), anotherGroupLine.getGroupMembers().get(0));
    }
    */
    public String getGroupString(){
        StringBuilder stringClusters = new StringBuilder();
        for (double station : getGroupMembers()) {
            stringClusters.append((int)station).append(" ");
        }
        stringClusters.append("\n");
        return stringClusters.toString();
    }
    public String getCorrelationString(){
        StringBuilder stringPhase = new StringBuilder();
        for (double phase : getPhases().getPhase()) {
            stringPhase.append(Math.round(phase*1000)/1000.0).append(" ");
        }
        stringPhase.append("\n");
        return stringPhase.toString();
    }
    @Override
    public int compareTo(Group anotherGroupLine) {
        if(this.groupMembers.size() > anotherGroupLine.getGroupMembers().size()) return -1;
        if(this.groupMembers.equals(anotherGroupLine.getGroupMembers())) return 0;
        return 1;
    }
}
