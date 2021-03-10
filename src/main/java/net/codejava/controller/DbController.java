package net.codejava.controller;

import net.codejava.Resolve.DbManager;
import net.codejava.Resolve.Model.ResolveForm;
import org.springframework.beans.factory.annotation.Autowired;
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
    public String showData(@RequestParam(value = "period", required = false) String period, Model model) {
        if(period == null) period = "3600";
        Map<Date, Double> chartData = dbManager.getWeekTemperatures(Integer.parseInt(period));
        model.addAttribute("chartData", chartData);
        model.addAttribute("title", "Датчик 2002");
        model.addAttribute("period", period);
        return "dataBase/DBchart";
    }
}
