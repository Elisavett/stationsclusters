package net.codejava.controller;

import net.codejava.Resolve.Model.GroupLine;
import net.codejava.Resolve.Model.ResolveForm;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

@Controller
public class DownloadFilesController {
    @RequestMapping("/downloadFrequency")
    public ResponseEntity<String> downloadFrequency(){

        StringBuilder stringPhase = new StringBuilder();
        for(int i = 1; i< ResolveForm.frequencyAnalysis.size(); i++) {
            stringPhase.append(i).append(" ").append(ResolveForm.frequencyAnalysis.get(i));
            stringPhase.append("\n");
        }
        return getFile("frequencyAnalysis", stringPhase.toString());
    }
    @RequestMapping("/downloadTypicals")
    public ResponseEntity<String> downloadTypicals() throws ExecutionException, InterruptedException {

        StringBuilder stringPhase = new StringBuilder();
        for (int i = 0; i < ResolveForm.arrayTypical.get(0).get().getArray().length; i++) {
            StringBuilder output = new StringBuilder();
            for (int j = 0; j < ResolveForm.arrayTypical.size(); j++) {
                output.append(ResolveForm.arrayTypical.get(j).get().getArray()[i]).append(" ");
            }
            stringPhase.append(output).append("\n");
        }

        return getFile("typicals", stringPhase.toString());
    }
    @RequestMapping("/downloadGroups")
    public ResponseEntity<String> downloadGroups() throws ExecutionException, InterruptedException {

        StringBuilder stringPhase = new StringBuilder();
        for (int i = 0; i < ResolveForm.arrayGroup.get(0).get().getArray().length; i++) {
            StringBuilder output = new StringBuilder();
            for (int j = 0; j < ResolveForm.arrayGroup.size(); j++) {
                output.append(ResolveForm.arrayGroup.get(j).get().getArray()[i]).append(" ");
            }
            stringPhase.append(output).append("\n");
        }

        return getFile("groups", stringPhase.toString());
    }
    @RequestMapping("/downloadJSON")
    public ResponseEntity<String> downloadJSON(){
        StringBuilder stringPhase = new StringBuilder();
        for (int i = 0; i < ResolveForm.json.size(); i++) {
            stringPhase.append(ResolveForm.json.get(i)).append("\n");
        }

        return getFile("groups", stringPhase.toString());
    }
    @RequestMapping("/downloadGeographicCharacters")
    public ResponseEntity<String> downloadGeographicCharacters() throws ExecutionException, InterruptedException {

        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        StringBuilder outputString = new StringBuilder("Дата расчета: " + dateFormat.format(Calendar.getInstance().getTime()) + "\n");
        outputString.append("Рассчетный период: с ").append(ResolveForm.periodStart).append(" по ").append(ResolveForm.periodEnd).append("\n");
        outputString.append("Расчет выполнялся по ").append(ResolveForm.isForPhases ? "фазе" : "амплитуде").append("\n");
        outputString.append("Размер окна: нач. частота ").append(ResolveForm.windowLeft).append(", конеч. частота : ").append(ResolveForm.windowRight).append(", несущая частота: ").append(ResolveForm.windowCenter).append("\n");
        outputString.append("Коэффициент корреляции: минимальный ").append(ResolveForm.corrDOWN).append(", максимальный ").append(ResolveForm.corrUP).append("\n");
        outputString.append("Минимальное кол-во эл. в кластере: ").append(ResolveForm.minGroupSize).append("\n");
        outputString.append("\nТиповые температуры" + "\n");
        StringBuilder stringPhase = new StringBuilder(ResolveForm.isForPhases ? "\nТиповые фазы" : "\nТиповые амплитуды" + "\n");
        StringBuilder stringAmpl = new StringBuilder(ResolveForm.isForPhases ? "\nТиповые амплитуды" : "\nТиповые фазы" + "\n");
        StringBuilder stringClusters = new StringBuilder("\nСтанции в кластерах " + "\n");
        int l = 0;
        for (GroupLine cluster : ResolveForm.clusters) {


            ArrayList<Integer> group = cluster.getGroup();
            if(group.size() > ResolveForm.minGroupSize) {
                l++;

                stringClusters.append("Кластер_").append(l).append(" ");
                for (double station:group) {
                    stringClusters.append((int)station).append(" ");
                }
                stringClusters.append("\n");

                stringPhase.append("Кластер_").append(l).append(" ");
                for (double phase:cluster.getTypicals()) {
                    stringPhase.append(Math.round(phase*1000)/1000.0).append(" ");
                }
                stringPhase.append("\n");

                stringAmpl.append("Кластер_").append(l);
                for (int j = 0; j < ResolveForm.arrayAmplitude.get(0).get().getArray().length; j++) {
                    double average = 0;
                    for (Integer station : group) {
                        average += ResolveForm.arrayAmplitude.get(station).get().getArray()[j];
                    }
                    stringAmpl.append(" ").append(Math.round(average/group.size()*1000)/1000.0).append(" ");
                }
                stringAmpl.append("\n");

                outputString.append("Кластер_").append(l);
                for (int j = 0; j < ResolveForm.TempData[0].length; j++) {
                    double average = 0;
                    for (Integer station : group) {
                        average += ResolveForm.TempData[station][j];
                    }
                    outputString.append(" ").append(Math.round(average/group.size()*1000)/1000.0).append(" ");
                }
                outputString.append("\n");
            }
        }
        outputString.append(stringAmpl);
        outputString.append(stringPhase);
        outputString.append(stringClusters);


        outputString.append("\nГеографические характеристики\n");
        outputString.append("1.номер_кластера 2.широта_центра 3.долгота_центра 4.макс_широта 5.мин_широта 6.макс_долгота 7.мин_долгота  \n");

        for (int i = 0; i < ResolveForm.geoChars.size(); i++) {
            outputString.append((int) ResolveForm.geoChars.get(i)[0]).append(" ");
            for(int j = 1; j < ResolveForm.geoChars.get(i).length; j++)
            {
                outputString.append(ResolveForm.geoChars.get(i)[j]).append(" ");
            }
            outputString.append("\n");
        }

        return getFile("groupGeoCharacteristics", outputString.toString());
    }

    @RequestMapping("/downloadSKO")
    public ResponseEntity<String> downloadSKO(){
        StringBuilder stringPhase = new StringBuilder();

        for(int i = 1; i< ResolveForm.SKO.size(); i++) {
            stringPhase.append(i).append(" ").append(ResolveForm.SKO.get(i));
            stringPhase.append("\n");
        }


        return getFile("SKO", stringPhase.toString());
    }
    private ResponseEntity<String> getFile(String fileName, String outputStr){
        MediaType mediaType = new MediaType("text", "plain", Charset.defaultCharset());
        return ResponseEntity.ok()
                // Content-Disposition
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName +".txt")
                // Content-Type
                .contentType(mediaType)
                .body(outputStr);
    }
}
