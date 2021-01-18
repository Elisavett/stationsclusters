package net.codejava.Resolve;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.codejava.Resolve.Model.*;
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
    ArrayList<GroupLine> groupList = new ArrayList<>();
    TreeSet<GroupLine> sortGroupLine;
    double[][] coordinatesSourceTXT;
    //GroupAndCoordinates groupAndCoordinates;
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
            if (groupList.get(i).getGroup().size() >= minGroupsSize) {
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
        coordinatesSourceTXT = new double[3][stationCount];
        coordinatesSourceTXT[0] = ResolveForm.coordData[1];
        coordinatesSourceTXT[1] = ResolveForm.coordData[2];
        coordinatesSourceTXT[2] = ResolveForm.coordData[0];
    }


    public ArrayList<String> getJson() {
        //формирую json файл
        ArrayList<String> json = new ArrayList<>();
        int numberGroup = 1;
        for (GroupLine gr : sortGroupLine) {
            if(!gr.islessThenFive()) {
                for (int j : gr.getGroup()) {
                   /* groupAndCoordinates = new GroupAndCoordinates(
                            coordinatesSourceTXT[0][j],
                            coordinatesSourceTXT[1][j],
                            coordinatesSourceTXT[2][j],
                            numberGroup,
                            gr.islessThenFive());*/
                    //String jsonData = GSON.toJson(groupAndCoordinates);
                    String jsonData = GSON.toJson(new String[] {String.valueOf(coordinatesSourceTXT[0][j]),
                            String.valueOf(coordinatesSourceTXT[1][j]),
                            String.valueOf(coordinatesSourceTXT[2][j]),
                                    String.valueOf(numberGroup),
                                            String.valueOf(gr.islessThenFive())});
                    json.add(jsonData);
                }
                numberGroup++;
            }

        }
        for (GroupLine gr : sortGroupLine) {
            if(gr.islessThenFive()) {
                for (int j : gr.getGroup()) {
                    String jsonData = GSON.toJson(new String[] {String.valueOf(coordinatesSourceTXT[0][j]),
                            String.valueOf(coordinatesSourceTXT[1][j]),
                            String.valueOf(coordinatesSourceTXT[2][j]),
                            String.valueOf(numberGroup),
                            String.valueOf(gr.islessThenFive())});
                    json.add(jsonData);
                }
                numberGroup++;
            }
        }

//        System.out.println(json);
        return json;

    }
}