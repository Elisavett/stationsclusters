package net.codejava.Resolve;


import lombok.Getter;

import javax.persistence.*;

@Entity
@Getter
@Table(name = "`50000022`")
public class TemperatureFromBase {
    @Id
    @Column(name = "time")
    long time;
    @Column(name = "timezone")
    int timezone;
    @Column(name = "2002")
    double temperature;
}
