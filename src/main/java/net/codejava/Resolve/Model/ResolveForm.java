package net.codejava.Resolve.Model;

import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ResolveForm {

    public static String resolveTime = "";
    public static double[][] TempData;
    public static String[][] TempString;
    public static List<Phase> arrayPhase;
    public static String[] fileParams;
    public static List<Phase> arrayAmplitude;
    public static List<Phase> arrayTypical;
    public static List<Group> arrayGroup;
    public static TreeSet<Group> clusters;
    public static ArrayList<String> json;
    public static boolean groupCross = false;
    public static int groupNum;
    public static String[][] coordData;
    public static double[] averageTemps;
    public static String tempFileName;
    public static String coordFileName;
    public static String periodStart = "1955-01-01";
    public static String periodString = "1955-01-01";
    public static String periodEnd = "2010-12-31";
    public static int minGroupSize = 5;
    public static double corrDOWN = 0.8;
    public static double corrUP = 1;
    public static double classCoef = 0.8;
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
    public static LinkedHashMap<Integer, Double> SKO;

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
    public static  <T> List<T> FutureToPlaneObj(List<Future<T>> futureObject) throws ExecutionException, InterruptedException {
        List<T> planeObject = new ArrayList<>();
         for(Future<T> item: futureObject){
            planeObject.add(item.get());
        }
        return planeObject;
    }
}
