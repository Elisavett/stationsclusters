/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.codejava.controller;

import com.google.gson.Gson;

import java.io.*;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;

import com.google.gson.GsonBuilder;
import net.codejava.Resolve.*;
import net.codejava.Resolve.Model.ResolveForm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author liza9
 */
@Controller
public class MapAllocationController {
    @Value("${upload.resolve}")
    private String uploadPath;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/resolveFunction")
    public String resolveFunction(Model model) {
        return "resolveFunction";
    }
    @PostMapping("/resolveFunction")
    public String resolveFunctionToMap(Model model, @RequestParam(value = "fileTemp", required = false) MultipartFile fileTemp,
                                       @RequestParam(value = "fileCoordinates", required = false) MultipartFile fileCoordinates) throws IOException {
        ResolveForm.isPhasesCounted = true;
        if(!fileTemp.getOriginalFilename().equals("")) {
            ResolveForm.PhasesData = new double[(int)fileTemp.getSize()][];
            ResolveForm.isStationsOnY = false;
            ResolveForm.PhasesData = SplitInputFile.ReadFromFileSplitting(fileTemp);
            ResolveForm.isStationsOnY = true;
        }
        if(!fileCoordinates.getOriginalFilename().equals("")){
            ResolveForm.coordData = new double[(int)fileCoordinates.getSize()][];
            ResolveForm.coordData = SplitInputFile.ReadFromFileSplitting(fileCoordinates);
            ResolveForm.coordFileName = fileCoordinates.getOriginalFilename();
        }
        ArrayList<String> json = new ArrayList<>();
        try {
            Start start = new Start();
            json = start.run();
        } catch (IOException | ClassNotFoundException e) {
            return "resolve";
        } catch (NumberFormatException e) {
            String error = "Проверьте правильность данных";
            model.addAttribute("error", error);
            return "resolve";
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        model.addAttribute("json", json);
        return "map";
    }
    @GetMapping("/resolveHistory")
    public String resolveHistory(Model model) {
        model.addAttribute("phases", ResolveForm.arrayPhase != null);
        return "resolveHistory";
    }
    @RequestMapping("/downloadPhases")
    public ResponseEntity<String> downloadFile1() throws IOException, ExecutionException, InterruptedException {

        MediaType mediaType = new MediaType("text", "plain", Charset.defaultCharset());
        String stringPhase = "";
        for (int i = 0; i < ResolveForm.arrayPhase.size(); i++) {
            String output = "";
            for (int j = 0; j < ResolveForm.arrayPhase.get(i).get().getArray().length; j++) {
                output += ResolveForm.arrayPhase.get(i).get().getArray()[j] + " ";
            }
            stringPhase += (output + "\n");
        }

        return ResponseEntity.ok()
                // Content-Disposition
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + "phases.txt")
                // Content-Type
                .contentType(mediaType)
                // Contet-Length
                .contentLength(stringPhase.length()) //
                .body(stringPhase);
    }

    @GetMapping("/resolve")
    public String resolve(Model model) {
            model.addAttribute("tempers", ResolveForm.tempFileName);
            model.addAttribute("coords", ResolveForm.coordFileName);
            model.addAttribute("sigma", ResolveForm.sigma);
            model.addAttribute("corr", ResolveForm.corr);
            model.addAttribute("dataType", ResolveForm.dataType);
            model.addAttribute("isStationsOnY", ResolveForm.isStationsOnY);
        model.addAttribute("wleft", ResolveForm.windowLeft);
        model.addAttribute("wRight", ResolveForm.windowRight);
        model.addAttribute("periodStart", ResolveForm.periodStart);
        model.addAttribute("periodEnd", ResolveForm.periodEnd);


        return "resolve";
    }

