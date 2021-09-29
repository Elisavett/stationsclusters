package net.codejava.controller;

import net.codejava.Resolve.Model.Group;
import net.codejava.Resolve.Model.ResolveForm;
import net.codejava.Resolve.PhaseCalc.DataAnalysis;
import net.codejava.Resolve.PhaseCalc.WindowChart;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

/*
    Контроллер для анализв данных и отображения гарфиков
 */

@Controller
public class DataAnalysisController {

    //Рассчет частотного спектра
    @GetMapping("/frequencyAnalysis")
    public String frequencyAnalysis(Model model, @RequestParam Integer station){
        double[] temp = ResolveForm.TempData[station].clone();
        //Анализ данных температуры - рассчет частотного спектра
        DataAnalysis dataAnalysis = new DataAnalysis(temp);
        ResolveForm.frequencyAnalysis = dataAnalysis.getFrequencySpector();
        //Формирование данных для отображения на графике
        setCommonChartOptions(model,
                ResolveForm.frequencyAnalysis,
                "Отсчеты", "Модуль спектра",
                "Частотный спектр (станция №" + station + ")",
                "frequency",
                "corechart");
        return "additionals/frequencyChart";
    }

    //Анализ группы
    @RequestMapping("/showClusterAnalysis")
    public String showClusterAnalysis(Model model, @RequestParam Integer clusterNum){
        Group[] groups = new Group[ResolveForm.clusters.size()];
        groups = ResolveForm.clusters.toArray(groups);
        List<Integer> group = groups[clusterNum-1].getGroupMembers();
        //Данные для графика типовых температур
        LinkedHashMap<String,Double[]> typicalTemps = DataAnalysis.getTypicalTempsChart(group);
        //Формировнаие дданных типовых темепратур для отображения на графике
        setCommonChartOptions(model,
                typicalTemps,
                "Отсчеты", "Значение температуры",
                "Температура",
                "temperature",
                "line");
        LinkedHashMap<String, Double> typicalPhases;
        LinkedHashMap<String, Double> typicalAmpls;
        //Рассчет спекра амплитуды или фазы в зависимости от того, по какой характеристике проводился рассчет
        if(ResolveForm.isForPhases){
            ResolveForm.phaseSpector = DataAnalysis.getCountableCharacterSpector(clusterNum);
            ResolveForm.amplitudeSpector = DataAnalysis.getSpecifiedCharacterSpector(group, ResolveForm.arrayAmplitude);
            typicalPhases = DataAnalysis.getTypicalCountableCharacterChart(clusterNum-1);
            typicalAmpls = DataAnalysis.getTypicalSpecifiedsForChart(group, ResolveForm.arrayAmplitude);
        }
        else{
            ResolveForm.phaseSpector = DataAnalysis.getSpecifiedCharacterSpector(group, ResolveForm.arrayPhase);
            ResolveForm.amplitudeSpector = DataAnalysis.getCountableCharacterSpector(clusterNum);
            typicalPhases = DataAnalysis.getTypicalSpecifiedsForChart(group, ResolveForm.arrayPhase);
            typicalAmpls = DataAnalysis.getTypicalCountableCharacterChart(clusterNum-1);
        }
        //Данные для передачи на страницу анализа группы
        model.addAttribute("phases", typicalPhases);
        model.addAttribute("phaseSpector", ResolveForm.phaseSpector);
        model.addAttribute("amplitudeSpector", ResolveForm.amplitudeSpector);
        model.addAttribute("amplitudes", typicalAmpls);
        model.addAttribute("clusterModel", DataAnalysis.getClusterModel(groups[clusterNum-1].getPhases(), group, 0.));
        model.addAttribute("clusterNum", clusterNum);
        model.addAttribute("corrTable", groups[clusterNum-1].getGroupCorrTable());
        model.addAttribute("maxCorr", groups[clusterNum-1].getMaxGroupCorr());
        model.addAttribute("minCorr", groups[clusterNum-1].getMinGroupCorr());
        model.addAttribute("groupMembers", group);

        return "additionals/groupAnalysis";
    }

    //Рассчет модели для группы с указанием сдвига по фазе
    @GetMapping("/clusterModel")
    public ResponseEntity<LinkedHashMap<String, Double>> clusterModel(String clusterNum, String offset) {
        Group[] groups = new Group[ResolveForm.clusters.size()];
        groups = ResolveForm.clusters.toArray(groups);
        List<Integer> group = groups[Integer.parseInt(clusterNum)-1].getGroupMembers();

        return ResponseEntity.ok().body(DataAnalysis.getClusterModel(groups[Integer.parseInt(clusterNum)-1].getPhases(), group, Double.parseDouble(offset)));
    }

    //Рассчет корреляции групп
    @GetMapping("/systemAnalysis")
    public String systemAnalysis(Model model) throws ExecutionException, InterruptedException {
        model.addAttribute("corrTable", DataAnalysis.getGroupPhasesCorrTable());
        model.addAttribute("maxCorr", ResolveForm.maxSystemCorr);
        model.addAttribute("minCorr", ResolveForm.minSystemCorr);
        return "additionals/systemAnalysis";
    }

    //Рассчет СКО, средней и подготовка данных для отображения графика температуры
    @GetMapping("/temperatureChart")
    public String temperatureChart(Model model, @RequestParam Integer station){
        double avgTemp = DataAnalysis.getAvgTemp(ResolveForm.TempData[station].clone());
        double sko = DataAnalysis.getStationSKO(avgTemp, ResolveForm.TempData[station].clone());
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

    //Анализ данных для станции (СКО, средняя температура, частотный анализ)
    @GetMapping("/dataAnalysisForStation")
    public String dataAnalysisForStation(Model model, @RequestParam Integer station) {
        int indexStation = station - 1;
        double avgTemp = DataAnalysis.getAvgTemp(ResolveForm.TempData[indexStation].clone());
        double sko = DataAnalysis.getStationSKO(avgTemp, ResolveForm.TempData[indexStation].clone());
        model.addAttribute("temperaturesData", DataAnalysis.getStationTemp(indexStation));
        model.addAttribute("additionalData", new double[]{avgTemp + sko, avgTemp - sko});

        double[] temp = ResolveForm.TempData[indexStation].clone();
        DataAnalysis dataAnalysis = new DataAnalysis(temp);
        ResolveForm.frequencyAnalysis = dataAnalysis.getFrequencySpector();
        model.addAttribute("frequencyData", ResolveForm.frequencyAnalysis);
        model.addAttribute("stationNum", station);
        return "additionals/stationDataCharts";
    }

    //Рассчет СКО по станциям и график
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

    //Отображение графика для выбора окна фильтрации
    @GetMapping("/showChart")
    public String showChart(Model model,
                            @RequestParam(value = "isWindowManually") String isWindowManually) throws ExecutionException, InterruptedException {
        boolean assimetricWindow = false;
        if (Integer.parseInt(isWindowManually) == 2) assimetricWindow = true;
        WindowChart.getWindowsChartData(assimetricWindow);
        model.addAttribute("chartData", WindowChart.chartData);
        model.addAttribute("window", ResolveForm.windowDelta);
        model.addAttribute("windowCenter", ResolveForm.windowCenter);
        return "additionals/windowChart";
    }

    //Добавление данных для отображения графика
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
