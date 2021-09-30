package net.codejava.Resolve.Modules;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.codejava.Resolve.Clustering.*;
import net.codejava.Resolve.GroupsForMap;
import net.codejava.Resolve.Model.*;
import net.codejava.Resolve.PhaseCalc.AmplitudeCalculation;
import net.codejava.Resolve.PhaseCalc.PhaseCalculation;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/*
    Класс для модульного рассчета
 */

public class ModulesCalc {

    //Рассчет фазы / амлитуды
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
        for (int i = 0; i < stationCount; i++) {
            double[] temp = ResolveForm.TempData[i].clone();
            AmplitudeCalculation phaseCalculation = new AmplitudeCalculation(temp, ResolveForm.windowLeft, ResolveForm.windowRight);
            amplitudeCalculationTasks.add(phaseCalculation);
        }
        //выполняем все задачи. главный поток ждет

        arrayAmplitude = executorService.invokeAll(amplitudeCalculationTasks);

        ResolveForm.arrayPhase = ResolveForm.FutureToPlaneObj(arrayPhase);
        ResolveForm.arrayAmplitude = ResolveForm.FutureToPlaneObj(arrayAmplitude);

        if (ResolveForm.isForPhases) {
            ResolveForm.countableCharacter = ResolveForm.arrayPhase;
        }
        else{
            ResolveForm.countableCharacter = ResolveForm.arrayAmplitude;
        }

