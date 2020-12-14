package net.codejava.Resolve.Model;

import java.util.List;
import java.util.concurrent.Future;

public class ResolveForm {

    public static double[][] TempData;
    public static double[][] PhasesData;
    public static List<Future<Phase>> arrayPhase;
    public static List<Future<Phase>> arrayTypical;
    public static String stringPhase;
    public static double[][] coordData;
    public static String tempFileName;
    public static String coordFileName;
    public static String periodStart = "1955-01-01";
    public static String periodEnd = "2010-12-31";
    public static int minGroupSize = 5;
    public static double corr = 0.8;
    public static double windowLeft = 0;
    public static double windowRight = 0;
    public static double windowCenter = 0;
    public static int windowDelta;
    public static String sigma = "0.0001";
    public static int dataType = 12;
    public static boolean isPhasesCounted = false;
    public static boolean tempsIsStationsOnY = true;
    public static boolean coordsIsStationsOnY = true;
    public static boolean isForPhases = true;
}
