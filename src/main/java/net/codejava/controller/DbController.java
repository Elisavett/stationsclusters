package net.codejava.controller;

import net.codejava.Resolve.DbManager;
import net.codejava.Resolve.Model.ResolveForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.persistence.Tuple;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
public class DbController {
    @Autowired
    private DbManager dbManager;
    @GetMapping("/showData")
    public String showData(Model model) {
        dbManager.countWeekTemperatures(3600);
        model.addAttribute("chartData2002", dbManager.map2002);
        model.addAttribute("chartData4402", dbManager.map4402);
        return "dataBase/DBchart";
    }
    @GetMapping("/showData2002")
    public ResponseEntity<Map<Date, Double>> showData2002(@RequestParam(value = "period", required = false) String period, Model model) {
        if(period == null) period = "3600";
        dbManager.countWeekTemperatures(Integer.parseInt(period));
        return ResponseEntity.ok().body(dbManager.map2002);
    }
    @GetMapping("/showData4402")
    public ResponseEntity<Map<Date, Double>> showData4402(@RequestParam(value = "period", required = false) String period, Model model) {
        if(period == null) period = "3600";
        dbManager.countWeekTemperatures(Integer.parseInt(period));
        return ResponseEntity.ok().body(dbManager.map4402);
    }

}
