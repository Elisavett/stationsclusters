package net.codejava.controller;

import net.codejava.Resolve.Model.ResolveForm;
import net.codejava.Resolve.ModulesCalc;
import net.codejava.Resolve.PhaseCalc.FrequencyAnalysis;
import net.codejava.Resolve.PhaseCalc.WindowChart;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutionException;

@Controller
public class CalcModulesController {
    @GetMapping("/resolveModules")
    public String resolve(Model model) {
        ResolveForm.addAllToModel(model);
        ResolveForm.arrayPhase = null;
        ResolveForm.arrayTypical = null;
        ResolveForm.arrayGroup = null;
        return "resolve/resolveModules";
    }

    @GetMapping("/getFrequencyFragment")
    public String getFrequencyFragment() {
        return "fragments/frequencyAnalysisFragment";
    }
    @GetMapping("/getPhaseFragment")
    public String getPhaseFragment() {
        return "fragments/phaseFragment";
    }
    @GetMapping("/getClusterFragment")

    public String getClusterFragment(Model model) {
        if (ResolveForm.arrayPhase == null)
            model.addAttribute("phaseWarning", "Фазы/амплитуды не расчитаны. Данные из исходного файла будут использоваться, как данные фаз/амплитуд");
        return "fragments/clusterFragment";
    }
    @GetMapping("/getShowGrFragment")
    public String getShowGrFragment(Model model) {
        if (ResolveForm.arrayGroup == null)
            model.addAttribute("warning", "Нет данных для отображения. Необходимо выполнить расчеты последовательно");
        return "fragments/showGrFragment";
    }
    @GetMapping("/getClassesFragment")
    public String getClassesFragment(Model model) {
        if (ResolveForm.arrayTypical == null)
            model.addAttribute("warning", "Перед данным расчетом необходимо выполнить кластеризацию");
        return "fragments/classesFragment";
    }


    @GetMapping("/showChart")
    public String showChart(Model model,
                            @RequestParam(value = "isWindowManually") String isWindowManually) throws ExecutionException, InterruptedException {
        boolean assimetricWindow = false;
        if (Integer.parseInt(isWindowManually) == 2) assimetricWindow = true;
        WindowChart.getWindowsChartData(assimetricWindow);
        model.addAttribute("chartData", WindowChart.chartData);
        model.addAttribute("window", ResolveForm.windowDelta);
        return "additionals/windowChart";
    }

    @GetMapping("/frequencyAnalysis")
    public String frequencyAnalysis(Model model, @RequestParam Integer station){
        double[] temp = ResolveForm.TempData[station].clone();
        FrequencyAnalysis frequencyAnalysis = new FrequencyAnalysis(temp);
        ResolveForm.frequencyAnalysis = frequencyAnalysis.spectorCalculation();
        model.addAttribute("chartData", ResolveForm.frequencyAnalysis);
        model.addAttribute("X_name", "Отсчеты");
        model.addAttribute("Y_name", "Модуль спектра");
        model.addAttribute("title", "Частотный спектр (станция №" + station + ")" );
        model.addAttribute("id", "frequency");
        model.addAttribute("chartType", "corechart");
        return "additionals/frequencyChart";
    }

    @GetMapping("/temperatureChart")
    public String temperatureChart(Model model, @RequestParam Integer station){
        double[] temp = ResolveForm.TempData[station].clone();
        LinkedHashMap<String, Double> temperatures = new LinkedHashMap<>();
        double averageT = 0;
        for(int i = 0; i < temp.length; i++)
        {
            temperatures.put(String.valueOf(i), Math.round(100*temp[i])/100.);
            averageT += temp[i];
        }
        averageT = Math.round(100*averageT / temp.length)/100.;
        double sko_temp = 0;
        for (double v : temp) {
            sko_temp =+ Math.pow((v - averageT), 2);
        }
        sko_temp = Math.round(100*Math.sqrt(sko_temp / (temp.length - 1)))/100.;
        model.addAttribute("chartData", temperatures);
        model.addAttribute("additionalData", new double[]{averageT, averageT+sko_temp, averageT-sko_temp});
        model.addAttribute("X_name", "Отсчеты");
        model.addAttribute("Y_name", "Значение температуры");
        model.addAttribute("additionalY_names", new String[]{"Средняя", "+СКО", "-СКО"});
        model.addAttribute("title", "Температура" );
        model.addAttribute("id", "temperature");
        model.addAttribute("chartType", "line");
        return "additionals/frequencyChart";
    }
    @GetMapping("/SKOAnalysis")
    public String SKOAnalysis(Model model) {
        //double[] temp = ResolveForm.TempData[station].clone();
        ResolveForm.averageTemps = new double[ResolveForm.TempData.length];
        for(int i = 0; i < ResolveForm.TempData.length; i++)
        {
            ResolveForm.averageTemps[i] = 0;
            for(int j = 0; j < ResolveForm.TempData[i].length; j++){
                ResolveForm.averageTemps[i] += ResolveForm.TempData[i][j];
            }
            ResolveForm.averageTemps[i] = ResolveForm.averageTemps[i] / ResolveForm.TempData[i].length;
        }
        LinkedHashMap<Integer, Double> SKO = new LinkedHashMap<>();

        for(int i = 0; i < ResolveForm.averageTemps.length; i++)
        {
            double sko_temp = 0;
            for(int j = 0; j < ResolveForm.TempData[i].length; j++){
                sko_temp =+ Math.pow((ResolveForm.TempData[i][j] - ResolveForm.averageTemps[i]), 2);
            }
            sko_temp = sko_temp / (ResolveForm.averageTemps.length - 1);
            SKO.put(i+1, Math.round(100*Math.sqrt(sko_temp))/100.);
        }
        model.addAttribute("X_name", "Станции");
        model.addAttribute("Y_name", "Значение СКО");
        model.addAttribute("title", "Среднеквадратическое отклонение по станциям");
        model.addAttribute("id", "sko");
        model.addAttribute("chartData", SKO);
        model.addAttribute("chartType", "corechart");
        ResolveForm.SKO = SKO;
        return "additionals/frequencyChart";
    }

