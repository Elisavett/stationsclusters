package net.codejava.Resolve.Model;

import org.springframework.ui.Model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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
    public static Model calculateMapModel(Model model) {

        model.addAttribute("groupNum", groupNum);
        if(resolveTime.equals("")) {
            DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh.mm.ss");
            resolveTime = dateFormat.format(Calendar.getInstance().getTime());
        }
        model.addAttribute("resolveTime", "Расчет: " + resolveTime);
        model.addAttribute("corr", "Коэффициент корреляции: " + corrDOWN + " - " + corrUP);
        model.addAttribute("window", "Спектральное окно: " + Math.round(windowLeft) + " - " + Math.round(windowRight));
        model.addAttribute("calcPeriod", "Рассчетный период: " + periodString);
        if(ResolveForm.isForPhases) model.addAttribute("isForPhase", "Рассчет по фазе");
        else model.addAttribute("isForPhase", "Рассчет по амплитуде");
        return model;
    }
    public static  <T> List<T> FutureToPlaneObj(List<Future<T>> futureObject) throws ExecutionException, InterruptedException {
        List<T> planeObject = new ArrayList<>();
         for(Future<T> item: futureObject){
            planeObject.add(item.get());
        }
        return planeObject;
    }
}
