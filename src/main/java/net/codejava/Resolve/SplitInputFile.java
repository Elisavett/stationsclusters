package net.codejava.Resolve;

import net.codejava.Exeptions.FileException;
import net.codejava.Resolve.Model.ResolveForm;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс служит для разделения входных данных
 *
 * @version 1.0
 */
public class SplitInputFile {
    /**
     * Разделяет полученный файл по строчкам и помещает каждую строку в новый файл в переданный путь
     *
     */
    public static double[][] ReadFromFileSplitting(MultipartFile fileTemp, char filetype) throws FileException {
        try {

            ArrayList<ArrayList<Double>> finalTemp = new ArrayList<>();
            InputStream is = fileTemp.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            boolean isOnY = false;

            if(filetype == 't') isOnY = ResolveForm.tempsIsStationsOnY;
            if(filetype == 'c') isOnY = ResolveForm.coordsIsStationsOnY;
            List<String> lines = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }

            boolean flag = false;
            for (String s : lines) {
                String[] parts = s.split("\\s+");
                int counter = 0;
                for (int j = 0; j < parts.length; j++) {
                    if (!parts[j].equals("")) {
                        if (!flag) finalTemp.add(new ArrayList<>());
                        {
                            if (parts[j].contains(",")) parts[j] = parts[j].replace(',', '.');
                            finalTemp.get(counter).add(Double.parseDouble(parts[j]));
                        }
                        counter++;
                    }
                }
                flag = true;
            }
            double[][] arrayTemp;
            if ((filetype == 't') != isOnY) {
                arrayTemp = new double[finalTemp.size()][];
                for (int k = 0; k < finalTemp.size(); k++) {
                    double[] tempArr = new double[finalTemp.get(k).size()];
                    for (int i = 0; i < finalTemp.get(k).size(); i++) {
                        tempArr[i] = finalTemp.get(k).get(i);
                    }
                    arrayTemp[k] = tempArr;
                }
            } else {
                arrayTemp = new double[finalTemp.get(0).size()][];
                for (int k = 0; k < finalTemp.get(0).size(); k++) {
                    double[] tempArr = new double[finalTemp.size()];
                    for (int i = 0; i < finalTemp.size(); i++) {
                        tempArr[i] = finalTemp.get(i).get(k);
                    }
                    arrayTemp[k] = tempArr;
                }
            }
            return arrayTemp;
        }
        catch (Exception ex){
            throw new FileException(ex);
        }

    }
}
