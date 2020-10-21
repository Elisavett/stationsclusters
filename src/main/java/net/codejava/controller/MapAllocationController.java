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
    public String resolve() {
        return "resolve";
    }

    @GetMapping("/map")
    public String map() {
        return "map";
    }


    private static final Gson GSON = new GsonBuilder().create();

    @PostMapping("/map")
    public String map(Model model,
                      @RequestParam("fileTemp") MultipartFile fileTemp,
                      @RequestParam("fileCoordinates") MultipartFile fileCoordinates,
                      @RequestParam String corr,
                      @RequestParam String windowLeft,
                      @RequestParam String  windowRight,
                      @RequestParam String sigma,
                      @RequestParam(value = "dataType", required = false) String dataType) throws IOException {

        double correlation = Double.parseDouble(corr.replace(',', '.'));
        double window_left = Double.parseDouble(windowLeft.replace(',', '.'));
        double window_right = Double.parseDouble(windowRight.replace(',', '.'));
        ArrayData arrayTemp = SplitInputFile.ReadFromFileSplitting(fileTemp);
        if(window_left==0 && window_right==0){
            window_left = arrayTemp.size()*0.85/Integer.parseInt(dataType);
            window_right = arrayTemp.size()*1.15/Integer.parseInt(dataType);
        }
        double err = Double.parseDouble(sigma.replace(',', '.'));

        ArrayList<String> json = new ArrayList<>();
        try {
            json = Start.run(arrayTemp, fileCoordinates, correlation, window_left, window_right, err);
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
