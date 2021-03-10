package net.codejava.Resolve.Model;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GeographicCharacters {
    private double center_lat;
    private double center_long;
    private double max_lat;
    private double min_lat;
    private double max_long;
    private double min_long;
    private int number_group;

    public GeographicCharacters(double center_lat, double center_long, double max_lat,
                                int number_group, double max_long, double min_lat,
                                double min_long) {
        this.center_lat = center_lat;
        this.center_long = center_long;
        this.max_lat = max_lat;
        this.number_group = number_group;
        this.max_long = max_long;
        this.min_lat = min_lat;
        this.min_long = min_long;

    }


}
