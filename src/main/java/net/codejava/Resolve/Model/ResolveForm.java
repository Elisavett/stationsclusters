package net.codejava.Resolve.Model;

import net.codejava.Resolve.SplitInputFile;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/*
    Класс с параметрами рассчета и вспомогательными методами / характеристиками
 */

public class ResolveForm {

    public static String resolveTime = "";
    public static double[][] TempData;
    public static String[][] TempString;
    public static List<Phase> arrayPhase;
    public static List<Phase> countableCharacter;
    public static String[] fileParams;
    public static List<Phase> arrayAmplitude;
    public static boolean phaseToZero = true;
    public static List<Phase> arrayTypical;
    public static List<Group> arrayGroup;
    public static List<List<Double>> arrayCorr;
    public static TreeSet<Group> clusters;
    public static ArrayList<String> json;
    public static boolean groupCross = false;
    public static int groupNum;
    public static String[][] coordData;
    public static double[] averageTemps;
    public static String tempFileName;
    public static String coordFileName;
    public static Date startDate;
    public static String periodStart = "1955-01-01";
    public static String periodString = "1955-01-01";
    public static String periodEnd = "2010-12-31";
    public static int minGroupSize = 5;
    public static double corrDOWN = 0.8;
    public static double corrUP = 1;
    public static double classCoefDOWN = 0.8;
    public static double classCoefUP = 1;
    public static double windowLeft = 0;
    public static double windowRight = 0;
    public static double windowCenter = 0;
    public static int windowDelta;
    public static String sigma = "0.0001";
    public static int dataType = 12;
    public static boolean isPhasesCounted = true;
    public static boolean tempsIsStationsOnY = false;
    public static boolean coordsIsStationsOnY = true;
    public static boolean isForPhases = true;
    public static boolean classification = false;
    public static boolean isAccurate = true;
    public static LinkedHashMap<Integer, Double> frequencyAnalysis;
    public static LinkedHashMap<Integer, Double> phaseSpector;
    public static LinkedHashMap<Integer, Double> amplitudeSpector;
    public static LinkedHashMap<Integer, Double> SKO;
    public static double maxSystemCorr = 0;
    public static double minSystemCorr = 1;

    //Добавление в модель стандартных значений параметров
    public static void addAllToModel(Model model){
        model.addAttribute("tempers", ResolveForm.tempFileName);
        model.addAttribute("coords", ResolveForm.coordFileName);
        model.addAttribute("sigma", ResolveForm.sigma);
        model.addAttribute("corr", ResolveForm.corrDOWN);
        model.addAttribute("dataType", ResolveForm.dataType);
        model.addAttribute("periodStart", ResolveForm.periodStart);
        model.addAttribute("periodEnd", ResolveForm.periodEnd);
        model.addAttribute("cordType", ResolveForm.coordsIsStationsOnY);
        model.addAttribute("tempType", ResolveForm.tempsIsStationsOnY);
        model.addAttribute("isForPhase", ResolveForm.isForPhases);
        model.addAttribute("classification", ResolveForm.classification);
    }

    //Модель для отображения на карте параметров рассчета
    public static void calculateMapModel(Model model) {
        model.addAttribute("groupNum", groupNum);
        if(resolveTime.equals("")) {
            DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh.mm.ss");
            resolveTime = dateFormat.format(Calendar.getInstance().getTime());
        }
        model.addAttribute("resolveTime", "Расчет: " + resolveTime);
        model.addAttribute("corr", "Коэф. корреляции: " + corrDOWN + " - " + corrUP);
        model.addAttribute("window", "Спектр. окно: " + Math.round(windowLeft) + " - " + Math.round(windowRight));
        model.addAttribute("calcPeriod", "Рассч. период: " + periodString);
        if(ResolveForm.isForPhases) model.addAttribute("isForPhase", "Рассчет по фазе");
        else model.addAttribute("isForPhase", "Рассчет по амплитуде");
    }

    //Сравнение введенных в форме прериодов и вывод ошибки (если есть)
    public static String compareDates(Date periodStart, Date periodEnd){
        Calendar date1 = Calendar.getInstance();
        Calendar date2 = Calendar.getInstance();
        date1.setTime(periodStart);
        date2.setTime(periodEnd);
        //Добавляем 1 день, чтобы сопоставить даты
        date2.add(Calendar.DATE, 1);

        int yearsBetween;
        String message;
        //Сопоставляем номер месяца и день месяца
        if(date1.get(Calendar.DAY_OF_MONTH) == date2.get(Calendar.DAY_OF_MONTH) && date1.get(Calendar.MONTH) == date2.get(Calendar.MONTH))
        {
            //Находим количество лет между датами
            yearsBetween = date2.get(Calendar.YEAR) - date1.get(Calendar.YEAR);

            //Если совпадает с подсчитанным центом окна - период указан верно
            if(yearsBetween == ResolveForm.windowCenter){
                int period1 = ResolveForm.TempString.length;
                int period2 = ResolveForm.coordData[0].length;
                if(period1 == period2) {
                    message = "Период выбран верно";
                }
                else
                {
                    message = "Количество станций в файлах не совпадает";
                }
            }
            else{
                message = "Указанный период (" + yearsBetween +") не совпадает с количеством данных в файле с температурами (" + (int)ResolveForm.windowCenter + ")";
            }
        }
        else
        {
            message = "Неверно указан период. Данные должны быть выбраны за ровное количество лет (Пример: 01.01.1955-31.12.2017)";
        }
        return message;
    }
    //Сохранить параметры из формы
    public static void saveParams(boolean tempType, boolean cordsType,
                                  MultipartFile fileTemp, MultipartFile fileCoordinates,
                                  int dataType,
                                  Date periodStart, Date periodEnd){
        SplitInputFile.saveFilesInfo(tempType, cordsType, fileTemp, fileCoordinates);

        ResolveForm.startDate = periodStart;
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        DateFormat formatter2 = new SimpleDateFormat("dd-MM-yyyy");

        ResolveForm.periodStart = formatter.format(periodStart);
        ResolveForm.periodEnd = formatter.format(periodEnd);

        ResolveForm.periodString = "c " + formatter2.format(periodStart) + " по " + formatter2.format(periodEnd);

        ResolveForm.dataType = dataType;

        ResolveForm.windowCenter = ResolveForm.TempData[0].length / ResolveForm.dataType;
    }
    public static  <T> List<T> FutureToPlaneObj(List<Future<T>> futureObject) throws ExecutionException, InterruptedException {
        List<T> planeObject = new ArrayList<>();
        for(Future<T> item: futureObject){
            planeObject.add(item.get());
        }
        return planeObject;
    }
    public static void setWindowLimits(int delta){
        ResolveForm.windowLeft = ResolveForm.windowCenter - delta;
        ResolveForm.windowRight = ResolveForm.windowCenter + delta;
    }
}
