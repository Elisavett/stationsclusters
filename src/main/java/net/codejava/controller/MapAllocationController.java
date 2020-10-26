/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.codejava.controller;

import com.google.gson.Gson;

import java.io.*;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import com.google.gson.GsonBuilder;
import net.codejava.Resolve.FileManager;
import net.codejava.Resolve.Model.ArrayData;
import net.codejava.Resolve.Model.Data;
import net.codejava.Resolve.Model.ResolveForm;
import net.codejava.Resolve.Model.Temp;
import net.codejava.Resolve.Serialize;
import net.codejava.Resolve.SplitInputFile;
import net.codejava.Resolve.Start;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

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
    @GetMapping("/resolve")
    public String resolve(Model model) {
            model.addAttribute("tempers", ResolveForm.tempFileName);
            model.addAttribute("coords", ResolveForm.coordFileName);
            model.addAttribute("sigma", ResolveForm.sigma);
            model.addAttribute("wleft", ResolveForm.windowLeft);
            model.addAttribute("wRight", ResolveForm.windowRight);
            model.addAttribute("corr", ResolveForm.corr);
            model.addAttribute("dataType", ResolveForm.dataType);
            model.addAttribute("isStationsOnY", ResolveForm.isStationsOnY);

        return "resolve";
    }

    @GetMapping("/map")
    public String map() {
        return "map";
    }


    private static final Gson GSON = new GsonBuilder().create();

    @PostMapping("/map")
    public String map(Model model,
                      @RequestParam(value = "fileTemp", required = false) MultipartFile fileTemp,
                      @RequestParam(value = "fileCoordinates", required = false) MultipartFile fileCoordinates,
                      @RequestParam String corr,
                      @RequestParam String windowLeft,
                      @RequestParam String  windowRight,
                      @RequestParam String sigma,
                      @RequestParam String isStationsOnY,
                      @RequestParam(value = "dataType", required = false) String dataType) throws IOException {

        ResolveForm.corr = Double.parseDouble(corr);
        ResolveForm.sigma = Double.parseDouble(sigma);
        ResolveForm.windowLeft = Double.parseDouble(windowLeft);
        ResolveForm.windowRight = Double.parseDouble(windowRight);
        ResolveForm.isStationsOnY = Boolean.parseBoolean(isStationsOnY);


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

        if(ResolveForm.windowLeft==0 && ResolveForm.windowRight==0){
            ResolveForm.dataType = Integer.parseInt(dataType);
            ResolveForm.windowLeft = ResolveForm.TempData[0].length*0.85/ResolveForm.dataType;
            ResolveForm.windowRight = ResolveForm.TempData[0].length*1.15/ResolveForm.dataType;
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