        executorService.shutdown();
    }

    //Рассчет кластеризации
    public static void ClustersCalc(boolean isFromPrev) throws InterruptedException, ExecutionException {
        int count = 0;
        List<Phase> arrayPhase;
        int stationCount = ResolveForm.TempData.length;
        int processors = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(processors);
        //Если фаза не рассчитана
        if (!ResolveForm.isPhasesCounted){
            arrayPhase = ResolveForm.countableCharacter;
        }
        else{
            arrayPhase = new ArrayList<>();
            for (int i = 0; i < ResolveForm.TempData.length; i++) {
                Phase phase = new Phase(ResolveForm.TempData[i]);
                arrayPhase.add(phase);
            }
            ResolveForm.arrayPhase = new ArrayList<>(arrayPhase);
        }
        List<Phase> typicalPhases = new ArrayList<>();
        List<Group> arrayGroup;
        List<Group> arrayPrevGroup = new ArrayList<>();
        List<List<Double>> arrayCorr;
        boolean check = false;
        int equalsCount;
        do {
            //блок вычисления таблицы корреляции
            // создаем лист задач
            List<CorrelationCalculation> corrThreadTasks = new ArrayList<>();
            for (int i = 0; i < stationCount; i++) {
                //Рассчет корреляции
                CorrelationCalculation corrThread = new CorrelationCalculation(arrayPhase.get(i), i, arrayPhase);
                corrThreadTasks.add(corrThread);
            }
            //выполняем все задачи. главный поток ждет
            arrayCorr = ResolveForm.FutureToPlaneObj(executorService.invokeAll(corrThreadTasks));

            //блок выделения групп
            GroupAllocation allocationThread = new GroupAllocation(stationCount, ResolveForm.corrDOWN, ResolveForm.corrUP, arrayCorr);
            arrayGroup = allocationThread.clustersCalc();

            equalsCount = 0;
            if(count == 0) ResolveForm.arrayCorr = arrayCorr;
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
                Group group = arrayGroup.get(i);
                TypicalCalculation typical;
                if(isFromPrev){
                    typical = new TypicalCalculation(stationCount, arrayPhase, group);
                }
                else{
                    typical = new TypicalCalculation(stationCount, ResolveForm.countableCharacter, group);
                }
                Phase typicalPhase = typical.run();
                group.setPhases(typicalPhase);
                typicalPhases.add(typicalPhase);
            }

            //блок проверки конца алгоритма
            EndChecking checkMethod = new EndChecking(arrayPhase, typicalPhases, executorService);
            arrayPhase = ResolveForm.FutureToPlaneObj(checkMethod.run());
            if(!ResolveForm.isAccurate) check = checkMethod.check();

            count++;
            arrayPrevGroup.clear();
            arrayPrevGroup.addAll(arrayGroup);
        } while (!check);
        //Запоминаем группы и фазы
        ResolveForm.arrayTypical = arrayPhase;
        ResolveForm.arrayGroup = arrayPrevGroup;
        executorService.shutdown();
    }

    //Рассчет классификации
    public static void ClassesCalc() throws InterruptedException, ExecutionException {
        TreeSet<Group> sortGroups = new TreeSet<>(ResolveForm.arrayGroup);
        ArrayList<Group> tmpGroup = new ArrayList<>(sortGroups);
        if(!ResolveForm.groupCross) {
            for (Group gr : tmpGroup) {
                gr.deleteDoubles(tmpGroup);
            }
        }

        tmpGroup.removeIf(s->s.getGroupMembers().size() == 0);
        sortGroups = new TreeSet<>(tmpGroup);
        //получаем количество процессоров
        int processors = Runtime.getRuntime().availableProcessors();
        //создаем пул на количество процессоров
        ExecutorService executorService = Executors.newFixedThreadPool(processors);

        Object[] groupMas = sortGroups.toArray();

        //Получаем типовую фазу первой станции в группе и записываем её как фазу группы
        Phase[] groupStartPhases = new Phase[groupMas.length];
        for (int i = 0; i < groupMas.length; i++) {
            Group group = (Group) groupMas[i];
            int index = group.getGroupMembers().get(0);
            Phase phase = ResolveForm.arrayTypical.get(index);
            groupStartPhases[i] = phase;
        }

        //Считаем корреляцию начальной фазы с каждой груповой фазой
        List<List<Double>> groupCorrs = new ArrayList<>();
        List<CorrelationCalculation> corrThreadTasks = new ArrayList<>();
        for (int i = 0; i < groupStartPhases.length; i++) {
            Phase phase = groupStartPhases.clone()[i];
            CorrelationCalculation corrThread = new CorrelationCalculation(phase, -1, ResolveForm.countableCharacter);
            corrThreadTasks.add(corrThread);
        }
        groupCorrs = ResolveForm.FutureToPlaneObj(executorService.invokeAll(corrThreadTasks));
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
        int[] GroupEntrys = new int[ResolveForm.arrayPhase.size()];
        for (List<Double> groupCorr : groupCorrs) {
            List<Integer> tempGroup = new ArrayList<>();
            List<Double> tempCorr = new ArrayList<>();
            for (int j = 0; j < groupCorr.size(); j++) {
                if (groupCorr.get(j) > ResolveForm.classCoefDOWN && groupCorr.get(j) < ResolveForm.classCoefUP) {
                    tempGroup.add(j);
                    tempCorr.add(groupCorr.get(j));
                    //Считаем сколько раз станция вошла в группу
                    GroupEntrys[j]++;
                }
            }
            if (tempGroup.size() > 0) {
                groups.add(tempGroup);
                corrs.add(tempCorr);
            }
        }
        for (int i = 0; i < GroupEntrys.length; i++) {
            //Если станция не вошла ни в одну группу
            //Добавим её отдельной группой
            if(GroupEntrys[i] == 0){
                List<Integer> tempGroup = new ArrayList<>();
                List<Double> tempCorr = new ArrayList<>();
                tempGroup.add(i);
                tempCorr.add(0.0);
                groups.add(tempGroup);
                corrs.add(tempCorr);
            }
        }
        GroupAllocation allocationThread = new GroupAllocation(groups, corrs);
        ResolveForm.arrayGroup = allocationThread.classesCalc();
        for (int i = 0; i < groupMas.length; i++) {
            ResolveForm.arrayGroup.get(i).setPhases(groupStartPhases[i]);
        }
    }
    //Преобразование данных о группах для отображения на карте
    public static ArrayList<String> JsonCalc() {
        GroupsForMap.loadGroups();
        GroupsForMap.sortGroups();

        return GroupsForMap.getJson();
    }
    //Получить данные для первоначального отображения станций на карте (до выполнения рассчета)
    public static ArrayList<String> JsonWOResolve() {
        Gson GSON = new GsonBuilder().create();
        //формирую json файл
        ArrayList<String> json = new ArrayList<>();
        for (int j = 0; j < ResolveForm.coordData[0].length; j++) {
            Map<String, String> additional = new HashMap<>();
            if (ResolveForm.coordData.length > 3 && ResolveForm.fileParams.length > 1) {
                int limit = Math.min((ResolveForm.coordData.length - 3), ResolveForm.fileParams.length / 2);
                for (int i = 0; i < limit; i++) {
                    if (Boolean.parseBoolean(ResolveForm.fileParams[i * 2 + 1]))
                        additional.put(ResolveForm.fileParams[i * 2], ResolveForm.coordData[i + 3][j]);
                }
            }
            GroupAndCoordinates groupAndCoordinates = new GroupAndCoordinates(
                    Double.parseDouble(ResolveForm.coordData[0][j]),
                    Double.parseDouble(ResolveForm.coordData[1][j]),
                    Double.parseDouble(ResolveForm.coordData[2][j]),
                    1,
                    true,
                    additional);
            String jsonData = GSON.toJson(groupAndCoordinates);
            json.add(jsonData);
        }

        return json;
    }

}
