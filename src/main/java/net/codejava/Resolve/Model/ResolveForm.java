package net.codejava.Resolve.Model;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class ResolveForm {

    public static double[][] TempData;
    public static double[][] coordData;
    public static String tempFileName;
    public static String coordFileName;
    public static double corr = 0.8;
    public static double windowLeft = 0;
    public static double windowRight = 0;
    public static double sigma = 0.0001;
    public static int dataType = 12;
    public static boolean isStationsOnY = true;

}
