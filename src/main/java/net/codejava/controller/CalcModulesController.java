package net.codejava.controller;

import net.codejava.Resolve.Model.ResolveForm;
import net.codejava.Resolve.Modules.ModulesCalc;
import net.codejava.Resolve.PhaseCalc.WindowChart;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/*
    Котроллер для модульного рассчета
 */
@Controller
public class CalcModulesController {
    //Рассчет фазы / амплитуды
    @GetMapping("/countPhase")
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<String> countPhase(@RequestParam(value = "isVisual", required = false) String isVisual,
                                             @RequestParam(value = "isDelta", required = false) String isDelta,
                                             @RequestParam(value = "isForPhase", required = false) String isForPhase,
                                             @RequestParam(value = "windowCounted", required = false) String windowCounted,
                                             @RequestParam(value = "isWindowManually", required = false) String isWindowManually,
                                             @RequestParam(value = "windowLeft", required = false) String windowLeft,
                                             @RequestParam(value = "windowRight", required = false) String  windowRight,
                                             @RequestParam(value = "toZero", required = false) String  toZero) throws InterruptedException, ExecutionException {
        //Если расчет в визуальном виде - нет параметров
        if(Boolean.parseBoolean(isVisual)){
            isWindowManually = "0";
        }
        else {
            //Рассчет по фазе или по амплитуде
            ResolveForm.isForPhases = Boolean.parseBoolean(isForPhase);
            //Выполнение дотяжки
            ResolveForm.phaseToZero = Boolean.parseBoolean(toZero);
            //Отмечаем, что фаза считалась
            ResolveForm.isPhasesCounted = false;
        }
        //Если окно уже было подсчитано (через вывод графика)
        if(windowCounted != null){
            //Устанавливаем граныцы окна
            if(Boolean.parseBoolean(isDelta)) {
                ResolveForm.setWindowLimits(Integer.parseInt(windowCounted));
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
                        ResolveForm.setWindowLimits(ResolveForm.windowDelta);
                    }
                }
            //Иначе - окно задано вручную
            else{
                double left = Double.parseDouble(windowLeft);
                double right = Double.parseDouble(windowRight);
                ResolveForm.windowCenter = (left + right)/2;
                ResolveForm.setWindowLimits((int)(ResolveForm.windowCenter - left));
            }
        }
        //Расчета фазы или амплитуды
        ModulesCalc.PhaseAmplCalc();
        //Строка вывода окно и несущей на форме по завершении рассчета
        String output = "Окно: " + (int)ResolveForm.windowLeft + " - " + (int)ResolveForm.windowRight + "; ";
        output += "Несущая: " + (int)ResolveForm.windowCenter;
        //Передача данных по завершении рассчета
        return ResponseEntity.ok().body(output);
    }

    //Рассчет кластеризации
    @GetMapping("/countClusters")
    @ResponseStatus(value = HttpStatus.OK)
    public void countClusters(@RequestParam(value = "isVisual", required = false) String isVisual,
                              @RequestParam(value = "corrUP", required = false) String corrUP,
                              @RequestParam(value = "corrDOWN", required = false) String corrDOWN,
                              @RequestParam(value = "isAccurate", required = false) String isAccurate,
                              @RequestParam(value = "sigma", required = false) String sigma,
                              @RequestParam(value = "isFromPrev", required = false) String isFromPrev
    ) throws InterruptedException, ExecutionException {
        //Если рассчет не в визуальном режиме - запоминаем параметры рассчета
        if(!Boolean.parseBoolean(isVisual)) {
            ResolveForm.isAccurate = Boolean.parseBoolean(isAccurate);
            ResolveForm.corrUP = Double.parseDouble(corrUP);
            ResolveForm.corrDOWN = Double.parseDouble(corrDOWN);
            ResolveForm.sigma = sigma;
        }
        //Если в визуальном - нет параметров
        else
        {
            //Рассчитываем фазу из предыдущих по умолчанию
            isFromPrev = "true";
        }
        //Рассчит фазы / амплитуды
        ModulesCalc.ClustersCalc(Boolean.parseBoolean(isFromPrev));
    }

    //Рассчет классификации
    @GetMapping("/countClasses")
    @ResponseStatus(value = HttpStatus.OK)
    public void countClasses(@RequestParam(value = "isVisual", required = false) String isVisual,
                             @RequestParam(value = "classCoefDOWN", required = false) String classCoefDOWN,
                             @RequestParam(value = "classCoefUP", required = false) String classCoefUP) throws InterruptedException, ExecutionException{
        //Если рассчет не в визуальном режиме - запоминаем параметры рассчета
        if(!Boolean.parseBoolean(isVisual)) {
            ResolveForm.classCoefDOWN = Double.parseDouble(classCoefDOWN);
            ResolveForm.classCoefUP = Double.parseDouble(classCoefUP);
        }
        //Рассчет классификации
        ModulesCalc.ClassesCalc();
    }

    //Подготовка данных для отображения на карте
    @GetMapping("/toMap")
    @ResponseStatus(value = HttpStatus.OK)
    public void toMap(@RequestParam(value = "isVisual", required = false) String isVisual,
                      @RequestParam(value = "minGroupSize", required = false) String minGroupSize,
                      @RequestParam(value = "groupCross", required = false) String groupCross) {
        ResolveForm.resolveTime = "";
        //Если рассчет не в визуальном режиме - запоминаем параметры рассчета
        if(!Boolean.parseBoolean(isVisual)) {
            ResolveForm.minGroupSize = Integer.parseInt(minGroupSize);
            ResolveForm.groupCross = groupCross.equals("true");
        }
        ResolveForm.json = new ArrayList<>();
        ResolveForm.json.addAll(ModulesCalc.JsonCalc());
    }

    //Отображение данных на карте
    @RequestMapping("/showMap")
    public String showMap(Model model){
        model.addAttribute("json", ResolveForm.json);
        ResolveForm.calculateMapModel(model);
        return "map1";
    }
}
