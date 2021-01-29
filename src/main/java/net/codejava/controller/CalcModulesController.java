package net.codejava.controller;

import net.codejava.Resolve.Clustering.ClustersCalc;
import net.codejava.Resolve.Model.ResolveForm;
import net.codejava.Resolve.PhaseCalc.PhaseAmplCalc;
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
import java.util.concurrent.ExecutionException;

@Controller
public class CalcModulesController {
    @GetMapping("/resolveModules")
    public String resolve(Model model) {
        ResolveForm.addAllToModel(model);
        return "resolve/resolveModules";
    }
    @GetMapping("/getPhaseFragment")
    public String getPhaseFragment() {
        return "fragments/phaseFragment";
    }
    @GetMapping("/getClusterFragment")
    public String getClusterFragment() {
        return "fragments/clusterFragment";
    }
    @GetMapping("/getShowGrFragment")
    public String getShowGrFragment() {
        return "fragments/showGrFragment";
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
        ResolveForm.isForPhases = Boolean.parseBoolean(isForPhase);
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
    @PostMapping("/toMap")
    @ResponseStatus(value = HttpStatus.OK)
    public String toMap(Model model,@RequestParam(value = "minGroupSize", required = false) String minGroupSize) throws InterruptedException, ExecutionException, IOException {
        ResolveForm.minGroupSize = Integer.parseInt(minGroupSize);
        ArrayList<String> json = toMap.getGroups();
        model.addAttribute("json", json);
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

}
