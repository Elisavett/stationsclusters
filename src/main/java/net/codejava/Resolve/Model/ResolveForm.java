package net.codejava.Resolve.Model;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

public class ResolveForm {

    public static double[][] TempData;
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
    public static int windowManually = 0;
    public static boolean isStationsOnY = true;

}
