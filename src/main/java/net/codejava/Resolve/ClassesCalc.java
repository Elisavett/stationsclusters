package net.codejava.Resolve;

import net.codejava.Resolve.Clustering.CorrelationCalculation;
import net.codejava.Resolve.Clustering.GroupAllocate;
import net.codejava.Resolve.Model.Corr;
import net.codejava.Resolve.Model.Group;
import net.codejava.Resolve.Model.GroupLine;
import net.codejava.Resolve.Model.ResolveForm;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ClassesCalc {
    public static void calculation() throws InterruptedException, ExecutionException {
        int stationCount;
        TreeSet<GroupLine> sortGroupLine = new TreeSet<>();
        for (int i = 0; i < ResolveForm.arrayGroup.size(); i++) {
            Group group = ResolveForm.arrayGroup.get(i).get();
            int[] array = group.getArray();
            sortGroupLine.add(new GroupLine(array, group.getCorrs(), i));
        }
        if(!ResolveForm.groupCross) {
            for (GroupLine gr : sortGroupLine) {
                gr.deleteDoubles(sortGroupLine);
            }
        }
        //получаем количество процессоров
        int processors = Runtime.getRuntime().availableProcessors();
        //создаем пул на количество процессоров
        ExecutorService executorService = Executors.newFixedThreadPool(processors);

        stationCount = ResolveForm.TempData.length;

        List<Future<Corr>> arrayCorr;


        List<CorrelationCalculation> corrThreadTasks = new ArrayList<>();
        Object[] groupLines = sortGroupLine.toArray();
        for (int i = 0; i < groupLines.length; i++) {
            GroupLine gl = (GroupLine)groupLines[i];
            ArrayList<Integer> group = gl.getGroup();
            double[] phases;
            for (Integer integer : group) {
                phases = ResolveForm.arrayTypical.get(integer).get().getArray();
                CorrelationCalculation corrThread = new CorrelationCalculation(phases, i, stationCount, ResolveForm.arrayPhase);
                corrThreadTasks.add(corrThread);
            }
        }
        //выполняем все задачи. главный поток ждет
        arrayCorr = executorService.invokeAll(corrThreadTasks);

        ArrayList[] groups = new ArrayList[groupLines.length];
        for (int i = 0; i < groupLines.length; i++) {
            GroupLine gl = (GroupLine)groupLines[i];
            ArrayList<Integer> group = gl.getGroup();
            double[] groupAVG = new double[group.size()];
            for(int j = 0; j < group.size(); j++)
            {
                double average = 0;
                double[] groupTemp = arrayCorr.get(i).get().getArray();
                for (double v : groupTemp) {
                    average += v;
                }
                groupAVG[j] = average / groupTemp.length;
            }
            groups[i].add(groupAVG);
        }
        //блок выделения групп
        GroupAllocate allocationThread = new GroupAllocate(stationCount, ResolveForm.classCoef, arrayCorr, executorService);
        ResolveForm.arrayGroup = allocationThread.run();
    }
}
