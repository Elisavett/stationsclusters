package net.codejava.Resolve.Model;


import com.google.gson.annotations.SerializedName;

public class GroupAndCoordinates {
    private double lat;
    @SerializedName("long")
    private double long_ ;
    private double number_station;
    private double number_group;

    public GroupAndCoordinates(double lat, double long_, double number_station, double number_group) {
        this.lat = lat;
        this.long_ = long_;
        this.number_station = number_station;
        this.number_group = number_group;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLong_() {
        return long_;
    }

    public void setLong_(double long_) {
        this.long_ = long_;
    }

    public double getNumber_station() {
        return number_station;
    }

    public void setNumber_station(double number_station) {
        this.number_station = number_station;
    }

    public double getNumber_group() {
        return number_group;
    }

    public void setNumber_group(double number_group) {
        this.number_group = number_group;
    }
}
