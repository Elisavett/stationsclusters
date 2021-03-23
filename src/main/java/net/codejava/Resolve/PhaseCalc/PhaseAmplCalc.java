package net.codejava.Resolve.PhaseCalc;

import net.codejava.Resolve.Model.Phase;
import net.codejava.Resolve.Model.ResolveForm;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PhaseAmplCalc {
    public static void calculation() throws InterruptedException {
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
}

