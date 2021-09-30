package net.codejava.Resolve;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.codejava.Resolve.Model.*;

import java.util.*;

/**
 * Класс служит для сопоставления данных о температуре с соответствующими кординатами станции
 *
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
                gr.setGroupCorrTable(getGroupCorrTable(gr));
            }
        }
        sortGroups = new TreeSet<>();
        sortGroups.addAll(groupList);
        ResolveForm.clusters = sortGroups;
    }

    //Таблица корреляции групп
    public static List<List<Double>> getGroupCorrTable(Group group){
        List<Integer> groupMembers = group.getGroupMembers();
        List<List<Double>> groupCorrTable = new ArrayList<>();
        int m0 = groupMembers.get(0);
        int m1 = groupMembers.get(groupMembers.size()-1);
        double currCorr = 1;
        if(groupMembers.size() > 1)
            currCorr = Math.round(ResolveForm.arrayCorr.get(Math.min(m1, m0)).get(Math.abs(m1 - m0) -1)*1000)/1000.0;
        double minCorr = currCorr;
        double maxCorr = currCorr;
        for(int member1 : groupMembers){
            List<Double> memberCorrs = new ArrayList<>();
            for(int member2 : groupMembers){
                if(member1 == member2) memberCorrs.add(1.);
                else {
                    currCorr = Math.round(ResolveForm.arrayCorr.get(Math.min(member1, member2)).get(Math.abs(member1 - member2) - 1)*1000)/1000.0;
                    memberCorrs.add(currCorr);
                }
                if(currCorr > maxCorr)
                    maxCorr = currCorr;
                if(currCorr < minCorr)
                    minCorr = currCorr;
            }
            groupCorrTable.add(memberCorrs);
        }
        group.setMaxGroupCorr(maxCorr);
        group.setMinGroupCorr(minCorr);
        return groupCorrTable;
    }

    //Преобразование данных для отображения на карте
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