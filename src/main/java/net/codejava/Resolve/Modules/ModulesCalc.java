package net.codejava.Resolve.Modules;

import net.codejava.Resolve.Clustering.*;
import net.codejava.Resolve.GroupsForMap;
import net.codejava.Resolve.Model.*;
import net.codejava.Resolve.PhaseCalc.AmplitudeCalculation;
import net.codejava.Resolve.PhaseCalc.PhaseCalculation;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ModulesCalc {
    public static void PhaseAmplCalc() throws InterruptedException, ExecutionException {
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
            ResolveForm.arrayPhase = ResolveForm.FutureToPlaneObj(arrayPhase);
            ResolveForm.arrayAmplitude = ResolveForm.FutureToPlaneObj(arrayAmplitude);
        } else {
            ResolveForm.arrayPhase = ResolveForm.FutureToPlaneObj(arrayAmplitude);
            ResolveForm.arrayAmplitude = ResolveForm.FutureToPlaneObj(arrayPhase);
        }
        executorService.shutdown();
    }

    public static void ClustersCalc() throws InterruptedException, ExecutionException {
        int count = 0;
        List<Phase> arrayPhase;
        int stationCount = ResolveForm.TempData.length;
        int processors = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(processors);
        if (!ResolveForm.isPhasesCounted){
            arrayPhase = ResolveForm.arrayPhase;
        }
        else{
            arrayPhase = new ArrayList<>();
            for (int i = 0; i < ResolveForm.TempData.length; i++) {
                Phase phase = new Phase(ResolveForm.TempData[i]);
                arrayPhase.add(phase);
            }
            ResolveForm.arrayPhase = arrayPhase;
        }
        List<Phase> typicalPhases = new ArrayList<>();
        List<Group> arrayGroup;
        List<Group> arrayPrevGroup = new ArrayList<>();
        List<Corr> arrayCorr;
        boolean check = false;
        int equalsCount;
        do {
            //блок вычисления таблицы корреляции
            // создаем лист задач
            List<CorrelationCalculation> corrThreadTasks = new ArrayList<>();
            for (int i = 0; i < stationCount; i++) {
                CorrelationCalculation corrThread = new CorrelationCalculation(arrayPhase.get(i), i, arrayPhase);
                corrThreadTasks.add(corrThread);
            }
            //выполняем все задачи. главный поток ждет
            arrayCorr = ResolveForm.FutureToPlaneObj(executorService.invokeAll(corrThreadTasks));

            //блок выделения групп
            GroupAllocation allocationThread = new GroupAllocation(stationCount, ResolveForm.corrDOWN, ResolveForm.corrUP, arrayCorr, executorService);
            arrayGroup = ResolveForm.FutureToPlaneObj(allocationThread.clustersCalc());

            equalsCount = 0;
            if(count>0 && ResolveForm.isAccurate) {
                for (int i = 0; i < stationCount; i++) {
                    if (arrayPrevGroup.get(i).equals(arrayGroup.get(i))) {
                        equalsCount++;
                    }
                }
                if (equalsCount == stationCount) //|| prevEqualsCount > equalsCount)
                    break;
            }
            //блок вычисления типовых фаз
            typicalPhases.clear();
            for (int i = 0; i < stationCount; i++) {
                Group groupIndex = arrayGroup.get(i);
                TypicalCalculation typical = new TypicalCalculation(stationCount, arrayPhase, groupIndex);
                Phase typicalPhase = typical.run();
                groupIndex.setPhases(typicalPhase);
                typicalPhases.add(typicalPhase);
            }

            //блок проверки конца алгоритма
            EndChecking checkMethod = new EndChecking(arrayPhase, typicalPhases, executorService);
            arrayPhase = ResolveForm.FutureToPlaneObj(checkMethod.run());
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
        TreeSet<Group> sortGroups = new TreeSet<>(ResolveForm.arrayGroup);
        if(!ResolveForm.groupCross) {
            for (Group gr : sortGroups) {
                gr.deleteDoubles(sortGroups);
            }
        }
        //получаем количество процессоров
        int processors = Runtime.getRuntime().availableProcessors();
        //создаем пул на количество процессоров
        ExecutorService executorService = Executors.newFixedThreadPool(processors);

        Object[] groupMas = sortGroups.toArray();

        //Получаем типовую фазу первой станции в группе и записываем её как фазу группы
        Phase[] groupStartPhases = new Phase[groupMas.length];
        for (int i = 0; i < groupMas.length; i++) {
            Group group = (Group) groupMas[i];
            Phase phase = ResolveForm.arrayTypical.get(group.getGroupMembers().get(0));
            groupStartPhases[i] = phase;
        }

        //Считаем корреляцию начальной фазы с каждой груповой фазой
        List<List<Double>> groupCorrs = new ArrayList<>();
        List<CorrelationCalculation> corrThreadTasks = new ArrayList<>();
        List<Future<Corr>> arrayCorr;
        for (int i = 0; i < groupStartPhases.length; i++) {
                Phase phase = ResolveForm.arrayPhase.get(i);
                CorrelationCalculation corrThread = new CorrelationCalculation(phase, i, groupStartPhases);
                corrThreadTasks.add(corrThread);

            //выполняем все задачи. главный поток ждет
        }
        arrayCorr = executorService.invokeAll(corrThreadTasks);
        for (Future<Corr> corrFuture : arrayCorr) {
            List<Double> correlations = corrFuture.get().getCorrelationArray();
            groupCorrs.add(correlations);
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
        ResolveForm.arrayGroup = ResolveForm.FutureToPlaneObj(allocationThread.classesCalc());
    }
    public static ArrayList<String> JsonCalc() {
        GroupsForMap.loadGroups();
        GroupsForMap.sortGroups();

        return GroupsForMap.getJson();
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
