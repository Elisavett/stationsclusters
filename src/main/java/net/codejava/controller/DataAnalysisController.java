package net.codejava.controller;

import net.codejava.Resolve.Model.ResolveForm;
import net.codejava.Resolve.PhaseCalc.DataAnalysis;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class DataAnalysisController {
    @GetMapping("/frequencyAnalysis")
    public String frequencyAnalysis(Model model, @RequestParam Integer station){
        double[] temp = ResolveForm.TempData[station].clone();
        DataAnalysis dataAnalysis = new DataAnalysis(temp);
        ResolveForm.frequencyAnalysis = dataAnalysis.getFrequencySpector();
        setCommonChartOptions(model,
                ResolveForm.frequencyAnalysis,
                "Отсчеты", "Модуль спектра",
                "Частотный спектр (станция №" + station + ")",
                "frequency",
                "corechart");
        return "additionals/frequencyChart";
    }
    @GetMapping("/temperatureChart")
    public String temperatureChart(Model model, @RequestParam Integer station){
        double avgTemp = DataAnalysis.getStationAvgTemp(station);
        double sko = DataAnalysis.getStationSKO(avgTemp, station);
        setCommonChartOptions(model,
                DataAnalysis.getStationTemp(station),
                "Отсчеты", "Значение температуры",
                "Температура",
                "temperature",
                "line");
        model.addAttribute("additionalData", new double[]{avgTemp, avgTemp+sko, avgTemp-sko});
        model.addAttribute("additionalY_names", new String[]{"Средняя", "+СКО", "-СКО"});
        return "additionals/frequencyChart";
    }
    @GetMapping("/SKOAnalysis")
    public String SKOAnalysis(Model model) {
        ResolveForm.SKO = DataAnalysis.getAllSKO();
        setCommonChartOptions(model, ResolveForm.SKO,
                "Станции", "Значение СКО",
                "Среднеквадратическое отклонение по станциям",
                "sko",
                "corechart");
        return "additionals/frequencyChart";
    }
    private void setCommonChartOptions(Model model,
                                       Object data,
                                       String X, String Y,
                                       String title, String ID, String type){
        model.addAttribute("chartData", data);
        model.addAttribute("X_name", X);
        model.addAttribute("Y_name", Y);
        model.addAttribute("title", title );
        model.addAttribute("id", ID);
        model.addAttribute("chartType", type);
    }
}
