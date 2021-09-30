package net.codejava.controller;

import java.util.*;
import java.util.concurrent.ExecutionException;

import net.codejava.Resolve.*;
import net.codejava.Resolve.Model.ResolveForm;
import net.codejava.Resolve.Modules.ModulesCalc;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/*
    Котроллер рассчетов
 */

@Controller
public class MapAllocationController {

    //Получение главное страницы
    @GetMapping("/")
    public String index() {
        return "index";
    }

    //Получение страницы рассчета по средним
    @GetMapping("/resolveAverage")
    public String resolveAverage() {
        return "resolve/resolveAverage";
    }

    //Получение страницы рассчета по функциям
    @GetMapping("/resolveFunction")
    public String resolveFunction() {
        return "resolve/resolveFunction";
    }

    //Рассчет по средним
    @PostMapping("/resolveAverage")
    public String resolveAverageToMap(Model model, @RequestParam(value = "fileTemp", required = false) MultipartFile fileTemp,
                                      @RequestParam(value = "fileCoordinates", required = false) MultipartFile fileCoordinates,
                                      @RequestParam String radius,
                                      @RequestParam(value = "cordsType", required = false) String cordsType,
                                      @RequestParam(value = "tempType", required = false) String tempType) {

        //Получение данных о температурах и координатах из файлов
        SplitInputFile.saveFilesInfo(Boolean.parseBoolean(tempType),
                Boolean.parseBoolean(cordsType),
                fileTemp, fileCoordinates);
        Double[] temps = new Double[ResolveForm.TempData.length];
        for (int j = 0; j < ResolveForm.TempData.length; j++) {
            temps[j] = ResolveForm.TempData[j][0];
        }
        //Рассчет по средним
        resolveAverage resolveAverage = new resolveAverage();
        ArrayList<String> json = resolveAverage.resolve(Double.parseDouble(radius), temps, ResolveForm.coordData);
        //Данные для отображения на карте
        model.addAttribute("json", json);
        return "map1";
    }

    //Отобразить станции на карте до рассчета
    @GetMapping("/showStationsOnMap")
    public String showStationsOnMap(Model model){
        ArrayList<String> json = ModulesCalc.JsonWOResolve();
        model.addAttribute("json", json);
        model.addAttribute("groupNum", 0);
        if(ResolveForm.resolveTime.equals("")) {
            ResolveForm.resolveTime = " ";
        }
        return "map1";
    }

