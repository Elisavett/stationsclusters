package net.codejava.controller;

import net.codejava.Resolve.Model.ResolveForm;
import net.codejava.Resolve.Modules.ModulesCalc;
import net.codejava.Resolve.PhaseCalc.WindowChart;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

@Controller
public class CalcModulesController {
    @GetMapping("/resolveModules")
    public String resolve(Model model) {
        ResolveForm.addAllToModel(model);
        ResolveForm.arrayPhase = null;
        ResolveForm.arrayTypical = null;
        ResolveForm.arrayGroup = null;
        ResolveForm.isPhasesCounted = true;
        model.addAttribute("isModules", "true");
        return "resolve/resolve";
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
        model.addAttribute("windowCenter", ResolveForm.windowCenter);
        return "additionals/windowChart";
    }
    @GetMapping("/countPhase")
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<String> countPhase(@RequestParam(value = "isDelta", required = false) String isDelta,
                                             @RequestParam(value = "isForPhase", required = false) String isForPhase,
                                             @RequestParam(value = "windowCounted", required = false) String windowCounted,
                                             @RequestParam(value = "isWindowManually", required = false) String isWindowManually,
                                             @RequestParam(value = "windowLeft", required = false) String windowLeft,
                                             @RequestParam(value = "windowRight", required = false) String  windowRight) throws InterruptedException, ExecutionException {
        //Рассчет по фазе или по амплитуде
        ResolveForm.isForPhases = Boolean.parseBoolean(isForPhase);
        //Отмечаем, что фаза считалась
        ResolveForm.isPhasesCounted = false;
        //Если окно уже было подсчитано (через вывод графика)
        if(windowCounted != null){
            //Устанавливаем граныцы окна
            if(Boolean.parseBoolean(isDelta)) {
                setWindowLimits(Integer.parseInt(windowCounted));
            }
            else{
                ResolveForm.windowLeft = Double.parseDouble(windowLeft);
                ResolveForm.windowRight = Double.parseDouble(windowRight);
            }

        }
        //Окно не подсчитано (автоматический расчет или задание границ окна вручную)
        else {
            //Если окно вручную не задано
            if (windowLeft == null && windowRight == null) {
                    //Выбран режим автоматического расчета
                    if (Integer.parseInt(isWindowManually) == 0) {
                        //Считаем симметричное окно
                        WindowChart.getWindowsChartData(false);
                        //Устанавливаем границы окна
                        setWindowLimits(ResolveForm.windowDelta);
                    }
                }
            //Иначе - окно задано вручную
            else{
                double left = Double.parseDouble(windowLeft);
                double right = Double.parseDouble(windowRight);
                ResolveForm.windowCenter = (left + right)/2;
                setWindowLimits((int)(ResolveForm.windowCenter - left));
            }
        }
        ModulesCalc.PhaseAmplCalc();
        String output = "Окно: " + (int)ResolveForm.windowLeft + " - " + (int)ResolveForm.windowRight + "; ";
        output += "Несущая: " + (int)ResolveForm.windowCenter;
        return ResponseEntity.ok().body(output);
    }
    private void setWindowLimits(int delta){
        ResolveForm.windowLeft = ResolveForm.windowCenter - delta;
        ResolveForm.windowRight = ResolveForm.windowCenter + delta;
    }
    @GetMapping("/countClusters")
    @ResponseStatus(value = HttpStatus.OK)
    public void countClusters(@RequestParam(value = "corrUP", required = false) String corrUP,
                              @RequestParam(value = "corrDOWN", required = false) String corrDOWN,
                              @RequestParam(value = "isAccurate", required = false) String isAccurate,
                              @RequestParam(value = "sigma", required = false) String sigma,
                              String isFromPrev
    ) throws InterruptedException, ExecutionException {
        ResolveForm.isAccurate = Boolean.parseBoolean(isAccurate);
        ResolveForm.corrUP = Double.parseDouble(corrUP);
        ResolveForm.corrDOWN = Double.parseDouble(corrDOWN);
        ResolveForm.sigma = sigma;
        ModulesCalc.ClustersCalc(Boolean.parseBoolean(isFromPrev));
    }
    @GetMapping("/countClasses")
    @ResponseStatus(value = HttpStatus.OK)
    public void countClasses(@RequestParam(value = "classCoef", required = false) String classCoef) throws InterruptedException, ExecutionException{
        ResolveForm.classCoef = Double.parseDouble(classCoef);
        ModulesCalc.ClassesCalc();
    }
    @GetMapping("/toMap")
    @ResponseStatus(value = HttpStatus.OK)
    public void toMap(@RequestParam(value = "minGroupSize", required = false) String minGroupSize,
                      @RequestParam(value = "groupCross", required = false) String groupCross) {
        ResolveForm.resolveTime = "";
        ResolveForm.minGroupSize = Integer.parseInt(minGroupSize);
        ResolveForm.groupCross = groupCross.equals("true");
        ResolveForm.json = new ArrayList<>();
        ResolveForm.json.addAll(ModulesCalc.JsonCalc());
    }
    @RequestMapping("/showMap")
    public String showMap(Model model){
        model.addAttribute("json", ResolveForm.json);
        model.addAttribute("groupNum", ResolveForm.groupNum);
        if(ResolveForm.resolveTime.equals("")) {
            DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh.mm.ss");
            ResolveForm.resolveTime = dateFormat.format(Calendar.getInstance().getTime());
        }
        model.addAttribute("resolveTime", "Расчет: " + ResolveForm.resolveTime);
        model.addAttribute("corr", "Коэф. корреляции: " + ResolveForm.corrDOWN + " - " + ResolveForm.corrUP);
        model.addAttribute("window", "Окно: " + ResolveForm.windowLeft + " - " + ResolveForm.windowRight);
        model.addAttribute("calcPeriod", "Рассчетный период: " + ResolveForm.periodStart + " - " + ResolveForm.periodEnd);
        return "map1";
    }




}
