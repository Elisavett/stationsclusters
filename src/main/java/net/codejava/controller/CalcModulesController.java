package net.codejava.controller;

import net.codejava.Resolve.ClassesCalc;
import net.codejava.Resolve.Clustering.ClustersCalc;
import net.codejava.Resolve.Model.ResolveForm;
import net.codejava.Resolve.PhaseCalc.FrequencyAnalysis;
import net.codejava.Resolve.PhaseCalc.PhaseAmplCalc;
import net.codejava.Resolve.PhaseCalc.WindowChart;
import net.codejava.Resolve.toMap;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.Charset;
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
        sko_temp = Math.round(100*sko_temp / (temp.length - 1))/100.;
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
            SKO.put(i+1, Math.round(100*sko_temp)/100.);
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
        PhaseAmplCalc.calculation();
    }
    @GetMapping("/countClusters")
    @ResponseStatus(value = HttpStatus.OK)
    public void countClusters(@RequestParam(value = "corr", required = false) String corr,
                           @RequestParam(value = "isAccurate", required = false) String isAccurate,
                           @RequestParam(value = "sigma", required = false) String sigma
                           ) throws InterruptedException, ExecutionException {
        ResolveForm.isAccurate = Boolean.parseBoolean(isAccurate);
        ResolveForm.corr = Double.parseDouble(corr);
        ResolveForm.sigma = sigma;
        ClustersCalc.calculation();
    }
    @GetMapping("/countClasses")
    @ResponseStatus(value = HttpStatus.OK)
    public void countClasses(@RequestParam(value = "classCoef", required = false) String classCoef) throws InterruptedException, ExecutionException{
        ResolveForm.classCoef = Double.parseDouble(classCoef);
        ClassesCalc.calculation();
    }
    @GetMapping("/toMap")
    @ResponseStatus(value = HttpStatus.OK)
    public void toMap(@RequestParam(value = "minGroupSize", required = false) String minGroupSize,
                      @RequestParam(value = "groupCross", required = false) String groupCross) throws InterruptedException, ExecutionException, IOException {
        ResolveForm.minGroupSize = Integer.parseInt(minGroupSize);
        ResolveForm.groupCross = groupCross.equals("true");
        ResolveForm.json = new ArrayList<>();
        ResolveForm.json.addAll(toMap.getGroups());
    }
    @RequestMapping("/showMap")
    public String showMap(Model model){
        model.addAttribute("json", ResolveForm.json);
        model.addAttribute("groupNum", ResolveForm.groupNum);
        return "map1";
    }

    @RequestMapping("/downloadTypicals")
    public ResponseEntity<String> downloadTypicals() throws ExecutionException, InterruptedException {

        MediaType mediaType = new MediaType("text", "plain", Charset.defaultCharset());
        String stringPhase = "";
        for (int i = 0; i < ResolveForm.arrayTypical.get(0).get().getArray().length; i++) {
            String output = "";
            for (int j = 0; j < ResolveForm.arrayTypical.size(); j++) {
                output += ResolveForm.arrayTypical.get(j).get().getArray()[i] + " ";
            }
            stringPhase += (output + "\n");
        }

        return ResponseEntity.ok()
                // Content-Disposition
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + "typicals.txt")
                // Content-Type
                .contentType(mediaType)
                // Contet-Length
                .contentLength(stringPhase.length()) //
                .body(stringPhase);
    }
    @RequestMapping("/downloadGroups")
    public ResponseEntity<String> downloadGroups() throws ExecutionException, InterruptedException {

        MediaType mediaType = new MediaType("text", "plain", Charset.defaultCharset());
        String stringPhase = "";
        for (int i = 0; i < ResolveForm.arrayGroup.get(0).get().getArray().length; i++) {
            String output = "";
            for (int j = 0; j < ResolveForm.arrayGroup.size(); j++) {
                output += ResolveForm.arrayGroup.get(j).get().getArray()[i] + " ";
            }
            stringPhase += (output + "\n");
        }

        return ResponseEntity.ok()
                // Content-Disposition
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + "groups.txt")
                // Content-Type
                .contentType(mediaType)
                // Contet-Length
                .contentLength(stringPhase.length()) //
                .body(stringPhase);
    }
    @RequestMapping("/downloadJSON")
    public ResponseEntity<String> downloadJSON(){

        MediaType mediaType = new MediaType("text", "plain", Charset.defaultCharset());
        String stringPhase = "";
        for (int i = 0; i < ResolveForm.json.size(); i++) {
            stringPhase += (ResolveForm.json.get(i) + "\n");
        }

        return ResponseEntity.ok()
                // Content-Disposition
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + "groups.txt")
                // Content-Type
                .contentType(mediaType)
                // Contet-Length
                .contentLength(stringPhase.length()) //
                .body(stringPhase);
    }
    @RequestMapping("/downloadGeographicCharacters")
    public ResponseEntity<String> downloadGeographicCharacters(){

        MediaType mediaType = new MediaType("text", "plain", Charset.defaultCharset());
        String stringPhase = "1.номер_группы 2.широта_центра 3.долгота_центра 4.макс_широта 5.мин_широта 6.макс_долгота 7.мин_долгота  \n";
        for (int i = 0; i < ResolveForm.geoChars.size(); i++) {
            stringPhase += ((int)ResolveForm.geoChars.get(i)[0] + " ");
            for(int j = 1; j < ResolveForm.geoChars.get(i).length; j++)
            {
                stringPhase += (ResolveForm.geoChars.get(i)[j] + " ");
            }
            stringPhase += "\n";
        }

        return ResponseEntity.ok()
                // Content-Disposition
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + "groupGeoCharacteristics.txt")
                // Content-Type
                .contentType(mediaType)
                // Contet-Length
                .contentLength(stringPhase.length()) //
                .body(stringPhase);
    }
    @RequestMapping("/downloadFrequency")
    public ResponseEntity<String> downloadFrequency(){

        MediaType mediaType = new MediaType("text", "plain", Charset.defaultCharset());
        String stringPhase = "";

        for(int i = 1; i< ResolveForm.frequencyAnalysis.size(); i++) {
            stringPhase += i + " " + ResolveForm.frequencyAnalysis.get(String.valueOf(i));
            stringPhase += "\n";
        }


        return ResponseEntity.ok()
                // Content-Disposition
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + "frequencyAnalysis.txt")
                // Content-Type
                .contentType(mediaType)
                // Contet-Length
                .contentLength(stringPhase.length()) //
                .body(stringPhase);
    }
    @RequestMapping("/downloadSKO")
    public ResponseEntity<String> downloadSKO(){

        MediaType mediaType = new MediaType("text", "plain", Charset.defaultCharset());
        String stringPhase = "";

        for(int i = 1; i< ResolveForm.SKO.size(); i++) {
            stringPhase += i + " " + ResolveForm.SKO.get(String.valueOf(i));
            stringPhase += "\n";
        }


        return ResponseEntity.ok()
                // Content-Disposition
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + "SKO.txt")
                // Content-Type
                .contentType(mediaType)
                // Contet-Length
                .contentLength(stringPhase.length()) //
                .body(stringPhase);
    }


}
