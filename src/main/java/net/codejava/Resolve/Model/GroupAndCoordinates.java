package net.codejava.Resolve.Model;


import com.google.gson.annotations.SerializedName;
import lombok.Getter;

import java.util.Map;

/*
    Класс для построения json и передач данных о группах на карту
 */

@Getter
public class GroupAndCoordinates {
    private final double lat;
    @SerializedName("long")
    private final double long_ ;
    private final double number_station;
    private final double number_group;
    private final boolean isLessThenMinGroupMembers;
    private final Map<String, String> additionalParams;

    public GroupAndCoordinates(double number_station, double lat, double long_, double number_group, boolean isLessThenN, Map<String, String> additionalParams) {
        this.lat = lat;
        this.long_ = long_;
        this.number_station = number_station;
        this.number_group = number_group;
        this.isLessThenMinGroupMembers = isLessThenN;
        this.additionalParams = additionalParams;
    }
}
