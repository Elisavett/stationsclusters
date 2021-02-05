package net.codejava.Resolve;

import net.codejava.Resolve.Clustering.*;
import net.codejava.Resolve.Model.*;
import net.codejava.Resolve.PhaseCalc.AmplitudeCalculation;
import net.codejava.Resolve.PhaseCalc.PhaseCalculation;
import net.codejava.Resolve.PhaseCalc.RewritePhase;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

public class Start {
    public ArrayList<String> run() throws IOException, ClassNotFoundException, InterruptedException, ExecutionException {

        List<Future<Phase>> arrayPhase;
        int stationCount;
        //получаем количество процессоров
        int processors = Runtime.getRuntime().availableProcessors();
        //создаем пул на количество процессоров
        ExecutorService executorService = Executors.newFixedThreadPool(processors);

        if(!ResolveForm.isPhasesCounted) {
            stationCount = ResolveForm.TempData.length;//количество станций
            //блок вычисления фазы
            //создаем лист задач
            if(ResolveForm.isForPhases){
                List<PhaseCalculation> phaseCalculationTasks = new ArrayList<>();
                //long startExec = System.currentTimeMillis(); //время старта вычислений
                for (int i = 0; i < stationCount; i++) {
                    double[] temp = ResolveForm.TempData[i].clone();
                    PhaseCalculation phaseCalculation = new PhaseCalculation(temp, ResolveForm.windowLeft, ResolveForm.windowRight);
                    phaseCalculationTasks.add(phaseCalculation);
                }
                //выполняем все задачи. главный поток ждет
                arrayPhase = executorService.invokeAll(phaseCalculationTasks);
                ResolveForm.arrayPhase = arrayPhase;
            }
            else {
                List<AmplitudeCalculation> amplitudeCalculationTasks = new ArrayList<>();
                //long startExec = System.currentTimeMillis(); //время старта вычислений
                for (int i = 0; i < stationCount; i++) {
                    double[] temp = ResolveForm.TempData[i].clone();
                    AmplitudeCalculation phaseCalculation = new AmplitudeCalculation(temp, ResolveForm.windowLeft, ResolveForm.windowRight);
                    amplitudeCalculationTasks.add(phaseCalculation);
                }
                //выполняем все задачи. главный поток ждет
                arrayPhase = executorService.invokeAll(amplitudeCalculationTasks);
                ResolveForm.arrayPhase = arrayPhase;
            }

            //Время выполнения п
            //long finishExec = System.currentTimeMillis(); // время конца вычислений
            //System.out.println("Total phase calculation time: " + (finishExec - startExec));
        }

        else{
            List<RewritePhase> rewritePhases = new ArrayList<>();
            for (int i = 0; i < ResolveForm.PhasesData.length; i++) {
                RewritePhase phaseCalculation = new RewritePhase(ResolveForm.PhasesData[i]);
                rewritePhases.add(phaseCalculation);
            }
            arrayPhase = executorService.invokeAll(rewritePhases);
            stationCount = arrayPhase.size();
        }
        int count = 0;
        ArrayData arrayTypicalPath = new ArrayData();
        List<Future<Group>> arrayGroup;
        List<Future<Group>> arrayPrevGroup = new ArrayList<>();
        List<Future<Corr>> arrayCorr;
        boolean check = false;
        //long cicleStartExec = System.currentTimeMillis(); //время старта вычислений
        do {
            //System.out.println("Cicle");
            //блок вычисления таблицы корреляции
            // создаем лист задач
            long startExec = System.currentTimeMillis(); //время старта вычислений
            List<CorrelationCalculation> corrThreadTasks = new ArrayList<>();
            for (int i = 0; i < stationCount; i++) {
                CorrelationCalculation corrThread = new CorrelationCalculation(arrayPhase.get(i), i, stationCount, arrayPhase);
                corrThreadTasks.add(corrThread);
            }
            //выполняем все задачи. главный поток ждет
            arrayCorr = executorService.invokeAll(corrThreadTasks);
            //System.out.println("Corelation");
            long finishExec = System.currentTimeMillis(); // время конца вычислений
            //System.out.println("Total corelation calculation time: " + (finishExec - startExec));

            //startExec = System.currentTimeMillis(); //время старта вычислений

            //блок выделения групп
            GroupAllocation1 allocationThread = new GroupAllocation1(false, stationCount, ResolveForm.corr, arrayCorr, executorService);
            arrayGroup = allocationThread.run();
            //finishExec = System.currentTimeMillis(); // время конца вычислений
            //System.out.println("Total group allocation time: " + (finishExec - startExec));
            int equalsCount = 0;
            if(count>0 && ResolveForm.isAccurate) {
                for (int i = 0; i < stationCount; i++) {
                    if (Arrays.equals(arrayPrevGroup.get(i).get().getArray(), arrayGroup.get(i).get().getArray())) {
                        equalsCount++;
                    }
                }
                if (equalsCount == stationCount)
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
        //System.out.println("count=" + count);
        long finishExec = System.currentTimeMillis(); // время конца вычислений
        //System.out.println("Total cycle ending time: " + (finishExec - cicleStartExec));
        //Алгоритм итоговой группировки

        /////////Классификация

        if(ResolveForm.classification) {

            List<CorrelationCalculation> corrThreadTasks = new ArrayList<>();
            for (int i = 0; i < stationCount; i++) {
                CorrelationCalculation corrThread = new CorrelationCalculation(ResolveForm.arrayPhase.get(i), i, stationCount, arrayPhase);
                corrThreadTasks.add(corrThread);
            }
            //выполняем все задачи. главный поток ждет
            arrayCorr = executorService.invokeAll(corrThreadTasks);
            //System.out.println("Corelation");
            //System.out.println("Total corelation calculation time: " + (finishExec - startExec));

            //startExec = System.currentTimeMillis(); //время старта вычислений
            //блок выделения групп
            GroupAllocation1 allocationThread = new GroupAllocation1(true, stationCount, ResolveForm.corr, arrayCorr, executorService);
            arrayGroup = allocationThread.run();
        }

        // объединяю станции в группы, дописываю координаты
        //Merger merger = new Merger(stationCount, arrayGroup, ResolveForm.minGroupSize);
        Merger1 merger = new Merger1(stationCount, arrayGroup, ResolveForm.minGroupSize);
        ArrayList<String> groupAndCoordinates = merger.run();

//        System.out.println(System.currentTimeMillis() - start);
        //finishExec = System.currentTimeMillis(); // время конца вычислений
        //System.out.println("Total calculations time: " + (finishExec - cicleStartExec));
        //закрываем потоки
        executorService.shutdown();
        return groupAndCoordinates;
    }
}
