package net.codejava.Resolve.Model;

import java.util.ArrayList;
import java.util.stream.IntStream;

public class GroupLine implements Comparable<GroupLine> {
    private int[] group;
    public GroupLine(int[] group){
        this.group=group;
    }

    public ArrayList<Integer> getGroup() {
        ArrayList<Integer> al = new ArrayList<Integer>();
        for(int i = 0; i < group.length; i++)
        {
            al.add(group[i]);
        }
        return al;
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