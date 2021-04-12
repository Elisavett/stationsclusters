package net.codejava.controller;

import net.codejava.Resolve.Model.ResolveForm;
import net.codejava.Resolve.Modules.ModulesCalc;
import net.codejava.Resolve.PhaseCalc.WindowChart;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
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
    @GetMapping("/countPhase")
    @ResponseStatus(value = HttpStatus.OK)
    public void countPhase(@RequestParam(value = "isForPhase", required = false) String isForPhase,
                           @RequestParam(value = "windowCounted", required = false) String windowCounted,
                           @RequestParam(value = "isWindowManually", required = false) String isWindowManually,
                           @RequestParam(value = "windowLeft", required = false) String windowLeft,
                           @RequestParam(value = "windowRight", required = false) String  windowRight) throws InterruptedException, ExecutionException {
        //Рассчет по фазе или по амплитуде
        ResolveForm.isForPhases = Boolean.parseBoolean(isForPhase);
        //Отмечаем, что фаза считалась
        ResolveForm.isPhasesCounted = false;
        //Если окно уже было подсчитано (через вывод графика)
        if(!windowCounted.equals("")){
            //Устанавливаем граныцы окна
            setWindowLimits(Integer.parseInt(windowCounted));
        }
        //Окно не подсчитано (автоматический расчет или задание границ окна вручную)
        else {
            //Если окно вручную не задано
            if ((windowLeft.equals("0.0") && windowRight.equals("0.0")) ||
                    (windowLeft.equals("") && windowRight.equals(""))) {
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
                              @RequestParam(value = "sigma", required = false) String sigma
    ) throws InterruptedException, ExecutionException {
        ResolveForm.isAccurate = Boolean.parseBoolean(isAccurate);
        ResolveForm.corrUP = Double.parseDouble(corrUP);
        ResolveForm.corrDOWN = Double.parseDouble(corrDOWN);
        ResolveForm.sigma = sigma;
        ModulesCalc.ClustersCalc();
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
