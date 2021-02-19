package net.codejava.controller;

import net.codejava.Resolve.ClassesCalc;
import net.codejava.Resolve.Clustering.ClustersCalc;
import net.codejava.Resolve.Model.Phase;
import net.codejava.Resolve.Model.ResolveForm;
import net.codejava.Resolve.PhaseCalc.PhaseAmplCalc;
import net.codejava.Resolve.PhaseCalc.WindowCalculation;
import net.codejava.Resolve.PhaseCalc.WindowChart;
import net.codejava.Resolve.Start;
import net.codejava.Resolve.toMap;
import org.apache.catalina.Cluster;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
    /*@GetMapping("/frequencyAnalysis")
    public String frequencyAnalysis(Model model) throws ExecutionException, InterruptedException {
        List<Future<Phase>> arrayWindows;
        int processors = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(processors);
        List<FrequencyAnalysis> windowCalculationTasks = new ArrayList<>();
        for (int i = 0; i < ResolveForm.TempData.length; i++) {
            double[] temp = ResolveForm.TempData[i].clone();
            FrequencyAnalysis frequencyAnalysis = new FrequencyAnalysis(temp);
            windowCalculationTasks.add(frequencyAnalysis);
        }
        //выполняем все задачи. главный поток ждет
        //arrayWindows = executorService.invokeAll(windowCalculationTasks);
        model.addAttribute("chartData", WindowChart.chartData);
        model.addAttribute("window", ResolveForm.windowDelta);
        return "additionals/windowChart";
    }*/

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
        if(windowCounted!=""){
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
    public void countClasses(@RequestParam(value = "classCoef", required = false) String classCoef) throws InterruptedException, ExecutionException, IOException {
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
    public ResponseEntity<String> downloadTypicals() throws IOException, ExecutionException, InterruptedException {

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
    public ResponseEntity<String> downloadGroups() throws IOException, ExecutionException, InterruptedException {

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
    public ResponseEntity<String> downloadJSON() throws IOException, ExecutionException, InterruptedException {

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
    public ResponseEntity<String> downloadGeographicCharacters() throws IOException, ExecutionException, InterruptedException {

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


}
