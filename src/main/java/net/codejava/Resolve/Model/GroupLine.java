package net.codejava.Resolve.Model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.IntStream;

public class GroupLine implements Comparable<GroupLine> {
    private int[] group;
    private double[] corrs;
    private boolean isLess5 = false;


    public GroupLine(int[] group, double[] corrs){
        this.group = group;
        this.corrs = corrs;
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
        for(GroupLine gr : allStations){
            ArrayList<Integer> currGroup = gr.getGroup();
            if(!Arrays.equals(group, currGroup.stream().mapToInt(q->q).toArray())) {
                for (int i = 0; i < group.length; i++) {
                    int idx = currGroup.indexOf(group[i]);
                    if (idx != -1) {
                        if (gr.getCorrsForIdx(idx) > corrs[i]) {
                            removeIdx(i);
                        }
                        else{
                           currGroup.remove(idx);
                           gr.removeIdx(idx);
                        }
                    }
                }
            }
        }
    }

    public boolean islessThenFive()
    {
        return isLess5;
    }
    public void lessThenFive()
    {
        isLess5 = true;
    }

    public void setGroup(int[] group) {
        this.group = group;
    }

    public boolean containsAll(int[] group2)
    {
        for (int i = 0; i < group.length; i++) {
            final int j = i;
            if (!(IntStream.of(group).anyMatch(x -> x == group2[j]))) {
                return false;
            }
        }
        return true;
    }
    public boolean containedIn(int[] group2)
    {
        for (int i = 0; i < group2.length; i++) {
            final int j = i;
            if (!(IntStream.of(group2).anyMatch(x -> x == group[j]))) {
                return false;
            }
        }
        return true;
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