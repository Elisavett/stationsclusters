package net.codejava.Resolve;

import net.codejava.Resolve.Model.ArrayData;
import net.codejava.Resolve.Model.Temp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
     * @param file          Входной файл
     * @throws IOException если файл не существует, это каталог, а не обычный файл, или по какой-либо другой причине не может быть открыт для чтения.
     */
    public static ArrayData ReadFromFileSplitting(File file) throws IOException {
        ArrayList<ArrayList<Double>> finalTemp = new ArrayList<ArrayList<Double>>();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
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
                    finalTemp.get(counter).add(Double.parseDouble(parts[j]));
                    counter++;
                }
            }
            flag = true;
            counter = 0;
        }

        ArrayData arrayTemp = new ArrayData();
        for (int k = 0; k < finalTemp.size(); k++) {
            double[] tempArr = new double[finalTemp.get(k).size()];
            for (int i = 0; i < finalTemp.get(k).size(); i++) {
                tempArr[i] = finalTemp.get(k).get(i);
            }
            Temp temp = new Temp(tempArr);
            arrayTemp.addData(temp);
        }
        return arrayTemp;
    }
}
