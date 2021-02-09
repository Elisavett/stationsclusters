package net.codejava.Resolve.Model;

import java.util.*;
import java.util.stream.IntStream;

public class GroupLine implements Comparable<GroupLine> {
    private int[] group;
    private double[] corrs;
    private boolean isLess5 = false;
    private final int index;


    public GroupLine(int[] group, double[] corrs, int index){
        this.group = group;
        this.corrs = corrs;
        this.index = index;
    }

    public ArrayList<Integer> getGroup() {
        ArrayList<Integer> al = new ArrayList<Integer>();
        for(int i = 0; i < group.length; i++)
        {
            al.add(group[i]);
        }
        return al;
    }
    public ArrayList<Double> getCorr() {
        ArrayList<Double> al = new ArrayList<Double>();
        for(int i = 0; i < corrs.length; i++)
        {
            al.add(corrs[i]);
        }
        return al;
    }
    public void removeIdx(int i){
        int[] result = new int[group.length - 1]; // Array which will contain the result.
        System.arraycopy(group, 0, result, 0, i);
        System.arraycopy(group, i + 1, result, i, group.length - i - 1);
        group = new int[group.length - 1];
        group = result.clone();

        double[] result2 = new double[corrs.length - 1];
        System.arraycopy(corrs, 0, result2, 0, i);
        System.arraycopy(corrs, i + 1, result2, i, corrs.length - i - 1);
        corrs = new double[corrs.length - 1];
        corrs = result2.clone();
    }

    public double getCorrsForIdx(int idx) {
        return corrs[idx];
    }

    public void deleteDoubles(TreeSet<GroupLine> allStations){
        Iterator<GroupLine> iter = allStations.iterator();
        while(iter.hasNext()){
            GroupLine tempGroup = iter.next();
            ArrayList<Integer> currGroup = tempGroup.getGroup();
            if(index != tempGroup.index) {
                for (int i = 0; i < group.length; i++) {
                    int idx = currGroup.indexOf(group[i]);
                    if (idx != -1) {
                        if (tempGroup.getCorrsForIdx(idx) > corrs[i]) {
                            removeIdx(i);
                        }
                        else{
                           currGroup.remove(idx);
                            tempGroup.removeIdx(idx);
                        }
                    }
                }
            }
        }
    }

    public void setGroup(int[] group) {
        this.group = group;
    }

    @Override
    public int compareTo(GroupLine anotherGroupLine) {
        if (this.group[0] == anotherGroupLine.group[0]) {
            return 0;
        } else if (this.group[0] < anotherGroupLine.group[0]) {
            return -1;
        } else {
            return 1;
        }
    }
}