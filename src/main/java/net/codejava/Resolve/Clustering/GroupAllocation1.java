package net.codejava.Resolve.Clustering;

import net.codejava.Resolve.Model.Corr;
import net.codejava.Resolve.Model.Group;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class GroupAllocation1 {
    double corrDOWN;
    double corrUP;
    ArrayList<ArrayList<Double>> corrTable;
    ArrayList[] groups;
    int stationCount;
    List<Future<Corr>> arrayCorr;
    ExecutorService executorService;
    double[] maxes;

    public GroupAllocation1(int stationCount, double corrDOWN, double corrUP, List<Future<Corr>> arrayCorr, ExecutorService executorService) {
        this.stationCount = stationCount;
        this.corrDOWN = corrDOWN;
        this.corrUP = corrUP;
        this.arrayCorr = arrayCorr;
        this.executorService = executorService;
        this.maxes = new double[stationCount];
    }

    public List<Future<Group>> run() throws ExecutionException, InterruptedException {
        loadCorrelationTable();
        allocateGroups();
        return saveGroups();
    }

    private void loadCorrelationTable() throws ExecutionException, InterruptedException {
        corrTable = new ArrayList<>();
        for (int i = 0; i < stationCount; i++) {
            Corr corr = arrayCorr.get(i).get();
            double[] array = corr.getArray();
            corrTable.add(new ArrayList<>());
            for (int j = 0; j < array.length; j++) {

                corrTable.get(i).add(array[j]);
                if(corrTable.get(i).get(j)>maxes[j])
                {
                    maxes[j] = corrTable.get(i).get(j);
                }
            }
        }
    }
    ArrayList[] groupsCorrs;
    private void allocateGroups() {
        groups = new ArrayList[corrTable.size()];
        groupsCorrs = new ArrayList[corrTable.size()];
        for (int i = 0; i < groups.length; i++) {
            groups[i] = new ArrayList<Integer>();
            groupsCorrs[i] = new ArrayList<Double>();
        }
        try {
            for (int i = 0; i < groups.length; i++) {
                for (int j = 0; j < groups.length; j++) {
                    if (i == j) {
                        groups[i].add(j);
                        groupsCorrs[i].add(0.0);
                    }
                    else {
                        if (j > i) {
                            if (corrTable.get(i).get(j - i - 1) >= corrDOWN && corrTable.get(i).get(j - i - 1) <= corrUP ) {
                                groups[i].add(j);
                                groupsCorrs[i].add(corrTable.get(i).get(j - i - 1));
                            }
                        } else {
                            if (corrTable.get(j).get(i - j - 1) >= corrDOWN && corrTable.get(j).get(i - j - 1) <= corrUP) {
                                groups[i].add(j);
                                groupsCorrs[i].add(corrTable.get(j).get(i - j - 1));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    private List<Future<Group>> saveGroups() throws InterruptedException {
        //создаем лист задач
        List<MyCallable> tasks = new ArrayList<>();
        for (int i = 0; i < groups.length; i++) {
            MyCallable myCallable = new MyCallable(i, groups, groupsCorrs);
            tasks.add(myCallable);
        }
        return executorService.invokeAll(tasks);
    }


    static class MyCallable implements Callable<Group> {
        int position;
        ArrayList<Integer>[] groups;
        ArrayList<Double>[] coords;

        public MyCallable(int position, ArrayList<Integer>[] groups, ArrayList<Double>[] coords) {
            this.position = position;// i = position
            this.groups = groups;
            this.coords = coords;
        }

        @Override
        public Group call() {
            int[] groupsArray = new int[groups[position].size()];
            double[] corrsArray = new double[groups[position].size()];
            for (int j = 0; j < groups[position].size(); j++) {
                groupsArray[j] = groups[position].get(j);
                corrsArray[j] = coords[position].get(j);
            }
            return new Group(groupsArray, corrsArray);
        }
    }
}

