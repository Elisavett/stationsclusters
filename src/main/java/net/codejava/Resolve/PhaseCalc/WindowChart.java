package net.codejava.Resolve.PhaseCalc;

import net.codejava.Resolve.Model.ResolveForm;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/*
    Класс для рассчета окна и преобразование данных для отображения графика
 */

public class WindowChart {
    public static LinkedHashMap<String, Double> chartData;
    public static void getWindowsChartData(boolean assimetric) throws InterruptedException, ExecutionException {
        int stationCount = ResolveForm.TempData.length;//количество станций
        LinkedHashMap<String, Double> graphData = new LinkedHashMap<>();

        int processors = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(processors);
        int wLeft = 1;
        List<Future<Integer>> arrayWindows;

        int wRight = (int)Math.ceil(ResolveForm.windowCenter*2)-1;
        //Рассчет окна фильтрации для каждой фазы
        List<WindowCalculation> windowCalculationTasks = new ArrayList<>();
        for (int i = 0; i < stationCount; i++) {
            double[] temp = ResolveForm.TempData[i].clone();
            WindowCalculation phaseCalculation = new WindowCalculation(temp, assimetric, wLeft, wRight, (int)Math.ceil(ResolveForm.windowCenter));
            windowCalculationTasks.add(phaseCalculation);
        }
        //Получаем рассчитаные для всех фаз окна фильтрации
        arrayWindows = executorService.invokeAll(windowCalculationTasks);
        ArrayList<Integer> windows = new ArrayList<>();
        for(int i = 0; i< stationCount; i++)
        {
            //округляем в большую сторону
            windows.add((int)Math.ceil(arrayWindows.get(i).get()));

        }
        //Сортировка
        windows.sort(Comparator.comparingInt(o -> o));
        int count = 1;
        int j = 0;
        if(assimetric) j = 1;
        int stationsNum = stationCount;
        int prevCount = 0;
        int bestWindow = windows.get(0);
        //Находим лучшее окно для фильтрации
        for(int i = 1; i< stationCount; i++)
        {
            if(!windows.get(i).equals(windows.get(i - 1))){
                graphData.put(String.valueOf(windows.get(i-1)), stationsNum*1.0/stationCount*100);
                stationsNum-=count;

                if(count > prevCount)
                {
                    prevCount = count;
                    bestWindow = windows.get(i - j);
                }
                count = 1;
            }
            else {
                count++;
            }
        }
        graphData.put(String.valueOf(windows.get(stationCount-1)), count*1.0/stationCount*100);

        ResolveForm.windowDelta = bestWindow;
        chartData = graphData;
    }

}
