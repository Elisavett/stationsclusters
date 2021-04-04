package net.codejava.Resolve;

import net.codejava.Resolve.Clustering.*;
import net.codejava.Resolve.Model.*;
import net.codejava.Resolve.PhaseCalc.AmplitudeCalculation;
import net.codejava.Resolve.PhaseCalc.PhaseCalculation;
import net.codejava.Resolve.PhaseCalc.RewritePhase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ModulesCalc {
    public static void PhaseAmplCalc() throws InterruptedException {
        List<Future<Phase>> arrayPhase;
        List<Future<Phase>> arrayAmplitude;
        //получаем количество процессоров
        int processors = Runtime.getRuntime().availableProcessors();
        //создаем пул на количество процессоров
        ExecutorService executorService = Executors.newFixedThreadPool(processors);

        int stationCount = ResolveForm.TempData.length;//количество станций
        //блок вычисления фазы
        //создаем лист задач
        List<PhaseCalculation> phaseCalculationTasks = new ArrayList<>();
        //long startExec = System.currentTimeMillis(); //время старта вычислений
        for (int i = 0; i < stationCount; i++) {
            double[] temp = ResolveForm.TempData[i].clone();
            PhaseCalculation phaseCalculation = new PhaseCalculation(temp, ResolveForm.windowLeft, ResolveForm.windowRight);
            phaseCalculationTasks.add(phaseCalculation);
        }
        //выполняем все задачи. главный поток ждет

        arrayPhase = executorService.invokeAll(phaseCalculationTasks);

        List<AmplitudeCalculation> amplitudeCalculationTasks = new ArrayList<>();
        //long startExec = System.currentTimeMillis(); //время старта вычислений
        for (int i = 0; i < stationCount; i++) {
            double[] temp = ResolveForm.TempData[i].clone();
            AmplitudeCalculation phaseCalculation = new AmplitudeCalculation(temp, ResolveForm.windowLeft, ResolveForm.windowRight);
            amplitudeCalculationTasks.add(phaseCalculation);
        }
        //выполняем все задачи. главный поток ждет

        arrayAmplitude = executorService.invokeAll(amplitudeCalculationTasks);
        if (ResolveForm.isForPhases) {
            ResolveForm.arrayPhase = arrayPhase;
            ResolveForm.arrayAmplitude = arrayAmplitude;
        } else {

            ResolveForm.arrayPhase = arrayAmplitude;
            ResolveForm.arrayAmplitude = arrayPhase;
        }
        executorService.shutdown();
    }

    public static void ClustersCalc() throws InterruptedException, ExecutionException {
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
            GroupAllocation allocationThread = new GroupAllocation(stationCount, ResolveForm.corrDOWN, ResolveForm.corrUP, arrayCorr, executorService);
            arrayGroup = allocationThread.clustersCalc();
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
    public static void ClassesCalc() throws InterruptedException, ExecutionException {
        TreeSet<GroupLine> sortGroupLine = new TreeSet<>();
        for (int i = 0; i < ResolveForm.arrayGroup.size(); i++) {
            Group group = ResolveForm.arrayGroup.get(i).get();
            double[] phase = ResolveForm.arrayPhase.get(i).get().getArray();
            int[] array = group.getArray();
            sortGroupLine.add(new GroupLine(array, group.getCorrs(), phase, i));
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

        Object[] groupLines = sortGroupLine.toArray();

        //Получаем типовую фазу первой станции в группе и записываем её как фазу группы
        List<double[]> groupStartPhases = new ArrayList<>();
        for (Object line : groupLines) {
            GroupLine groupLine = (GroupLine) line;
            Phase phase = ResolveForm.arrayTypical.get(groupLine.getGroup().get(0)).get();
            groupStartPhases.add(phase.getArray());
        }

        //Считаем корреляцию начальной фазы с каждой груповой фазой
        List<List<Double>> groupCorrs = new ArrayList<>();
        for (int i = 0; i < groupStartPhases.size(); i++) {
            groupCorrs.add(new ArrayList<>());
            for(int j = 0; j < ResolveForm.arrayPhase.size(); j++){
                double[] phase = ResolveForm.arrayPhase.get(j).get().getArray();
                groupCorrs.get(i).add(correlationCalc(groupStartPhases.get(i), phase));
            }
        }
        //Находим наибольшую корреляцию для станции по всем группам. Остальные обращаются в 0
        for (int i = 0; i < groupCorrs.size(); i++) {
            for(int j = 0; j < groupCorrs.get(i).size(); j++) {
                double currCor = groupCorrs.get(i).get(j);
                for (List<Double> groupCorr : groupCorrs) {
                    if (groupCorr.get(j) < currCor)
                        groupCorr.set(j, 0.0);
                }
            }
        }
        //Выбираем из списка только те станции, у которых корреляция больше установленного значения
        List<List<Integer>> groups = new ArrayList<>();
        List<List<Double>> corrs = new ArrayList<>();
        int[] lonelyStations = new int[ResolveForm.arrayPhase.size()];
        for (List<Double> groupCorr : groupCorrs) {
            List<Integer> tempGroup = new ArrayList<>();
            List<Double> tempCorr = new ArrayList<>();
            for (int j = 0; j < groupCorr.size(); j++) {
                if (groupCorr.get(j) > ResolveForm.classCoef) {
                    tempGroup.add(j);
                    tempCorr.add(groupCorr.get(j));
                } else {
                    //Считаем сколько раз станция не входила в группу
                    lonelyStations[j]++;
                }
            }
            if (tempGroup.size() > 0) {
                groups.add(tempGroup);
                corrs.add(tempCorr);
            }
        }
        for (int i = 0; i < groupCorrs.size(); i++) {
            //Если станция не вошла ни в одну группу
            //Добавим её отдельной группой
            if(lonelyStations[i] == groupCorrs.size()){
                List<Integer> tempGroup = new ArrayList<>();
                List<Double> tempCorr = new ArrayList<>();
                tempGroup.add(i);
                tempCorr.add(0.0);
                groups.add(tempGroup);
                corrs.add(tempCorr);
            }
        }
        GroupAllocation allocationThread = new GroupAllocation(groups, corrs, executorService);
        ResolveForm.arrayGroup = allocationThread.classesCalc();
    }
    public static ArrayList<String> JsonCalc() throws InterruptedException, ExecutionException, IOException {
        Merger merger = new Merger(ResolveForm.TempData.length, ResolveForm.arrayGroup, ResolveForm.arrayTypical, ResolveForm.minGroupSize);
        return merger.run();
    }
    private static double correlationCalc(double[] firstStation, double[] secondStation) {
        double average1 = 0, average2 = 0;
        int num = firstStation.length;
        for (int i = 0; i < num; i++) {
            average1 += firstStation[i];
            average2 += secondStation[i];
        }
        if(average1 == 0 || average2 == 0)
            return 0;
        average1 = average1 / num;
        average2 = average2 / num;

        double part1 = 0;
        double part2 = 0;
        double part3 = 0;
        for (int i = 0; i < num; i++) {
            double dif1 = firstStation[i] - average1;
            double dif2 = secondStation[i] - average2;
            part1 += dif1 * dif2;
            part2 += dif1 * dif1;
            part3 += dif2 * dif2;
        }
        double part4 = Math.sqrt(part2 * part3);
        return part1 / part4;
    }
}
