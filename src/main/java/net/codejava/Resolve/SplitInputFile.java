package net.codejava.Resolve;

import net.codejava.Exeptions.FileException;
import net.codejava.Resolve.Model.ArrayData;
import net.codejava.Resolve.Model.ResolveForm;
import net.codejava.Resolve.Model.Temp;
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
     * @throws IOException если файл не существует, это каталог, а не обычный файл, или по какой-либо другой причине не может быть открыт для чтения.
     */
    public static double[][] ReadFromFileSplitting(MultipartFile fileTemp, char filetype) throws FileException {
        try {

            ArrayList<ArrayList<Double>> finalTemp = new ArrayList<ArrayList<Double>>();
            InputStream is = fileTemp.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            String firstLine = reader.readLine();
            String fileContent = firstLine.split("\\s+")[2];
            boolean isOnY;
            if(fileContent.equals("x") || fileContent.equals("х") || fileContent.equals("X") || fileContent.equals("Х")){
                isOnY = false;
            }
            else if(fileContent.equals("y") || fileContent.equals("у") || fileContent.equals("Y") || fileContent.equals("У")){
                isOnY = true;
            }
            else
            {
                throw new Exception("Данные о компановке файла не верны. Укажите данные в формате 'Станции по х' первой строкой в файле");
            }

            if(filetype == 't') ResolveForm.tempsIsStationsOnY = isOnY;
            if(filetype == 'c') ResolveForm.coordsIsStationsOnY = isOnY;
            List<String> lines = new ArrayList<String>();
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }

            boolean flag = false;
            for (int i = 0; i < lines.size(); i++) {
                String[] parts = lines.get(i).split("\\s+");
                int counter = 0;
                for (int j = 0; j < parts.length; j++) {
                    if (!parts[j].equals("")) {
                        if (!flag) finalTemp.add(new ArrayList<Double>());
                        {
                            if(parts[j].contains(",")) parts[j] = parts[j].replace(',', '.');
                            finalTemp.get(counter).add(Double.parseDouble(parts[j]));
                        }
                        counter++;
                    }
                }
                flag = true;
                counter = 0;
            }
            double[][] arrayTemp;
            if (filetype=='t'?!isOnY:isOnY) {
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
