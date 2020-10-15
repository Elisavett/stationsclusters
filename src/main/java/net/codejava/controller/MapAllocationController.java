/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.codejava.controller;

import com.google.gson.Gson;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import com.google.gson.GsonBuilder;
import net.codejava.Resolve.FileManager;
import net.codejava.Resolve.Serialize;
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
                      @RequestParam String sigma) {

        double correlation = Double.parseDouble(corr.replace(',', '.'));
        double window_left = Double.parseDouble(windowLeft.replace(',', '.'));
        double window_right = Double.parseDouble(windowRight.replace(',', '.'));
        double err = Double.parseDouble(sigma.replace(',', '.'));

        String name = fileTemp.getOriginalFilename();

        String usernamePath = "user" + "/";

        //String usernamePath = "/" + user.getUsername() + "/";
        String userPath = uploadPath + "/" + usernamePath;//путь до папки пользователя
        String uuid = UUID.randomUUID().toString();
        String uniqueNameTemp = uuid + "Temp.txt";
        String uniqueNameCoord = uuid + "Koord.txt";
        String uniqueNameJson = uuid + "json";
        //создаю папки если они не созданы
        FileManager.createFolder(uploadPath);
        FileManager.createFolder(userPath);

        if (!fileTemp.isEmpty() & !fileCoordinates.isEmpty()) {
            try {
                byte[] bytesTemp = fileTemp.getBytes();
                byte[] bytesCoordinates = fileCoordinates.getBytes();
                BufferedOutputStream streamTemp =
                        new BufferedOutputStream(new FileOutputStream(new File(userPath + uniqueNameTemp)));
                BufferedOutputStream streamCoordinates =
                        new BufferedOutputStream(new FileOutputStream(new File(userPath + uniqueNameCoord)));
                streamTemp.write(bytesTemp);
                streamCoordinates.write(bytesCoordinates);
                streamTemp.close();
                streamCoordinates.close();
            } catch (Exception e) {
                System.out.println("не удалось загрузить " + e.getMessage());
            }
        } else {
            System.out.println("файл пустой");
        }

        ArrayList<String> json = new ArrayList<>();
        try {
            json = Start.run(userPath, uniqueNameTemp, uniqueNameCoord, correlation, window_left, window_right, err);
        } catch (IOException | ClassNotFoundException e) {
            return "resolve";
        } catch (NumberFormatException e) {
            String error = "Проверьте правильность данных";
            model.addAttribute("error", error);
            return "resolve";
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        //Сериализую json файл
        try {
            Serialize.serialize(userPath + uniqueNameJson, json);
        } catch (IOException e) {
            e.printStackTrace();
        }
        model.addAttribute("json", json);
        return "map";
    }
}
