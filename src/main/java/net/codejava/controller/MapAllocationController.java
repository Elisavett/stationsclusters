package net.codejava.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import net.codejava.Resolve.*;
import net.codejava.Resolve.Model.ResolveForm;
import net.codejava.Resolve.PhaseCalc.WindowChart;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author liza9
 */
@Controller
public class MapAllocationController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/error")
    public String error() {
        return "index";
    }

    @GetMapping("/resolveAverage")
    public String resolveAverage() {
        return "resolve/resolveAverage";
    }
    @PostMapping("/resolveAverage")
    public String resolveAverageToMap(Model model, @RequestParam(value = "fileTemp", required = false) MultipartFile fileTemp,
                                      @RequestParam(value = "fileCoordinates", required = false) MultipartFile fileCoordinates,
                                      @RequestParam String radius,
                                      @RequestParam(value = "cordsType", required = false) String cordsType,
                                      @RequestParam(value = "tempType", required = false) String tempType) {

        saveFilesInfo(Boolean.parseBoolean(tempType),
                Boolean.parseBoolean(cordsType),
                fileTemp, fileCoordinates);
        Double[] temps = new Double[ResolveForm.TempData.length];
        for (int j = 0; j < ResolveForm.TempData.length; j++) {
            temps[j] = ResolveForm.TempData[j][0];
        }
        resolveAverage resolveAverage = new resolveAverage();
        ArrayList<String> json = resolveAverage.resolve(Double.parseDouble(radius), temps, ResolveForm.coordData);
        model.addAttribute("json", json);
        return "map1";
    }

    @GetMapping("/resolveFunction")
    public String resolveFunction() {
        return "resolve/resolveFunction";
    }
    @PostMapping("/map")
    @Transactional(timeout = 120)
    public String  map(Model model,
                      @RequestParam(value = "corr", required = false) String corr,
                      @RequestParam(value = "windowLeft", required = false) String windowLeft,
                      @RequestParam(value = "windowRight", required = false) String  windowRight,
                      @RequestParam(value = "sigma", required = false) String sigma,
                      @RequestParam(value = "isForPhase", required = false) String isForPhase,
                      @RequestParam(value = "classification", required = false) String classification,
                      @RequestParam(value = "isWindowManually", required = false) String isWindowManually,
                      @RequestParam(value = "isAccurate", required = false) String isAccurate,
                      @RequestParam(value = "windowCounted", required = false) Integer windowCounted,
                      @RequestParam(value = "minGroupSize", required = false) Integer minGroupSize,
                      @RequestParam(value = "classCoef", required = false) String classCoef, @RequestParam(value = "groupCross", required = false) String groupCross) throws ExecutionException, InterruptedException {

        ResolveForm.isPhasesCounted = false;
        ResolveForm.isAccurate = Boolean.parseBoolean(isAccurate);
        ResolveForm.isForPhases = Boolean.parseBoolean(isForPhase);
        ResolveForm.groupCross = "true".equals(groupCross);
        ResolveForm.classification = "true".equals(classification);
        if(ResolveForm.classification){
            ResolveForm.classCoef = Double.parseDouble(classCoef);
        }
        if(windowCounted!=null){
            ResolveForm.windowLeft = ResolveForm.windowCenter - windowCounted;
            ResolveForm.windowRight = ResolveForm.windowCenter + windowCounted;
        }
        else {
            ResolveForm.minGroupSize = minGroupSize;
            ResolveForm.corrDOWN = Double.parseDouble(corr);
            ResolveForm.sigma = sigma;
            int manuallyWindow = Integer.parseInt(isWindowManually);

            if ((windowLeft.equals("0.0") && windowRight.equals("0.0")) ||
                    (windowLeft.equals("") && windowRight.equals(""))) {
                boolean asymmetricWindow = false;
                if(manuallyWindow == 2) asymmetricWindow=true;
                WindowChart.getWindowsChartData(asymmetricWindow);
                if (manuallyWindow > 0) {
                    model.addAttribute("chartData", WindowChart.chartData);
                    model.addAttribute("window", ResolveForm.windowDelta);
                    return "resolve/chartForPlaneCalc";
                } else {
                    if (manuallyWindow == 0) {
                        ResolveForm.windowLeft = ResolveForm.windowCenter - ResolveForm.windowDelta;
                        ResolveForm.windowRight = ResolveForm.windowCenter + ResolveForm.windowDelta;
                    }
                }
            }
            else{
                ResolveForm.windowLeft = Double.parseDouble(windowLeft);
                ResolveForm.windowRight = Double.parseDouble(windowRight);
            }
        }
        return "redirect:/showOnMap";
    }
    @GetMapping("/showOnMap")
    public String toMap(Model model){
        ArrayList<String> json = new ArrayList<>();
        try {
            Start start = new Start();
            json = start.run();
        } catch (NumberFormatException e) {
            String error = "Проверьте правильность данных";
            model.addAttribute("error", error);
            return "resolve/resolve";
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        model.addAttribute("json", json);
        model.addAttribute("groupNum", ResolveForm.groupNum);
        return "map1";
    }

    @Transactional(timeout = 120)
    @PostMapping("/resolveFunction")
    public String resolveFunctionToMap(@RequestParam MultipartFile fileTemp,
                                       @RequestParam MultipartFile fileCoordinates,
                                       @RequestParam(value = "tempType", required = false) String tempType,
                                       @RequestParam(value = "cordsType", required = false) String cordsType,
                                       @RequestParam(value = "corr", required = false) String corr){

        ResolveForm.isPhasesCounted = true;
        ResolveForm.corrDOWN = Double.parseDouble(corr);
        saveFilesInfo(Boolean.parseBoolean(tempType),
                Boolean.parseBoolean(cordsType),
                fileTemp, fileCoordinates);
        return "redirect:/showOnMap";
    }
    @GetMapping("/resolveHistory")
    public String resolveHistory(Model model) {
        model.addAttribute("phases", ResolveForm.arrayPhase != null);
        return "additionals/resolveHistory";
    }


    @GetMapping("/resolve")
    public String resolve(Model model) {
        ResolveForm.addAllToModel(model);
        return "resolve/resolve";
    }
    @PostMapping("/check")
    public String check(Model model, @RequestParam(value = "fileTemp", required = false) MultipartFile fileTemp,
                        @RequestParam(value = "fileCoordinates", required = false) MultipartFile fileCoordinates,
                        @RequestParam(value = "tempType", required = false) String tempType,
                        @RequestParam(value = "isModules", required = false) String isModules,
                        @RequestParam(value = "cordsType", required = false) String cordsType,
                        @RequestParam(value = "dataType", required = false) String dataType,
                        @RequestParam(value = "periodStart", required = false) @DateTimeFormat(pattern="yyyy-MM-dd") Date periodStart,
                        @RequestParam(value = "periodEnd", required = false) @DateTimeFormat(pattern="yyyy-MM-dd") Date periodEnd) {
        saveParamsFromCheck(Boolean.parseBoolean(tempType), Boolean.parseBoolean(cordsType),
                fileTemp, fileCoordinates,
                Integer.parseInt(dataType),
                periodStart, periodEnd);

        String compareDateMessage = compareDates(periodStart, periodEnd);
        model.addAttribute("message", compareDateMessage);
        model.addAttribute("minGroupSize", ResolveForm.minGroupSize);
        ResolveForm.addAllToModel(model);
        if("true".equals(isModules)) return "resolve/resolveModules";
        else return "resolve/resolve";
    }
    @PostMapping("/withNoCheck")
    public String withNoCheck(Model model, @RequestParam(value = "fileTemp", required = false) MultipartFile fileTemp,
                        @RequestParam(value = "fileCoordinates", required = false) MultipartFile fileCoordinates,
                        @RequestParam(value = "tempType", required = false) String tempType,
                        @RequestParam(value = "isModules", required = false) String isModules,
                        @RequestParam(value = "cordsType", required = false) String cordsType,
                        @RequestParam(value = "dataType", required = false) String dataType,
                        @RequestParam(value = "periodStart", required = false) @DateTimeFormat(pattern="yyyy-MM-dd") Date periodStart,
                        @RequestParam(value = "periodEnd", required = false) @DateTimeFormat(pattern="yyyy-MM-dd") Date periodEnd){

        saveParamsFromCheck(Boolean.parseBoolean(tempType), Boolean.parseBoolean(cordsType),
                fileTemp, fileCoordinates,
                Integer.parseInt(dataType),
                periodStart, periodEnd);
        model.addAttribute("message", "Данные не прошли проверку");
        model.addAttribute("minGroupSize", ResolveForm.minGroupSize);
        ResolveForm.addAllToModel(model);
        if("true".equals(isModules)) return "resolve/resolveModules";
        else return "resolve/resolve";
    }

    @GetMapping("/map")
    public String map() {
        return "map1";
    }
    private void saveParamsFromCheck(boolean tempType, boolean cordsType,
                                     MultipartFile fileTemp, MultipartFile fileCoordinates,
                                     int dataType,
                                     Date periodStart, Date periodEnd){
        saveFilesInfo(tempType, cordsType, fileTemp, fileCoordinates);
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        ResolveForm.periodStart = formatter.format(periodStart);
        ResolveForm.periodEnd = formatter.format(periodEnd);
        ResolveForm.dataType = dataType;

        ResolveForm.windowCenter = ResolveForm.TempData[0].length / ResolveForm.dataType;
    }
    private void saveFilesInfo(boolean tempType, boolean cordsType,
                               MultipartFile fileTemp, MultipartFile fileCoordinates) {
        ResolveForm.tempsIsStationsOnY = tempType;
        ResolveForm.coordsIsStationsOnY = cordsType;
        if (!"".equals(fileTemp.getOriginalFilename())) {
            ResolveForm.TempData = new double[(int) fileTemp.getSize()][];
            ResolveForm.TempData = SplitInputFile.ReadFromFileSplitting(fileTemp, 't');
            ResolveForm.tempFileName = fileTemp.getOriginalFilename();
        }
        if (!"".equals(fileCoordinates.getOriginalFilename())) {
            ResolveForm.coordData = new double[(int) fileCoordinates.getSize()][];
            ResolveForm.coordData = SplitInputFile.ReadFromFileSplitting(fileCoordinates, 'c');
            ResolveForm.coordFileName = fileCoordinates.getOriginalFilename();
        }
    }
    private String compareDates(Date periodStart, Date periodEnd){
        Calendar date1 = Calendar.getInstance();
        Calendar date2 = Calendar.getInstance();
        date1.setTime(periodStart);
        date2.setTime(periodEnd);
        //Добавляем 1 день, чтобы сопоставить даты
        date2.add(Calendar.DATE, 1);

        int yearsBetween;
        String message;
        //Сопоставляем номер месяца и день месяца
        if(date1.get(Calendar.DAY_OF_MONTH) == date2.get(Calendar.DAY_OF_MONTH) && date1.get(Calendar.MONTH) == date2.get(Calendar.MONTH))
        {
            //Находим количество лет между датами
            yearsBetween = date2.get(Calendar.YEAR) - date1.get(Calendar.YEAR);

            //Если совпадает с подсчитанным центом окна - период указан верно
            if(yearsBetween == ResolveForm.windowCenter){
                int period1 = ResolveForm.TempData.length;
                int period2 = ResolveForm.coordData[0].length;
                if(period1 == period2) {
                    message = "Период выбран верно";
                }
                else
                {
                    message = "Количество станций в файлах не совпадает";
                }
            }
            else{
                message = "Указанный период (" + yearsBetween +") не совпадает с количеством данных в файле с температурами (" + (int)ResolveForm.windowCenter + ")";
            }
        }
        else
        {
            message = "Неверно указан период. Данные должны быть выбраны за ровное количество лет (Пример: 01.01.1955-31.12.2017)";
        }
        return message;
    }

}
