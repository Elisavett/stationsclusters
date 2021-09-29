package net.codejava.Resolve;

import net.codejava.Exeptions.FileException;
import net.codejava.Resolve.Model.ResolveForm;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Класс служит для разделения входных данных
 *
 * @version 1.0
 */
public class SplitInputFile {

    public static void saveFilesInfo(boolean tempType, boolean cordsType,
                               MultipartFile fileTemp, MultipartFile fileCoordinates) {
        ResolveForm.tempsIsStationsOnY = tempType;
        ResolveForm.coordsIsStationsOnY = cordsType;
        if (!"".equals(fileTemp.getOriginalFilename())) {
            ResolveForm.TempString = new String[(int) fileTemp.getSize()][];
            ResolveForm.TempString = ReadFromFileSplitting(fileTemp, 't');
            ResolveForm.TempData = new double[ResolveForm.TempString.length][ResolveForm.TempString[0].length];
            for(int i = 0; i < ResolveForm.TempString.length; i++){
                for(int j = 0; j < ResolveForm.TempString[i].length; j++){
                    ResolveForm.TempData[i][j] = Double.parseDouble(ResolveForm.TempString[i][j]);
                }
            }
            ResolveForm.tempFileName = fileTemp.getOriginalFilename();
        }
        if (!"".equals(fileCoordinates.getOriginalFilename())) {
            ResolveForm.coordData = new String[(int) fileCoordinates.getSize()][];
            ResolveForm.coordData = ReadFromFileSplitting(fileCoordinates, 'c');
            ResolveForm.coordFileName = fileCoordinates.getOriginalFilename();
        }
    }

    public static String[][] ReadFromFileSplitting(MultipartFile fileTemp, char filetype) throws FileException {
        try {
            InputStream is = fileTemp.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "windows-1251"));
            String line;

            List<String> lines = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            return ReadFromStrings(lines, filetype);

        }
        catch (Exception ex){
            throw new FileException(ex);
        }
    }
    public static String[][] ReadFromStrings(List<String> fileStrings, char filetype) throws Exception {
        boolean flag = false;
        boolean isOnY = false;
        ArrayList<ArrayList<String>> finalTemp = new ArrayList<>();
        if(filetype == 't') isOnY = ResolveForm.tempsIsStationsOnY;
        if(filetype == 'c') isOnY = ResolveForm.coordsIsStationsOnY;
        for (String s : fileStrings) {
            String[] parts = s.split("\\s+");
            int counter = 0;
            for (int j = 0; j < parts.length; j++) {
                if (!parts[j].equals("")) {
                    if (!flag) finalTemp.add(new ArrayList<>());
                    {
                        if (parts[j].contains(",")) parts[j] = parts[j].replace(',', '.');
                        finalTemp.get(counter).add(parts[j]);
                    }
                    counter++;
                }
            }
            flag = true;
        }
        String[][] arrayTemp;
        if ((filetype == 't') != isOnY) {
            arrayTemp = new String[finalTemp.size()][];
            for (int k = 0; k < finalTemp.size(); k++) {
                String[] tempArr = new String[finalTemp.get(k).size()];
                for (int i = 0; i < finalTemp.get(k).size(); i++) {
                    tempArr[i] = finalTemp.get(k).get(i);
                }
                arrayTemp[k] = tempArr;
            }
        } else {
            arrayTemp = new String[finalTemp.get(0).size()][finalTemp.size()];
            for (int k = 0; k < finalTemp.get(0).size(); k++) {
                if(finalTemp.get(k).size() == finalTemp.get(0).size()) {
                    String [] tempArr = new String[finalTemp.size()];
                    for (int i = 0; i < finalTemp.size(); i++) {
                        tempArr[i] = finalTemp.get(i).get(k);
                    }
                    arrayTemp[k] = tempArr;
                }
                else {
                    String file = "характеристик";
                    if(filetype == 'c') file = "координат";
                    throw new Exception("Проверьте данные в файле " + file + ". Вероятно, в строке " + (k+1) + " пропущенно значение");
                }
            }
        }
        return arrayTemp;
    }
    public static String[][] ReadServerFile(String filename, char filetype) throws Exception {
        List<String> fileStrings = new ArrayList<>();
        File myObj = new File(filename);
        Scanner myReader = new Scanner(myObj, "windows-1251");
        while (myReader.hasNextLine()) {
            String data = myReader.nextLine();
            fileStrings.add(data);
        }
        myReader.close();
        return ReadFromStrings(fileStrings, filetype);
    }

}
