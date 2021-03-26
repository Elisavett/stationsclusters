package net.codejava.controller;

import net.codejava.Resolve.ClassesCalc;
import net.codejava.Resolve.Clustering.ClustersCalc;
import net.codejava.Resolve.Clustering.ClustersCalc1;
import net.codejava.Resolve.Model.GroupLine;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
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
    ClustersCalc clustersCalc = new ClustersCalc();
    @GetMapping("/countClustersStart")
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
        clustersCalc = new ClustersCalc();
        clustersCalc.calculationStart();
    }
    @GetMapping("/countClusters20sec")
    public ResponseEntity<Boolean> countClusters20sec() throws InterruptedException, ExecutionException {
        return ResponseEntity.ok().body(clustersCalc.calculation20seconds());
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
    public ResponseEntity<String> downloadGeographicCharacters() throws ExecutionException, InterruptedException {

        MediaType mediaType = new MediaType("text", "plain", Charset.defaultCharset());
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        StringBuilder outputString = new StringBuilder("Дата расчета: " + dateFormat.format(Calendar.getInstance().getTime()) + "\n");
        outputString.append("Рассчетный период: с ").append(ResolveForm.periodStart).append(" по ").append(ResolveForm.periodEnd).append("\n");
        outputString.append("Расчет выполнялся по ").append(ResolveForm.isForPhases ? "фазе" : "амплитуде").append("\n");
        outputString.append("Размер окна: нач. частота ").append(ResolveForm.windowLeft).append(", конеч. частота : ").append(ResolveForm.windowRight).append(", несущая частота: ").append(ResolveForm.windowCenter).append("\n");
        outputString.append("Коэффициент корреляции: минимальный ").append(ResolveForm.corrDOWN).append(", максимальный ").append(ResolveForm.corrUP).append("\n");
        outputString.append("Минимальное кол-во эл. в кластере: ").append(ResolveForm.minGroupSize).append("\n");
        outputString.append("\nТиповые температуры" + "\n");
        StringBuilder stringPhase = new StringBuilder(ResolveForm.isForPhases ? "\nТиповые фазы" : "\nТиповые амплитуды" + "\n");
        StringBuilder stringAmpl = new StringBuilder(ResolveForm.isForPhases ? "\nТиповые амплитуды" : "\nТиповые фазы" + "\n");
        StringBuilder stringClusters = new StringBuilder("\nСтанции в кластерах " + "\n");
        int l = 0;
        for (GroupLine cluster : ResolveForm.clusters) {


            ArrayList<Integer> group = cluster.getGroup();
            if(group.size() > ResolveForm.minGroupSize) {
                l++;

                stringClusters.append("Кластер_").append(l + " ");
                for (double station:group) {
                    stringClusters.append((int)station + " ");
                }
                stringClusters.append("\n");

                stringPhase.append("Кластер_").append(l + " ");
                for (double phase:cluster.getTypicals()) {
                    stringPhase.append(Math.round(phase*1000)/1000.0 + " ");
                }
                stringPhase.append("\n");

                stringAmpl.append("Кластер_").append(l);
                for (int j = 0; j < ResolveForm.arrayAmplitude.get(0).get().getArray().length; j++) {
                    double average = 0;
                    for (Integer station : group) {
                        average += ResolveForm.arrayAmplitude.get(station).get().getArray()[j];
                    }
                    stringAmpl.append(" ").append(Math.round(average/group.size()*1000)/1000.0).append(" ");
                }
                stringAmpl.append("\n");

                outputString.append("Кластер_").append(l);
                for (int j = 0; j < ResolveForm.TempData[0].length; j++) {
                    double average = 0;
                    for (Integer station : group) {
                        average += ResolveForm.TempData[station][j];
                    }
                    outputString.append(" ").append(Math.round(average/group.size()*1000)/1000.0).append(" ");
                }
                outputString.append("\n");
            }
        }
        outputString.append(stringAmpl);
        outputString.append(stringPhase);
        outputString.append(stringClusters);


        outputString.append("\nГеографические характеристики\n");
        outputString.append("1.номер_кластера 2.широта_центра 3.долгота_центра 4.макс_широта 5.мин_широта 6.макс_долгота 7.мин_долгота  \n");

        for (int i = 0; i < ResolveForm.geoChars.size(); i++) {
            outputString.append((int) ResolveForm.geoChars.get(i)[0]).append(" ");
            for(int j = 1; j < ResolveForm.geoChars.get(i).length; j++)
            {
                outputString.append(ResolveForm.geoChars.get(i)[j]).append(" ");
            }
            outputString.append("\n");
        }

        return ResponseEntity.ok()
                // Content-Disposition
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + "groupGeoCharacteristics.txt")
                // Content-Type
                .contentType(mediaType)
                .body(outputString.toString());
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