    //Рассчитать и отобразить на карте
    @GetMapping("/showOnMap")
    public String toMap(Model model){
        try {
            ArrayList<String> json;

            //Расчет из предыдущих
            boolean isFromPrev = true;
            //Процесс рассчета
            ModulesCalc.PhaseAmplCalc();
            ModulesCalc.ClustersCalc(isFromPrev);
            if (ResolveForm.classification) {
                ModulesCalc.ClassesCalc();
            }
            json = ModulesCalc.JsonCalc();
            model.addAttribute("json", json);

            ResolveForm.calculateMapModel(model);

        } catch (NumberFormatException e) {
            String error = "Проверьте правильность данных";
            model.addAttribute("error", error);
            return "resolve/resolve";
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return "map1";
    }

    //Рассчитать по функциям
    @Transactional(timeout = 120)
    @PostMapping("/resolveFunction")
    public String resolveFunctionToMap(@RequestParam MultipartFile fileTemp,
                                       @RequestParam MultipartFile fileCoordinates,
                                       @RequestParam(value = "tempType", required = false) String tempType,
                                       @RequestParam(value = "cordsType", required = false) String cordsType,
                                       @RequestParam(value = "corr", required = false) String corr){

        ResolveForm.isPhasesCounted = true;
        ResolveForm.corrDOWN = Double.parseDouble(corr);
        SplitInputFile.saveFilesInfo(Boolean.parseBoolean(tempType),
                Boolean.parseBoolean(cordsType),
                fileTemp, fileCoordinates);
        return "redirect:/showOnMap";
    }

    //Получить доп. параметры в файле температур из формы рассчета
    @PostMapping("/recieveParams")
    public ResponseEntity<String> recieveParams(Model model, @RequestParam(value = "fileTemp") MultipartFile fileTemp,
                              @RequestParam(value = "fileCoordinates") MultipartFile fileCoordinates,
                              @RequestParam(value = "tempType", required = false) String tempType,
                              @RequestParam(value = "isModules", required = false) String isModules,
                              @RequestParam(value = "cordsType", required = false) String cordsType,
                              @RequestParam(value = "dataType", required = false) String dataType,
                              @RequestParam(value = "fileAdditionalParameters") String additionalParams,
                              @RequestParam(value = "periodStart", required = false) @DateTimeFormat(pattern="yyyy-MM-dd") Date periodStart,
                              @RequestParam(value = "periodEnd", required = false) @DateTimeFormat(pattern="yyyy-MM-dd") Date periodEnd) {
        //Сохранить параметры рассчета
        ResolveForm.saveParams(Boolean.parseBoolean(tempType), Boolean.parseBoolean(cordsType),
                fileTemp, fileCoordinates,
                Integer.parseInt(dataType),
                periodStart, periodEnd);
        ResolveForm.fileParams = additionalParams.split("delimiter");
        ResolveForm.addAllToModel(model);
        return ResponseEntity.ok().body("success");
    }

    //Проверить данные в файлах координат и температур
    @PostMapping("/check")
    public String check(Model model, @RequestParam(value = "fileTemp") MultipartFile fileTemp,
                        @RequestParam(value = "fileCoordinates") MultipartFile fileCoordinates,
                        @RequestParam(value = "tempType", required = false) String tempType,
                        @RequestParam(value = "isModules", required = false) String isModules,
                        @RequestParam(value = "cordsType", required = false) String cordsType,
                        @RequestParam(value = "dataType", required = false) String dataType,
                        @RequestParam(value = "fileAdditionalParameters") String additionalParams,
                        @RequestParam(value = "periodStart", required = false) @DateTimeFormat(pattern="yyyy-MM-dd") Date periodStart,
                        @RequestParam(value = "periodEnd", required = false) @DateTimeFormat(pattern="yyyy-MM-dd") Date periodEnd) {
        //Сохранить параметры рассчета
        ResolveForm.saveParams(Boolean.parseBoolean(tempType), Boolean.parseBoolean(cordsType),
                fileTemp, fileCoordinates,
                Integer.parseInt(dataType),
                periodStart, periodEnd);
        //Сохранить параметры файла температр
        ResolveForm.fileParams = additionalParams.split("delimiter");
        String compareDateMessage = ResolveForm.compareDates(periodStart, periodEnd);
        model.addAttribute("message", compareDateMessage);
        model.addAttribute("minGroupSize", ResolveForm.minGroupSize);
        ResolveForm.addAllToModel(model);
        if("true".equals(isModules)) return "resolve/resolveModules";
        else return "resolve/paramsForm";
    }

    //Получить форму рассчета без проверки данных
    @PostMapping("/withNoCheck")
    public String withNoCheck(Model model, @RequestParam(value = "fileTemp", required = false) MultipartFile fileTemp,
                        @RequestParam(value = "fileCoordinates", required = false) MultipartFile fileCoordinates,
                        @RequestParam(value = "tempType", required = false) String tempType,
                        @RequestParam(value = "isModules", required = false) String isModules,
                        @RequestParam(value = "cordsType", required = false) String cordsType,
                        @RequestParam(value = "dataType", required = false) String dataType,
                        @RequestParam(value = "periodStart", required = false) @DateTimeFormat(pattern="yyyy-MM-dd") Date periodStart,
                        @RequestParam(value = "periodEnd", required = false) @DateTimeFormat(pattern="yyyy-MM-dd") Date periodEnd){
        //Сохранить параметры рассчета
        ResolveForm.saveParams(Boolean.parseBoolean(tempType), Boolean.parseBoolean(cordsType),
                fileTemp, fileCoordinates,
                Integer.parseInt(dataType),
                periodStart, periodEnd);
        model.addAttribute("message", "Данные не прошли проверку");
        model.addAttribute("minGroupSize", ResolveForm.minGroupSize);
        ResolveForm.addAllToModel(model);
        if("true".equals(isModules)) return "resolve/resolveModules";
        else return "resolve/paramsForm";
    }

    //Карта
    @GetMapping("/map")
    public String map() {
        return "map1";
    }

    //Ошибка
    @GetMapping("/error")
    public String error() {
        return "index";
    }

    //Получение фрагментадля выбора границ окна фильтрации
    @GetMapping("/windowLimits")
    public String windowLimits() {
        return "fragments/windowLimitsFragment";
    }

    //Получить фрагмент с указанием доп. параметров в файле температур
    @GetMapping("/getAdditionalParamFragment")
    public String getAdditionalParamFragment(Model model,
                                             @RequestParam(value = "paramNumber") Integer paramNumber) {

        model.addAttribute("num", paramNumber);
        return "fragments/additionalStationCharacter";
    }



}
