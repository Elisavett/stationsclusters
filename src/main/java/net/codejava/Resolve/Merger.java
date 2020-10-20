/*
package h4rar.climate.Resolve;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import h4rar.climate.Resolve.Model.Group;
import h4rar.climate.Resolve.Model.GroupAndCoordinates;
import h4rar.climate.Resolve.Model.GroupLine;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

*/
/**
 * Класс служит для сопоставления данных о температуре с соответствующими кординатами станции
 *
 * @version 1.0
 *//*

public class Merger {
    private static final Gson GSON = new GsonBuilder().create();
    String pathCoordinates;
    ArrayList<GroupLine> groupList = new ArrayList<>();
    TreeSet<GroupLine> sortGroupLine;
    ArrayList<ArrayList<Integer>>  groupsWNdoubles;
    double[][] coordinatesSourceTXT;
    GroupAndCoordinates groupAndCoordinates;
    int stationCount;
    List<Future<Group>> arrayGroup;

    public Merger(int stationCount, String pathCoordinates, List<Future<Group>> arrayGroup) {
        this.pathCoordinates = pathCoordinates;
        this.stationCount = stationCount;
        this.arrayGroup = arrayGroup;
    }

    public ArrayList<String> run() throws IOException, ExecutionException, InterruptedException {
        loadGroups();
        sortGroups();
        loadCoordinates();
        ArrayList<String> Grop = getJson();
        return Grop;
    }

    */
/**
     * Достает группы из файла
     *
     * @throws IOException            если файл не существует, это каталог, а не обычный файл, или по какой-либо другой причине не может быть открыт для чтения.
     * @throws ClassNotFoundException Класс сериализованного объекта не найден.
     *//*

    public void loadGroups() throws ExecutionException, InterruptedException {
        //Записываю группы в groupList
        for (int i = 0; i < stationCount; i++) {
            Group group = (Group) arrayGroup.get(i).get();
            int[] array = group.getArray();
            GroupLine groupLine = new GroupLine(array);
            groupList.add(groupLine);
        }
    }

    */
/**
     * Сортирует и удаляет повторяющиеся данные
     *//*

    public void sortGroups() {
        sortGroupLine = new TreeSet<GroupLine>();
        groupsWNdoubles = new ArrayList<ArrayList<Integer>>();
        groupsWNdoubles.add(groupList.get(0).getGroup());
        for (int i = 0; i < groupList.size(); i++) {
            if (groupList.get(i).getGroup().size() > 5) {
                if (!groupsWNdoubles.contains(groupList.get(i).getGroup())) {

                    int flag = 0;
                    for (int j = 0; j < groupsWNdoubles.size(); j++) {

                        if (!groupsWNdoubles.get(j).containsAll(groupList.get(i).getGroup())) {
                            if (groupList.get(i).getGroup().containsAll(groupsWNdoubles.get(j))) {
                                groupsWNdoubles.set(j, groupList.get(i).getGroup());
                                flag = 1;
                            }

                        }
                        else flag = 1;
                    }
                    if (flag == 0) {
                        groupsWNdoubles.add(groupList.get(i).getGroup());
                    }
                }

            }

        }
    }

    public void loadCoordinates() throws IOException {
        //Получаю координаты каждой станции и записываю в coordinatesSourceTXT
        FileReader fr1 = new FileReader(pathCoordinates);
        BufferedReader fr = new BufferedReader(fr1);
        Scanner scan = new Scanner(fr);

        String[] arrRetval;
        String retval;
        coordinatesSourceTXT = new double[stationCount][3];
        for (int column = 0; column < stationCount; column++) {

            String string = scan.nextLine().trim();
            arrRetval = string.split("\\s+");

                retval = arrRetval[1].replace(',', '.');
                double num = Double.parseDouble(retval);
                coordinatesSourceTXT[column][0] = num;
                retval = arrRetval[2].replace(',', '.');
                num = Double.parseDouble(retval);
                coordinatesSourceTXT[column][1] = num;
                retval = arrRetval[0].replace(',', '.');
                num = Double.parseDouble(retval);
                coordinatesSourceTXT[column][2] = num;

        }
        fr.close();
    }


    public ArrayList<String> getJson() {
        //формирую json файл
        ArrayList<String> json = new ArrayList<>();
        int numberGroup = 1;
        for (GroupLine gr : sortGroupLine) {
            for (int j : gr.getGroup()) {
                groupAndCoordinates = new GroupAndCoordinates(
                        coordinatesSourceTXT[j][0],
                        coordinatesSourceTXT[j][1],
                        coordinatesSourceTXT[j][2],
                        numberGroup);
                String jsonData = GSON.toJson(groupAndCoordinates);
                json.add(jsonData);
            }
            numberGroup++;
        }
//        System.out.println(json);
        return json;

    }
}
*/
package net.codejava.Resolve;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.codejava.Resolve.Model.Group;
import net.codejava.Resolve.Model.GroupAndCoordinates;
import net.codejava.Resolve.Model.GroupLine;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Класс служит для сопоставления данных о температуре с соответствующими кординатами станции
 *
 * @version 1.0
 */
