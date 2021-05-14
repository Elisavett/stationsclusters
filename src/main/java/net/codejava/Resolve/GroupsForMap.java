package net.codejava.Resolve;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.codejava.Resolve.Model.*;

import java.util.*;

/**
 * Класс служит для сопоставления данных о температуре с соответствующими кординатами станции
 *
 * @version 1.0
 */
public class GroupsForMap {
    private static ArrayList<Group> groupList;
    private static TreeSet<Group> sortGroups;

    public static void loadGroups(){
        //Записываю группы в groupList
        groupList = new ArrayList<>();
        groupList.addAll(ResolveForm.arrayGroup);
    }

    /**
     * Сортирует и удаляет повторяющиеся данные
     */
    public static void sortGroups() {
        TreeSet<Group> tempGroups = new TreeSet<>(groupList);
        groupList = new ArrayList<>(tempGroups);

        if(!ResolveForm.groupCross) {
            for (Group gr : groupList) {
                //Удаление вхождений групп в другие группы
                gr.deleteDoubles(groupList);
                //Добавление таблицы корреляции между элементами группы
                gr.setGroupCorrTable(getGroupCorrTable(gr.getGroupMembers()));
            }
        }
        sortGroups = new TreeSet<>();
        sortGroups.addAll(groupList);
        ResolveForm.clusters = sortGroups;
    }
    public static List<List<Double>> getGroupCorrTable(List<Integer> groupMembers){
        List<List<Double>> groupCorrTable = new ArrayList<>();
        for(int member1 : groupMembers){
            List<Double> memberCorrs = new ArrayList<>();
            for(int member2 : groupMembers){
                if (member1 > member2) {
                    memberCorrs.add(Math.round(ResolveForm.arrayCorr.get(member2).get(member1 - member2 - 1)*1000)/1000.0);
                }
                else if (member1 < member2){
                    memberCorrs.add(Math.round(ResolveForm.arrayCorr.get(member1).get(member2 - member1 - 1)*1000)/1000.0);
                }
                else{
                    memberCorrs.add(1.);
                }
            }
            groupCorrTable.add(memberCorrs);
        }
        return groupCorrTable;
    }


    public static ArrayList<String> getJson() {
        Gson GSON = new GsonBuilder().create();
        //формирую json файл
        ArrayList<String> json = new ArrayList<>();
        int numberGroup = 1;
        for (Group gr : sortGroups) {
            boolean isLessThenMinGroupSize;
            isLessThenMinGroupSize = gr.getGroupMembers().size() < ResolveForm.minGroupSize;
            for (int groupMember : gr.getGroupMembers()) {
                Map<String, String> additional = new HashMap<>();
                if(ResolveForm.coordData.length > 3 && ResolveForm.fileParams.length > 1) {
                    int limit = Math.min((ResolveForm.coordData.length-3), ResolveForm.fileParams.length / 2);
                    for (int i = 0; i < limit; i++) {
                        if(Boolean.parseBoolean(ResolveForm.fileParams[i*2+1]))
                            additional.put(ResolveForm.fileParams[i * 2], ResolveForm.coordData[i+3][groupMember]);
                    }
                }
                GroupAndCoordinates groupAndCoordinates = new GroupAndCoordinates(
                        Double.parseDouble(ResolveForm.coordData[0][groupMember]),
                        Double.parseDouble(ResolveForm.coordData[1][groupMember]),
                        Double.parseDouble(ResolveForm.coordData[2][groupMember]),
                        numberGroup,
                        isLessThenMinGroupSize,
                        additional);
                String jsonData = GSON.toJson(groupAndCoordinates);
                json.add(jsonData);
            }
            if(!isLessThenMinGroupSize) numberGroup++;
        }
        //Для отображения номеров групп на карте
        ResolveForm.groupNum = numberGroup-1;
        return json;

    }
}