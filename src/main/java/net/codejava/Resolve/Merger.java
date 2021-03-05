package net.codejava.Resolve;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.codejava.Resolve.Model.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
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
    ArrayList<GroupLine> groupList = new ArrayList<>();
    TreeSet<GroupLine> sortGroupLine;
    double[][] coordinatesSourceTXT;
    GroupAndCoordinates groupAndCoordinates;
    int stationCount;
    List<Future<Group>> arrayGroup;
    int minGroupsSize;

    public Merger(int stationCount, List<Future<Group>> arrayGroup, int minGroupsSize) {
        this.stationCount = stationCount;
        this.arrayGroup = arrayGroup;
        this.minGroupsSize = minGroupsSize;
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
            GroupLine groupLine = new GroupLine(array, group.getCorrs(), i);
            groupList.add(groupLine);
        }
    }

    /**
     * Сортирует и удаляет повторяющиеся данные
     */
    public void sortGroups() {
        sortGroupLine = new TreeSet<GroupLine>();
        for (int i = 0; i < groupList.size(); i++) {
            sortGroupLine.add(groupList.get(i));
        }
        if(!ResolveForm.groupCross) {
            for (GroupLine gr : sortGroupLine) {
                gr.deleteDoubles(sortGroupLine);
            }
        }
    }

    public void loadCoordinates() throws IOException {
        //Получаю координаты каждой станции и записываю в coordinatesSourceTXT
        coordinatesSourceTXT = new double[3][stationCount];
        coordinatesSourceTXT[0] = ResolveForm.coordData[1];
        coordinatesSourceTXT[1] = ResolveForm.coordData[2];
        coordinatesSourceTXT[2] = ResolveForm.coordData[0];
    }


    public ArrayList<String> getJson() {
        //формирую json файл
        ArrayList<String> json = new ArrayList<>();
        ArrayList<String> geoChars = new ArrayList<>();
        int numberGroup = 1;
        for (GroupLine gr : sortGroupLine) {
            double max_lat = -90;
            double max_long = -180;
            double min_lat = 90;
            double min_long = 180;
            if(gr.getGroup().size() >= minGroupsSize) {
                for (int j : gr.getGroup()) {
                    double lat = coordinatesSourceTXT[0][j];
                    double _long = coordinatesSourceTXT[1][j];
                    //Для вывода географических характеристик
                    if(lat > max_lat) max_lat = lat;
                    if(lat < min_lat) min_lat = lat;
                    if(_long > max_long) max_long = _long;
                    if(_long < min_long) min_long = _long;
                        groupAndCoordinates = new GroupAndCoordinates(
                                lat,
                                _long,
                                coordinatesSourceTXT[2][j],
                                numberGroup,
                                false);
                        String jsonData = GSON.toJson(groupAndCoordinates);
                        json.add(jsonData);
                    }
                double[] geoChar = new double[7];
                geoChar[1] = Math.round(10*(min_lat+max_lat)/2)/10.;
                geoChar[2] = Math.round(10*(min_long+max_long)/2)/10.;
                geoChar[3] = max_lat;
                geoChar[4] = min_lat;
                geoChar[5] = max_long;
                geoChar[6] = min_long;
                geoChar[0] = numberGroup;

                //Для вывода географических характеристик
                ResolveForm.geoChars.add(geoChar);
                numberGroup++;
            }
        }

        ResolveForm.groupNum = numberGroup-1;
        for (GroupLine gr : sortGroupLine) {
            if(gr.getGroup().size() < minGroupsSize) {
                for (int j : gr.getGroup()) {
                    groupAndCoordinates = new GroupAndCoordinates(
                            coordinatesSourceTXT[0][j],
                            coordinatesSourceTXT[1][j],
                            coordinatesSourceTXT[2][j],
                            numberGroup,
                            true);
                    String jsonData = GSON.toJson(groupAndCoordinates);
                    json.add(jsonData);
                }
                numberGroup++;
            }
        }

//        System.out.println(json);
        return json;

    }
}