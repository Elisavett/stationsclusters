package net.codejava.Resolve.Clustering;

import net.codejava.Resolve.Model.*;
import net.codejava.Resolve.PhaseCalc.RewritePhase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ClustersCalc {
    public static void calculation() throws InterruptedException, ExecutionException {
        int count = 0;
        List<Future<Phase>> arrayPhase;
        int stationCount = ResolveForm.TempData.length;
        int processors = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(processors);
        List<RewritePhase> rewritePhases = new ArrayList<>();
        if (!ResolveForm.isPhasesCounted){
            for (int i = 0; i < ResolveForm.arrayPhase.size(); i++) {
                RewritePhase phaseCalculation = new RewritePhase(ResolveForm.arrayPhase.get(i).get().getArray());
                rewritePhases.add(phaseCalculation);
            }
        arrayPhase = executorService.invokeAll(rewritePhases);
        }
        else{
            for (int i = 0; i < ResolveForm.TempData.length; i++) {
                RewritePhase phaseCalculation = new RewritePhase(ResolveForm.TempData[i]);
                rewritePhases.add(phaseCalculation);
            }
            arrayPhase = executorService.invokeAll(rewritePhases);
            ResolveForm.arrayPhase = arrayPhase;
        }
        ArrayData arrayTypicalPath = new ArrayData();
        List<Future<Group>> arrayGroup;
        List<Future<Group>> arrayPrevGroup = new ArrayList<>();
        List<Future<Corr>> arrayCorr;
        boolean check = false;
        int equalsCount = 0;
        //long cicleStartExec = System.currentTimeMillis(); //время старта вычислений
        do {
            //System.out.println("Cicle");
            //блок вычисления таблицы корреляции
            // создаем лист задач
            long startExec = System.currentTimeMillis(); //время старта вычислений
            List<CorrelationCalculation> corrThreadTasks = new ArrayList<>();
            for (int i = 0; i < stationCount; i++) {
                CorrelationCalculation corrThread = new CorrelationCalculation(arrayPhase.get(i).get().getArray(), i, stationCount, arrayPhase);
                corrThreadTasks.add(corrThread);
            }
            //выполняем все задачи. главный поток ждет
            arrayCorr = executorService.invokeAll(corrThreadTasks);
            //System.out.println("Corelation");
            long finishExec = System.currentTimeMillis(); // время конца вычислений
            //System.out.println("Total corelation calculation time: " + (finishExec - startExec));

            //startExec = System.currentTimeMillis(); //время старта вычислений

            //блок выделения групп
            GroupAllocation1 allocationThread = new GroupAllocation1(stationCount, ResolveForm.corrDOWN, ResolveForm.corrUP, arrayCorr, executorService);
            arrayGroup = allocationThread.run();
            //finishExec = System.currentTimeMillis(); // время конца вычислений
            //System.out.println("Total group allocation time: " + (finishExec - startExec));

            int prevEqualsCount = equalsCount;
            equalsCount = 0;
            if(count>0 && ResolveForm.isAccurate) {
                for (int i = 0; i < stationCount; i++) {
                    if (Arrays.equals(arrayPrevGroup.get(i).get().getArray(), arrayGroup.get(i).get().getArray())) {
                        equalsCount++;
                    }
                }
                if (equalsCount == stationCount) //|| prevEqualsCount > equalsCount)
                    break;
            }
            //System.out.println("Groups");
            //блок вычисления типовых фаз
            arrayTypicalPath.clear();
            //startExec = System.currentTimeMillis(); //время старта вычислений
            for (int i = 0; i < stationCount; i++) {
                Group groupIndex = (Group) arrayGroup.get(i).get();
                TypicalCalculation typical = new TypicalCalculation(stationCount, arrayPhase, groupIndex);
                TypicalPhase typicalPhase = typical.run();
                arrayTypicalPath.addData(typicalPhase);
            }

            //finishExec = System.currentTimeMillis(); // время конца вычислений
            //System.out.println("Total typical calculation time: " + (finishExec - startExec));
            //System.out.println("Typicals");

            //блок проверки конца алгоритма
            EndChecking checkMethod = new EndChecking(stationCount, arrayPhase, arrayTypicalPath, executorService);
            arrayPhase = checkMethod.run();
            if(!ResolveForm.isAccurate) check = checkMethod.check();

            count++;
            arrayPrevGroup.clear();
            arrayPrevGroup.addAll(arrayGroup);
//            System.out.println(count);
        } while (!check);
        ResolveForm.arrayTypical = arrayPhase;
        ResolveForm.arrayGroup = arrayPrevGroup;
        executorService.shutdown();
    }
}