    @PostMapping("/check")
    public String check(Model model, @RequestParam(value = "fileTemp", required = false) MultipartFile fileTemp,
                        @RequestParam(value = "fileCoordinates", required = false) MultipartFile fileCoordinates,
                        @RequestParam(value = "dataType", required = false) String dataType,
                        @RequestParam String isStationsOnY,
                        @RequestParam(value = "periodStart", required = false) @DateTimeFormat(pattern="yyyy-MM-dd") Date periodStart,
                        @RequestParam(value = "periodEnd", required = false) @DateTimeFormat(pattern="yyyy-MM-dd") Date periodEnd) throws IOException {

        if(!fileTemp.getOriginalFilename().equals("")) {
            ResolveForm.TempData = new double[(int)fileTemp.getSize()][];
            ResolveForm.TempData = SplitInputFile.ReadFromFileSplitting(fileTemp);
            ResolveForm.tempFileName = fileTemp.getOriginalFilename();
        }
        if(!fileCoordinates.getOriginalFilename().equals("")){
            ResolveForm.coordData = new double[(int)fileCoordinates.getSize()][];
            ResolveForm.coordData = SplitInputFile.ReadFromFileSplitting(fileCoordinates);
            ResolveForm.coordFileName = fileCoordinates.getOriginalFilename();
        }
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        ResolveForm.periodStart = formatter.format(periodStart);
        ResolveForm.periodEnd = formatter.format(periodEnd);
        Calendar date1 = Calendar.getInstance();
        Calendar date2 = Calendar.getInstance();
        date1.setTime(periodStart);
        date2.setTime(periodEnd);
        date2.add(Calendar.DATE, 1);
        if(Boolean.parseBoolean(isStationsOnY)){
            ResolveForm.windowCenter = ResolveForm.TempData[0].length/ResolveForm.dataType;
        }
        else{
            ResolveForm.windowCenter = ResolveForm.TempData.length/ResolveForm.dataType;
        }

        ResolveForm.dataType = Integer.parseInt(dataType);
        int yearsBetween = 0;
        String message;
        if(date1.get(Calendar.DAY_OF_MONTH) == date2.get(Calendar.DAY_OF_MONTH) && date1.get(Calendar.MONTH) == date2.get(Calendar.MONTH))
        {
            yearsBetween = date2.get(Calendar.YEAR) - date1.get(Calendar.YEAR);


            if(yearsBetween == ResolveForm.windowCenter){
                if(ResolveForm.coordData[0].length == ResolveForm.TempData.length) {
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
        model.addAttribute("message", message);
        model.addAttribute("tempers", ResolveForm.tempFileName);
        model.addAttribute("coords", ResolveForm.coordFileName);
        model.addAttribute("dataType", ResolveForm.dataType);
        model.addAttribute("periodStart", ResolveForm.periodStart);
        model.addAttribute("periodEnd", ResolveForm.periodEnd);
        model.addAttribute("sigma", ResolveForm.sigma);
        model.addAttribute("corr", ResolveForm.corr);
        model.addAttribute("dataType", ResolveForm.dataType);
        model.addAttribute("isStationsOnY", ResolveForm.isStationsOnY);
        model.addAttribute("minGroupSize", ResolveForm.minGroupSize);
        return "resolve";
    }

    @GetMapping("/map")
    public String map() {
        return "map";
    }


    private static final Gson GSON = new GsonBuilder().create();

    @PostMapping("/map")
    public String map(Model model,
                      @RequestParam(value = "corr", required = false) String corr,
                      @RequestParam(value = "windowLeft", required = false) String windowLeft,
                      @RequestParam(value = "windowRight", required = false) String  windowRight,
                      @RequestParam(value = "sigma", required = false) String sigma,
                      @RequestParam(value = "isWindowManually", required = false) String isWindowManually,
                      @RequestParam(value = "windowCounted", required = false) Integer windowCounted,
                      @RequestParam(value = "minGroupSize", required = false) Integer minGroupSize) throws IOException, ExecutionException, InterruptedException {

        ResolveForm.isPhasesCounted = false;
        if(windowCounted!=null){
            ResolveForm.windowLeft = ResolveForm.windowCenter - windowCounted;
            ResolveForm.windowRight = ResolveForm.windowCenter + windowCounted;
        }
        else {
            ResolveForm.minGroupSize = minGroupSize;
            ResolveForm.corr = Double.parseDouble(corr);
            ResolveForm.sigma = sigma;


            if (windowLeft.equals("") && windowRight.equals("")) {
                boolean assimetricWindow = false;
                if(Integer.parseInt(isWindowManually)==2) assimetricWindow=true;
                WindowChart.getWindowsChartData(assimetricWindow);
                if (Integer.parseInt(isWindowManually)>0) {
                    model.addAttribute("chartData", WindowChart.chartData);
                    model.addAttribute("window", ResolveForm.windowDelta);
                    return "windowChart";
                } else {
                    if (Integer.parseInt(isWindowManually) == 0) {
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

        ArrayList<String> json = new ArrayList<>();
        try {
            Start start = new Start();
            json = start.run();
        } catch (IOException | ClassNotFoundException e) {
            return "resolve";
        } catch (NumberFormatException e) {
            String error = "Проверьте правильность данных";
            model.addAttribute("error", error);
            return "resolve";
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }


        model.addAttribute("json", json);
        return "map";
    }
}
