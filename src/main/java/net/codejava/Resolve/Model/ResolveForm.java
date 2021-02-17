package net.codejava.Resolve.Model;

import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

public class ResolveForm {

    public static double[][] TempData;
    public static double[][] PhasesData;
    public static List<Future<Phase>> arrayPhase;
    public static List<Future<Phase>> arrayTypical;
    public static List<Future<Group>> arrayGroup;
    public static ArrayList<String> json;
    public static ArrayList<String> geoChars;
    public static boolean groupCross = false;
    public static String stringPhase;
    public static int groupNum;
    public static double[][] coordData;
    public static String tempFileName;
    public static String coordFileName;
    public static String periodStart = "1955-01-01";
    public static String periodEnd = "2010-12-31";
    public static int minGroupSize = 5;
    public static double corr = 0.8;
    public static double classCoef = 0.8;
    public static double windowLeft = 0;
    public static double windowRight = 0;
    public static double windowCenter = 0;
    public static int windowDelta;
    public static String sigma = "0.0001";
    public static int dataType = 12;
    public static boolean isPhasesCounted = true;
    public static boolean tempsIsStationsOnY = true;
    public static boolean coordsIsStationsOnY = true;
    public static boolean isForPhases = true;
    public static boolean classification = false;
    public static boolean isAccurate = true;

    public static void addAllToModel(Model model){
        model.addAttribute("tempers", ResolveForm.tempFileName);
        model.addAttribute("coords", ResolveForm.coordFileName);
        model.addAttribute("sigma", ResolveForm.sigma);
        model.addAttribute("corr", ResolveForm.corr);
        model.addAttribute("dataType", ResolveForm.dataType);
        model.addAttribute("wleft", ResolveForm.windowLeft);
        model.addAttribute("wRight", ResolveForm.windowRight);
        model.addAttribute("periodStart", ResolveForm.periodStart);
        model.addAttribute("periodEnd", ResolveForm.periodEnd);
        model.addAttribute("cordType", ResolveForm.coordsIsStationsOnY);
        model.addAttribute("tempType", ResolveForm.tempsIsStationsOnY);
        model.addAttribute("isForPhase", ResolveForm.isForPhases);
        model.addAttribute("classification", ResolveForm.classification);
    }
}
