package net.codejava.controller;

import net.codejava.Resolve.Model.ResolveForm;
import net.codejava.Resolve.PhaseCalc.WindowChart;
import net.codejava.Resolve.SplitInputFile;
import net.codejava.Resolve.Start;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.ExecutionException;

/*
    Контроллер для рассчета регионов
 */

@Controller
public class CalcRegionsController {

    //Рассчет для Томской области
    @GetMapping("/tomskRegionStations")
    public String tomskMap(Model model, @RequestParam(value = "correlation_coefficient", required = false) String corr) throws Exception {
        //Установка периода рассчета
        Calendar calendarStart = new GregorianCalendar(1961, Calendar.JANUARY , 1);
        Calendar calendarEnd = new GregorianCalendar(2010, Calendar.DECEMBER , 31);

        //Доп. параметры в фале температур
        ResolveForm.fileParams = new String[]{"Высота над уровнем моря", "true", "Название", "true"};
        try {
            //Рассчет
            ArrayList<String> json = calculateRegion(calendarStart.getTime(), calendarEnd.getTime(), "0.95",
                    "TomskTemps.txt", "TomskCoords.txt", true, false);
            model.addAttribute("json", json);
            ResolveForm.calculateMapModel(model);
            model.addAttribute("fromRegions", true);
        } catch (NumberFormatException e) {
            String error = "Проверьте правильность данных";
            model.addAttribute("error", error);
            return "resolve/resolve";
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return "map1";
    }

    //Рассчет для байкальской природной территории
    @GetMapping("/bptStations")
    public String bptMap(Model model, @RequestParam(value = "correlation_coefficient", required = false) String corr) throws Exception {
        //Доп. параметры в фале температур
        ResolveForm.fileParams = new String[]{"Название", "false"};

        //Установка периода рассчета
        Calendar calendarStart = new GregorianCalendar(1980, Calendar.JANUARY , 1);
        Calendar calendarEnd = new GregorianCalendar(2018, Calendar.DECEMBER , 31);
        try {
            //Рассчет
            ArrayList<String> json = calculateRegion(calendarStart.getTime(), calendarEnd.getTime(), "0.93",
                    "BPT-Temp.txt", "BPT-Coord.txt", false, true);
            model.addAttribute("json", json);
            ResolveForm.calculateMapModel(model);
            model.addAttribute("fromRegions", true);
        } catch (NumberFormatException e) {
            String error = "Проверьте правильность данных";
            model.addAttribute("error", error);
            return "resolve/resolve";
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return "map1";
    }

    //Рассчет для региона
    public ArrayList<String> calculateRegion(Date periodStart, Date periodEnd, String corr,
                                             String tempFileName, String cordFileName,
                                             boolean isStationOnY, boolean isFromPrev) throws Exception {
        //Считываение файла с сервера по его имени
        ResolveForm.TempString = SplitInputFile.ReadServerFile(tempFileName, 't');
        ResolveForm.TempData = new double[ResolveForm.TempString.length][ResolveForm.TempString[0].length];
        for(int i = 0; i < ResolveForm.TempString.length; i++){
            for(int j = 0; j < ResolveForm.TempString[i].length; j++){
                ResolveForm.TempData[i][j] = Double.parseDouble(ResolveForm.TempString[i][j]);
            }
        }

        //Установка параметров рассчета
        ResolveForm.coordsIsStationsOnY = isStationOnY;
        ResolveForm.coordData = SplitInputFile.ReadServerFile(cordFileName, 'c');
        ResolveForm.minGroupSize = 2;
        ResolveForm.isForPhases = false;
        ResolveForm.phaseToZero = false;
        ResolveForm.classification = true;

        //Если в параметрах указан коэф. корреляции, учитываем его, иначе - 0.95
        if(corr != null)
            ResolveForm.corrDOWN = Double.parseDouble(corr);
        else
            ResolveForm.corrDOWN = 0.95;
        ResolveForm.windowCenter = ResolveForm.TempData[0].length / ResolveForm.dataType;
        ResolveForm.isPhasesCounted = false;

        ResolveForm.startDate = periodStart;

        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        ResolveForm.periodStart = formatter.format(ResolveForm.startDate);
        ResolveForm.periodEnd = formatter.format(periodEnd);

        DateFormat formatter2 = new SimpleDateFormat("dd-MM-yyyy");

        ResolveForm.periodString = "c " + formatter2.format(periodStart) + " по " + formatter2.format(periodEnd);

        WindowChart.getWindowsChartData(false);
        ResolveForm.windowLeft = ResolveForm.windowCenter - ResolveForm.windowDelta;
        ResolveForm.windowRight = ResolveForm.windowCenter + ResolveForm.windowDelta;

        //Рассчет
        Start start = new Start();

        return start.run(isFromPrev);

    }
}
