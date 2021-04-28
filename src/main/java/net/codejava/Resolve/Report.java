package net.codejava.Resolve;

import net.codejava.Resolve.Model.Group;
import net.codejava.Resolve.Model.ResolveForm;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class Report {
    private static class clusterGeography{
        private final double max_lat;
        private final double max_long;
        private final double min_lat;
        private final double min_long;
        private final double center_lat;
        private final double center_long;
        private final int groupNum;

        public clusterGeography(double min_lat, double max_lat,
                                double min_long, double max_long,
                                int groupNum){
            this.min_lat = min_lat;
            this.max_lat = max_lat;
            this.min_long = min_long;
            this.max_long = max_long;
            this.groupNum = groupNum;
            this.center_lat = Math.round(10 * (min_lat + max_lat) / 2) / 10.;
            this.center_long = Math.round(10 * (min_long + max_long) / 2) / 10.;

        }

        @Override
        public String toString() {
            return groupNum + " " +
                    center_lat + " " + center_long + " " +
                    max_lat + " " + min_lat + " " +
                    max_long + " " + min_long + "\n";
        }
    }
    public static String getGeoCharacteristics(){
        StringBuilder stringClusterGeo = new StringBuilder("\nГеографические характеристики\n" +
                "1.номер_кластера 2.широта_центра 3.долгота_центра 4.макс_широта 5.мин_широта 6.макс_долгота 7.мин_долгота  \n");

        int numberGroup = 1;
        double max_lat, max_long, min_lat, min_long;
        for (Group gr : ResolveForm.clusters) {
            if(gr.getGroupMembers().size() < ResolveForm.minGroupSize) break;
            max_lat = -90;
            max_long = -180;
            min_lat = 90;
            min_long = 180;
            for (int j : gr.getGroupMembers()) {
                double lat = Double.parseDouble(ResolveForm.coordData[1][j]);
                double _long = Double.parseDouble(ResolveForm.coordData[2][j]);
                    if (lat > max_lat) max_lat = lat;
                    if (lat < min_lat) min_lat = lat;
                    if (_long > max_long) max_long = _long;
                    if (_long < min_long) min_long = _long;
            }

            clusterGeography clusterGeography = new clusterGeography(min_lat, max_lat, min_long, max_long, numberGroup);
            //Для вывода географических характеристик
            stringClusterGeo.append(clusterGeography.toString());
            numberGroup++;
        }
        return stringClusterGeo.toString();
    }
    public static StringBuilder getCalcCharacteristics(){
        if(ResolveForm.resolveTime.equals("")) {
            DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
            ResolveForm.resolveTime = dateFormat.format(Calendar.getInstance().getTime());
        }
        StringBuilder outputString = new StringBuilder("Дата расчета: " + ResolveForm.resolveTime + "\n");
        outputString.append("Рассчетный период: ").append(ResolveForm.periodString).append("\n");
        outputString.append("Расчет выполнялся по ").append(ResolveForm.isForPhases ? "фазе" : "амплитуде").append("\n");
        outputString.append("Размер окна: нач. частота ").append(ResolveForm.windowLeft).append(", конеч. частота : ").append(ResolveForm.windowRight).append(", несущая частота: ").append(ResolveForm.windowCenter).append("\n");
        outputString.append("Коэффициент корреляции: минимальный ").append(ResolveForm.corrDOWN).append(", максимальный ").append(ResolveForm.corrUP).append("\n");
        outputString.append("Минимальное кол-во эл. в кластере: ").append(ResolveForm.minGroupSize).append("\n");
        return outputString;
    }
    public static String getReport() {
        StringBuilder outputString = getCalcCharacteristics();
        outputString.append("\nТиповые температуры" + "\n");
        StringBuilder stringAmpl = new StringBuilder(ResolveForm.isForPhases ? "\nТиповые амплитуды\n" : "\nТиповые фазы\n");
        //StringBuilder stringPhase = new StringBuilder(ResolveForm.isForPhases ? "\nТиповые фазы\n" : "\nТиповые амплитуды\n");
        StringBuilder stringPhase = new StringBuilder(ResolveForm.isForPhases ? "\nТиповые фазы\n" : "\nТиповые амплитуды\n");
        StringBuilder stringStations = new StringBuilder();
        int l = 0;
        for (Group cluster : ResolveForm.clusters) {

            List<Integer> group = cluster.getGroupMembers();

            if(group.size() > ResolveForm.minGroupSize) {
                l++;
                //Добавление строки с номерами станций в группе
                stringStations.append("Кластер_").append(l).append(" ")
                            .append(cluster.getGroupString());

                stringPhase.append("Кластер_").append(l).append(" ")
                            .append(cluster.getPhaseString());

                outputString.append("Кластер_").append(l);
                stringAmpl.append("Кластер_").append(l);
                for (int j = 0; j < ResolveForm.TempData[0].length; j++) {
                    double averageAmpl = 0;
                    double averageTemp = 0;
                    for (Integer station : group) {
                        averageAmpl += ResolveForm.arrayAmplitude.get(station).getPhase().get(j);
                        averageTemp += ResolveForm.TempData[station][j];
                    }
                    outputString.append(" ").append(Math.round(averageTemp/group.size()*1000)/1000.0).append(" ");
                    stringAmpl.append(" ").append(Math.round(averageAmpl/group.size()*1000)/1000.0).append(" ");
                }
                stringAmpl.append("\n");
                outputString.append("\n");
            }
        }
        outputString.append("\nСтанции в кластерах " + "\n").append(stringStations);
        outputString.append(stringAmpl);

        outputString.append(stringPhase);

        outputString.append(getGeoCharacteristics());
        return outputString.toString();
    }

}