    @GetMapping("/countPhase")
    @ResponseStatus(value = HttpStatus.OK)
    public void countPhase(@RequestParam(value = "isForPhase", required = false) String isForPhase,
                           @RequestParam(value = "windowCounted", required = false) String windowCounted,
                           @RequestParam(value = "isWindowManually", required = false) String isWindowManually,
                           @RequestParam(value = "windowLeft", required = false) String windowLeft,
                           @RequestParam(value = "windowRight", required = false) String  windowRight) throws InterruptedException, ExecutionException {
        ResolveForm.isForPhases = Boolean.parseBoolean(isForPhase);
        //Отмечаем, что фаза считалась
        ResolveForm.isPhasesCounted = false;
        if(!windowCounted.equals("")){
            ResolveForm.windowLeft = ResolveForm.windowCenter - Integer.parseInt(windowCounted);
            ResolveForm.windowRight = ResolveForm.windowCenter + Integer.parseInt(windowCounted);
        }
        else {
            if ((windowLeft.equals("0.0") && windowRight.equals("0.0")) ||
                    (windowLeft.equals("") && windowRight.equals(""))) {

                    if (Integer.parseInt(isWindowManually) == 0) {
                        WindowChart.getWindowsChartData(false);
                        ResolveForm.windowLeft = ResolveForm.windowCenter - ResolveForm.windowDelta;
                        ResolveForm.windowRight = ResolveForm.windowCenter + ResolveForm.windowDelta;
                    }
                }
            else{
                ResolveForm.windowLeft = Double.parseDouble(windowLeft);
                ResolveForm.windowRight = Double.parseDouble(windowRight);
            }
        }
        ModulesCalc.PhaseAmplCalc();;
    }
    @GetMapping("/countClusters")
    @ResponseStatus(value = HttpStatus.OK)
    public void countClusters(@RequestParam(value = "corrUP", required = false) String corrUP,
                              @RequestParam(value = "corrDOWN", required = false) String corrDOWN,
                              @RequestParam(value = "isAccurate", required = false) String isAccurate,
                              @RequestParam(value = "sigma", required = false) String sigma
    ) throws InterruptedException, ExecutionException {
        ResolveForm.isAccurate = Boolean.parseBoolean(isAccurate);
        ResolveForm.corrUP = Double.parseDouble(corrUP);
        ResolveForm.corrDOWN = Double.parseDouble(corrDOWN);
        ResolveForm.sigma = sigma;
        ModulesCalc.ClustersCalc();
    }
    /*@GetMapping("/countClusters")
    @Transactional(timeout = 200)
    @ResponseStatus(value = HttpStatus.OK)
    public void countClusters(@RequestParam(value = "corrUP", required = false) String corrUP,
                              @RequestParam(value = "corrDOWN", required = false) String corrDOWN,
                              @RequestParam(value = "isAccurate", required = false) String isAccurate,
                              @RequestParam(value = "sigma", required = false) String sigma
    ) throws InterruptedException, ExecutionException {
        ResolveForm.isAccurate = Boolean.parseBoolean(isAccurate);
        ResolveForm.corrUP = Double.parseDouble(corrUP);
        ResolveForm.corrDOWN = Double.parseDouble(corrDOWN);
        ResolveForm.sigma = sigma;
        ClustersCalc1.calculation();
    }*/
    @GetMapping("/countClasses")
    @ResponseStatus(value = HttpStatus.OK)
    public void countClasses(@RequestParam(value = "classCoef", required = false) String classCoef) throws InterruptedException, ExecutionException{
        ResolveForm.classCoef = Double.parseDouble(classCoef);
        ModulesCalc.ClassesCalc();
    }
    @GetMapping("/toMap")
    @ResponseStatus(value = HttpStatus.OK)
    public void toMap(@RequestParam(value = "minGroupSize", required = false) String minGroupSize,
                      @RequestParam(value = "groupCross", required = false) String groupCross) throws InterruptedException, ExecutionException, IOException {
        ResolveForm.minGroupSize = Integer.parseInt(minGroupSize);
        ResolveForm.groupCross = groupCross.equals("true");
        ResolveForm.json = new ArrayList<>();
        ResolveForm.json.addAll(ModulesCalc.JsonCalc());
    }
    @RequestMapping("/showMap")
    public String showMap(Model model){
        model.addAttribute("json", ResolveForm.json);
        model.addAttribute("groupNum", ResolveForm.groupNum);
        return "map1";
    }




}
