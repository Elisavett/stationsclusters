package net.codejava.controller;

import net.codejava.Resolve.Model.Group;
import net.codejava.Resolve.Model.ResolveForm;
import net.codejava.Resolve.Report;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.nio.charset.Charset;

@Controller
public class DownloadFilesController {
    @RequestMapping("/downloadPhases")
    public ResponseEntity<String> downloadFile1() {

        StringBuilder stringPhase = new StringBuilder();
        for (int i = 0; i < ResolveForm.arrayPhase.get(0).getPhase().size(); i++) {
            StringBuilder output = new StringBuilder();
            for (int j = 0; j < ResolveForm.arrayPhase.size(); j++) {
                output.append(ResolveForm.arrayPhase.get(j).getPhase().get(i)).append(" ");
            }
            stringPhase.append(output).append("\n");
        }

        return getFile("phases", stringPhase.toString());
    }
    @RequestMapping("/downloadAmplitudeSpector")
    public ResponseEntity<String> downloadAmplitudeSpector() {

        StringBuilder stringAmplitude = new StringBuilder();
        for (int i = 0; i < ResolveForm.amplitudeSpector.size(); i++) {
            stringAmplitude.append(i).append(" ").append(ResolveForm.amplitudeSpector.get(i)).append("\n");
        }

        return getFile("amplitudeSpector", stringAmplitude.toString());
    }
    @RequestMapping("/downloadPhaseSpector")
    public ResponseEntity<String> downloadPhaseSpector() {

        StringBuilder stringPhase = new StringBuilder();
        for (int i = 0; i < ResolveForm.phaseSpector.size(); i++) {
            stringPhase.append(i).append(" ").append(ResolveForm.phaseSpector.get(i)).append("\n");
        }

        return getFile("phaseSpector", stringPhase.toString());
    }
    @RequestMapping("/downloadFrequency")
    public ResponseEntity<String> downloadFrequency(){

        StringBuilder stringPhase = new StringBuilder();
        for(int i = 1; i< ResolveForm.frequencyAnalysis.size(); i++) {
            stringPhase.append(i).append(" ").append(ResolveForm.frequencyAnalysis.get(i));
            stringPhase.append("\n");
        }
        return getFile("frequencyAnalysis", stringPhase.toString());
    }
    @RequestMapping("/downloadClusterInfo")
    public ResponseEntity<String> downloadClusterInfo(){

        StringBuilder clusterInfo = new StringBuilder();
        int i = 0;
        clusterInfo.append("группа станция широта долгота").append("\n");
        for(Group group : ResolveForm.clusters){
            i++;
            if(group.getGroupMembers().size() >= ResolveForm.minGroupSize) {
                for (int member : group.getGroupMembers()) {
                    clusterInfo.append(i).append(" ").append(member);
                    clusterInfo.append(" ").append(ResolveForm.coordData[1][member]).append(" ").append(ResolveForm.coordData[2][member]);
                    clusterInfo.append("\n");
                }
            }
        }
        return getFile("groupsInfo", clusterInfo.toString());
    }

    @RequestMapping("/downloadTypicals")
    public ResponseEntity<String> downloadTypicals() {

        StringBuilder stringPhase = new StringBuilder();
        for (int i = 0; i < ResolveForm.arrayTypical.get(0).getPhase().size(); i++) {
            StringBuilder output = new StringBuilder();
            for (int j = 0; j < ResolveForm.arrayTypical.size(); j++) {
                output.append(ResolveForm.arrayTypical.get(j).getPhase().get(i)).append(" ");
            }
            stringPhase.append(output).append("\n");
        }

        return getFile("typicals", stringPhase.toString());
    }
    @RequestMapping("/downloadGroups")
    public ResponseEntity<String> downloadGroups() {

        StringBuilder stringPhase = new StringBuilder();
        for (int i = 0; i < ResolveForm.arrayGroup.get(0).getGroupMembers().size(); i++) {
            StringBuilder output = new StringBuilder();
            for (int j = 0; j < ResolveForm.arrayGroup.size(); j++) {
                output.append(ResolveForm.arrayGroup.get(j).getGroupMembers().get(i)).append(" ");
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
    @RequestMapping("/downloadReport")
    public ResponseEntity<String> downloadGeographicCharacters() {
        return getFile(ResolveForm.resolveTime, Report.getReport());
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
