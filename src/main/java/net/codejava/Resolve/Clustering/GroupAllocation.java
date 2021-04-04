package net.codejava.Resolve.Clustering;

import net.codejava.Resolve.Model.Corr;
import net.codejava.Resolve.Model.Group;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class GroupAllocation {
    double corrDOWN;
    double corrUP;
    List<List<Double>> corrTable;
    List<List<Integer>> groups;
    List<List<Double>> groupsCorrs;
    int stationCount;
    List<Future<Corr>> arrayCorr;
    ExecutorService executorService;

    public GroupAllocation(int stationCount, double corrDOWN, double corrUP, List<Future<Corr>> arrayCorr, ExecutorService executorService)
    {
        this(executorService);
        this.stationCount = stationCount;
        this.corrDOWN = corrDOWN;
        this.corrUP = corrUP;
        this.arrayCorr = arrayCorr;
    }
    public GroupAllocation(List<List<Integer>> groups, List<List<Double>> corrs, ExecutorService executorService) {
        this(executorService);
        this.groups = groups;
        this.groupsCorrs = corrs;
    }
    public GroupAllocation(ExecutorService executorService)
    {
        this.executorService = executorService;
    }

    public List<Future<Group>> clustersCalc() throws ExecutionException, InterruptedException {
        loadCorrelationTable();
        allocateGroups();
        return saveGroups();
    }
    public List<Future<Group>> classesCalc() throws ExecutionException, InterruptedException {
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
            }
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

    private List<Future<Group>> saveGroups() throws InterruptedException {
        //создаем лист задач
        List<MyCallable> tasks = new ArrayList<>();
        for (int i = 0; i < groups.size(); i++) {
            MyCallable myCallable = new MyCallable(i, groups, groupsCorrs);
            tasks.add(myCallable);
        }
        return executorService.invokeAll(tasks);
    }


    static class MyCallable implements Callable<Group> {
        int position;
        List<List<Integer>> groups;
        List<List<Double>> coords;

        public MyCallable(int position, List<List<Integer>> groups, List<List<Double>> coords) {
            this.position = position;// i = position
            this.groups = groups;
            this.coords = coords;
        }

        @Override
        public Group call() {
            int[] groupsArray = new int[groups.get(position).size()];
            double[] corrsArray = new double[groups.get(position).size()];
            for (int j = 0; j < groups.get(position).size(); j++) {
                groupsArray[j] = groups.get(position).get(j);
                corrsArray[j] = groups.get(position).get(j);
            }
            return new Group(groupsArray, corrsArray);
        }
    }
}

