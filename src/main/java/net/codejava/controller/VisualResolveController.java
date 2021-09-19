package net.codejava.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.codejava.Resolve.Model.GroupAndCoordinates;
import net.codejava.Resolve.Model.ResolveForm;
import net.codejava.Resolve.Modules.ModulesCalc;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Controller
public class VisualResolveController {
    @GetMapping("/getJsonStations")
    public ResponseEntity<ArrayList<String>> showStationsOnMap() {

        Gson GSON = new GsonBuilder().create();
        //формирую json файл
        ArrayList<String> json = new ArrayList<>();
        for (int j = 0; j < ResolveForm.coordData[0].length; j++) {
            Map<String, String> additional = new HashMap<>();
            if (ResolveForm.coordData.length > 3 && ResolveForm.fileParams.length > 1) {
                int limit = Math.min((ResolveForm.coordData.length - 3), ResolveForm.fileParams.length / 2);
                for (int i = 0; i < limit; i++) {
                    if (Boolean.parseBoolean(ResolveForm.fileParams[i * 2 + 1]))
                        additional.put(ResolveForm.fileParams[i * 2], ResolveForm.coordData[i + 3][j]);
                }
            }
            GroupAndCoordinates groupAndCoordinates = new GroupAndCoordinates(
                    Double.parseDouble(ResolveForm.coordData[0][j]),
                    Double.parseDouble(ResolveForm.coordData[1][j]),
                    Double.parseDouble(ResolveForm.coordData[2][j]),
                    1,
                    true,
                    additional);
            String jsonData = GSON.toJson(groupAndCoordinates);
            json.add(jsonData);
        }
        ResolveForm.isPhasesCounted = false;
        return ResponseEntity.ok().body(json);
    }
    @GetMapping("/resolveDiagram")
    public String resolveDiagram(Model model) {
        ResolveForm.addAllToModel(model);
        model.addAttribute("isModules", "false");
        return "resolve/resolveDiagram";
    }
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
