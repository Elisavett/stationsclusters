package net.codejava.Resolve;

import net.codejava.Resolve.Model.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Start {
    public static ArrayList<String> run(MultipartFile fileTemp, MultipartFile fileCoord, double coefficientCorr, double window_left, double window_right, double sigma) throws IOException, ClassNotFoundException, InterruptedException, ExecutionException {
        //long start = System.currentTimeMillis();

        //получаем количество процессоров
        int processors = Runtime.getRuntime().availableProcessors();
        //создаем пул на количество процессоров
        ExecutorService executorService = Executors.newFixedThreadPool(processors);

        //File file = new File(fileTemp); // файл с температурами
        //String pathCoordinates = path + Coord;//Путь до координат

        ArrayData arrayTemp = new ArrayData();
        List<Future<Phase>> arrayPhase;
        ArrayData arrayTypicalPath = new ArrayData();
        List<Future<Group>> arrayGroup;
        List<Future<Corr>> arrayCorr;

        // загружаем все температуры из файла и разбиваем на файлы для каждой станции

        arrayTemp = SplitInputFile.ReadFromFileSplitting(fileTemp);
        /*BufferedReader br;
        try {

            String line;
            InputStream is = fileTemp.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));

            while ((line = br.readLine()) != null) {
                String[] splitedLine = line.split("\\s+");
                double[] data = new double[splitedLine.length - 1];
                for(int i =1; i<splitedLine.length - 1; i++)
                {
                    data[i] = Double.parseDouble(splitedLine[i]);
                }
                arrayTemp.add(data);
            }

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }*/


        //блок вычисления фазы
        //создаем лист задач
        List<PhaseCalculation> phaseCalculationTasks = new ArrayList<>();
        int stationCount = arrayTemp.size();//количество станций
        for (int i = 0; i < stationCount; i++) {
            Temp temp = (Temp)arrayTemp.getData(i);
            PhaseCalculation phaseCalculation = new PhaseCalculation(temp, window_left, window_right);
            phaseCalculationTasks.add(phaseCalculation);
        }
        //выполняем все задачи. главный поток ждет
        arrayPhase = executorService.invokeAll(phaseCalculationTasks);

        int count = 0;
        boolean check;
        do {
            //блок вычисления таблицы корреляции
            // создаем лист задач
            List<CorrelationCalculation> corrThreadTasks = new ArrayList<>();
            for (int i = 0; i < stationCount; i++) {
                CorrelationCalculation corrThread = new CorrelationCalculation(i, stationCount, arrayPhase);
                corrThreadTasks.add(corrThread);
            }
            //выполняем все задачи. главный поток ждет
            arrayCorr = executorService.invokeAll(corrThreadTasks);


            //блок выделения групп
            GroupAllocation allocationThread = new GroupAllocation(stationCount, coefficientCorr, arrayCorr, executorService);
            arrayGroup = allocationThread.run();

            //блок вычисления типовых фаз
            arrayTypicalPath.clear();
            for (int i = 0; i < stationCount; i++) {
                Group groupIndex = (Group) arrayGroup.get(i).get();
                TypicalCalculation typical = new TypicalCalculation(stationCount, arrayPhase, groupIndex);
                TypicalPhase typicalPhase = typical.run();
                arrayTypicalPath.addData(typicalPhase);
            }

            //блок проверки конца алгоритма
            EndChecking checkMethod = new EndChecking(sigma, stationCount, arrayPhase, arrayTypicalPath, executorService);
            arrayPhase = checkMethod.run();
            check = checkMethod.check();


            count++;
//            System.out.println(count);
        } while (!check);


        // объединяю станции в группы, дописываю координаты
        Merger merger = new Merger(stationCount, fileCoord, arrayGroup);
        ArrayList<String> groupAndCoordinates = merger.run();

//        System.out.println(System.currentTimeMillis() - start);

        //закрываем потоки
        executorService.shutdown();
        return groupAndCoordinates;
    }
}
