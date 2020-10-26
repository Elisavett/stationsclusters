package net.codejava.Resolve;

import net.codejava.Resolve.Model.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Start {
    public ArrayList<String> run() throws IOException, ClassNotFoundException, InterruptedException, ExecutionException {
        //long start = System.currentTimeMillis();
        //System.out.println("Start");
        //получаем количество процессоров
        int processors = Runtime.getRuntime().availableProcessors();
        //создаем пул на количество процессоров
        ExecutorService executorService = Executors.newFixedThreadPool(processors);

        //File file = new File(fileTemp); // файл с температурами
        //String pathCoordinates = path + Coord;//Путь до координат

        List<Future<Phase>> arrayPhase;
        ArrayData arrayTypicalPath = new ArrayData();
        List<Future<Group>> arrayGroup;
        List<Future<Corr>> arrayCorr;


        //блок вычисления фазы
        //создаем лист задач
        List<PhaseCalculation> phaseCalculationTasks = new ArrayList<>();
        int stationCount = ResolveForm.TempData.length;//количество станций
        for (int i = 0; i < stationCount; i++) {
            double[] temp = ResolveForm.TempData[i].clone();
            PhaseCalculation phaseCalculation = new PhaseCalculation(temp, ResolveForm.windowLeft, ResolveForm.windowRight);
            phaseCalculationTasks.add(phaseCalculation);
        }
        //выполняем все задачи. главный поток ждет
        arrayPhase = executorService.invokeAll(phaseCalculationTasks);
/*        System.out.println("Phases");
        System.out.println(arrayPhase.size());*/

        int count = 0;
        boolean check;
        do {
            //System.out.println("Cicle");
            //блок вычисления таблицы корреляции
            // создаем лист задач
            List<CorrelationCalculation> corrThreadTasks = new ArrayList<>();
            for (int i = 0; i < stationCount; i++) {
                CorrelationCalculation corrThread = new CorrelationCalculation(i, stationCount, arrayPhase);
                corrThreadTasks.add(corrThread);
            }
            //выполняем все задачи. главный поток ждет
            arrayCorr = executorService.invokeAll(corrThreadTasks);
            //System.out.println("Corelation");

            //блок выделения групп
            GroupAllocation allocationThread = new GroupAllocation(stationCount, ResolveForm.corr, arrayCorr, executorService);
            arrayGroup = allocationThread.run();

            //System.out.println("Groups");
            //блок вычисления типовых фаз
            arrayTypicalPath.clear();
            for (int i = 0; i < stationCount; i++) {
                Group groupIndex = (Group) arrayGroup.get(i).get();
                TypicalCalculation typical = new TypicalCalculation(stationCount, arrayPhase, groupIndex);
                TypicalPhase typicalPhase = typical.run();
                arrayTypicalPath.addData(typicalPhase);
            }
            //System.out.println("Typicals");

            //блок проверки конца алгоритма
            EndChecking checkMethod = new EndChecking(ResolveForm.sigma, stationCount, arrayPhase, arrayTypicalPath, executorService);
            arrayPhase = checkMethod.run();
            check = checkMethod.check();


            count++;
//            System.out.println(count);
        } while (!check);
        //System.out.println("count=" + count);

        // объединяю станции в группы, дописываю координаты
        Merger merger = new Merger(stationCount, arrayGroup);
        ArrayList<String> groupAndCoordinates = merger.run();

//        System.out.println(System.currentTimeMillis() - start);

        //закрываем потоки
        executorService.shutdown();
        return groupAndCoordinates;
    }
}