public class Merger {
    private static final Gson GSON = new GsonBuilder().create();
    MultipartFile Coordinates;
    ArrayList<GroupLine> groupList = new ArrayList<>();
    TreeSet<GroupLine> sortGroupLine;
    double[][] coordinatesSourceTXT;
    GroupAndCoordinates groupAndCoordinates;
    int stationCount;
    List<Future<Group>> arrayGroup;

    public Merger(int stationCount, MultipartFile fileCoordinates, List<Future<Group>> arrayGroup) {
        this.Coordinates = fileCoordinates;
        this.stationCount = stationCount;
        this.arrayGroup = arrayGroup;
    }

    public ArrayList<String> run() throws IOException, ExecutionException, InterruptedException {
        loadGroups();
        sortGroups();
        loadCoordinates();
        ArrayList<String> Grop = getJson();
        return Grop;
    }

    /**
     * Достает группы из файла
     *
     * @throws IOException            если файл не существует, это каталог, а не обычный файл, или по какой-либо другой причине не может быть открыт для чтения.
     * @throws ClassNotFoundException Класс сериализованного объекта не найден.
     */
    public void loadGroups() throws ExecutionException, InterruptedException {
        //Записываю группы в groupList
        for (int i = 0; i < stationCount; i++) {
            Group group = (Group) arrayGroup.get(i).get();
            int[] array = group.getArray();
            GroupLine groupLine = new GroupLine(array);
            groupList.add(groupLine);
        }
    }

    /**
     * Сортирует и удаляет повторяющиеся данные
     */
    public void sortGroups() {
        sortGroupLine = new TreeSet<GroupLine>();
        for (int i = 0; i < groupList.size(); i++) {
            if (groupList.get(i).getGroup().size() > 5) {
                sortGroupLine.add(groupList.get(i));
            }
            else
            {
                groupList.get(i).lessThenFive();
                sortGroupLine.add(groupList.get(i));
            }

        }

    }

    public void loadCoordinates() throws IOException {
        //Получаю координаты каждой станции и записываю в coordinatesSourceTXT
        //FileReader fr1 = new FileReader(pathCoordinates);
        String line;
        InputStream is = Coordinates.getInputStream();
        BufferedReader fr = new BufferedReader(new InputStreamReader(is));
        Scanner scan = new Scanner(fr);



        String[] arrRetval;
        String retval;
        coordinatesSourceTXT = new double[stationCount][3];
        for (int column = 0; column < stationCount; column++) {

            String string = scan.nextLine().trim();
            arrRetval = string.split("\\s+");

            retval = arrRetval[1].replace(',', '.');
            double num = Double.parseDouble(retval);
            coordinatesSourceTXT[column][0] = num;
            retval = arrRetval[2].replace(',', '.');
            num = Double.parseDouble(retval);
            coordinatesSourceTXT[column][1] = num;
            retval = arrRetval[0].replace(',', '.');
            num = Double.parseDouble(retval);
            coordinatesSourceTXT[column][2] = num;

        }
        fr.close();
    }


    public ArrayList<String> getJson() {
        //формирую json файл
        ArrayList<String> json = new ArrayList<>();
        int numberGroup = 1;
        for (GroupLine gr : sortGroupLine) {
            for (int j : gr.getGroup()) {
                groupAndCoordinates = new GroupAndCoordinates(
                        coordinatesSourceTXT[j][0],
                        coordinatesSourceTXT[j][1],
                        coordinatesSourceTXT[j][2],
                        numberGroup,
                        gr.islessThenFive());
                String jsonData = GSON.toJson(groupAndCoordinates);
                json.add(jsonData);
            }
            numberGroup++;
        }
//        System.out.println(json);
        return json;

    }
}