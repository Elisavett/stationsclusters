package net.codejava.controller;

import net.codejava.Resolve.Model.ResolveForm;
import net.codejava.Resolve.Modules.ModulesCalc;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;

/*
    Котроллер для визуального рассчета
 */

@Controller
public class VisualResolveController {

    //Получение данных для отображения на карте
    @GetMapping("/getJsonStations")
    public ResponseEntity<ArrayList<String>> showStationsOnMap() {

        ArrayList<String> json = ModulesCalc.JsonWOResolve();
        ResolveForm.isPhasesCounted = false;
        return ResponseEntity.ok().body(json);
    }

    //Получение формы для загрузки файлов с данными (температуры, координаты)
    @GetMapping("/resolveDiagram")
    public String resolveDiagram(Model model) {
        ResolveForm.addAllToModel(model);
        model.addAttribute("isModules", "false");
        return "resolve/resolveDiagram";
    }

    //Формиование данных для отображения на карте
    @GetMapping("/getJsonToMap")
    public ResponseEntity<ArrayList<String>> getJson() {
        ResolveForm.resolveTime = "";
        return ResponseEntity.ok().body(ModulesCalc.JsonCalc());
    }

    @GetMapping("/getData")
    public ResponseEntity<String> getData() {
        return ResponseEntity.ok().body("/downloadPhases");
    }

}
