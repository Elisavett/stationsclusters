package net.codejava.Resolve;

import net.codejava.Resolve.Clustering.CorrelationCalculation;
import net.codejava.Resolve.Clustering.GroupAllocation1;
import net.codejava.Resolve.Model.Corr;
import net.codejava.Resolve.Model.Group;
import net.codejava.Resolve.Model.ResolveForm;
import net.codejava.Resolve.PhaseCalc.RewritePhase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ClassesCalc {
    public static void calculation() throws InterruptedException, ExecutionException {
        int stationCount;
        //получаем количество процессоров
        int processors = Runtime.getRuntime().availableProcessors();
        //создаем пул на количество процессоров
        ExecutorService executorService = Executors.newFixedThreadPool(processors);

        stationCount = ResolveForm.TempData.length;

        List<Future<Corr>> arrayCorr;


        List<CorrelationCalculation> corrThreadTasks = new ArrayList<>();
        for (int i = 0; i < stationCount; i++) {
            CorrelationCalculation corrThread = new CorrelationCalculation(ResolveForm.arrayPhase.get(i), i, stationCount, ResolveForm.arrayTypical);
            corrThreadTasks.add(corrThread);
        }
        //выполняем все задачи. главный поток ждет
        arrayCorr = executorService.invokeAll(corrThreadTasks);

        //блок выделения групп
        GroupAllocation1 allocationThread = new GroupAllocation1(stationCount, ResolveForm.classCoef, arrayCorr, executorService);
        ResolveForm.arrayGroup = allocationThread.run();
    }
}
