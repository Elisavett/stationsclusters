package net.codejava.controller;

import net.codejava.Resolve.Model.ResolveForm;
import net.codejava.Resolve.PhaseCalc.PhaseAmplCalc;
import net.codejava.Resolve.PhaseCalc.WindowChart;
import net.codejava.Resolve.Start;
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



    /*@PostMapping("/map")
    @Transactional(timeout = 120)
    public String map(Model model,
                      @RequestParam(value = "corr", required = false) String corr,
                      @RequestParam(value = "windowLeft", required = false) String windowLeft,
                      @RequestParam(value = "windowRight", required = false) String  windowRight,
                      @RequestParam(value = "sigma", required = false) String sigma,
                      @RequestParam(value = "isForPhase", required = false) String isForPhase,
                      @RequestParam(value = "classification", required = false) String classification,
                      @RequestParam(value = "clusters", required = false) String clusters,
                      @RequestParam(value = "toMap", required = false) String toMap,
                      @RequestParam(value = "phaseCalc", required = false) String phaseCalc,
                      @RequestParam(value = "isWindowManually", required = false) String isWindowManually,
                      @RequestParam(value = "isAccurate", required = false) String isAccurate,
                      @RequestParam(value = "windowCounted", required = false) Integer windowCounted,
                      @RequestParam(value = "minGroupSize", required = false) Integer minGroupSize) throws IOException, ExecutionException, InterruptedException {

        ResolveForm.isPhasesCounted = false;
        ResolveForm.isAccurate = Boolean.parseBoolean(isAccurate);
        ResolveForm.isForPhases = Boolean.parseBoolean(isForPhase);
        if("true".equals(phaseCalc)) {
            if (windowCounted != null) {
                ResolveForm.windowLeft = ResolveForm.windowCenter - windowCounted;
                ResolveForm.windowRight = ResolveForm.windowCenter + windowCounted;
            } else {
                ResolveForm.minGroupSize = minGroupSize;
                ResolveForm.corr = Double.parseDouble(corr);
                ResolveForm.sigma = sigma;


                if ((windowLeft.equals("0.0") && windowRight.equals("0.0")) ||
                        (windowLeft.equals("") && windowRight.equals(""))) {
                    boolean assimetricWindow = false;
                    if (Integer.parseInt(isWindowManually) == 2) assimetricWindow = true;
                    WindowChart.getWindowsChartData(assimetricWindow);
                    if (Integer.parseInt(isWindowManually) > 0) {
                        model.addAttribute("chartData", WindowChart.chartData);
                        model.addAttribute("window", ResolveForm.windowDelta);
                        return "additionals/windowChart";
                    } else {
                        if (Integer.parseInt(isWindowManually) == 0) {
                            ResolveForm.windowLeft = ResolveForm.windowCenter - ResolveForm.windowDelta;
                            ResolveForm.windowRight = ResolveForm.windowCenter + ResolveForm.windowDelta;
                        }
                    }
                } else {
                    ResolveForm.windowLeft = Double.parseDouble(windowLeft);
                    ResolveForm.windowRight = Double.parseDouble(windowRight);
                }
            }
        }
        if("true".equals(classification)) {
            ResolveForm.classification = true;
        }
        else {
            ResolveForm.classification = false;
        }
        ArrayList<String> json = new ArrayList<>();
        try {
            Start start = new Start();
            json = start.run();
        } catch (IOException | ClassNotFoundException e) {
            return "resolve/resolve";
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
    }*/
}
