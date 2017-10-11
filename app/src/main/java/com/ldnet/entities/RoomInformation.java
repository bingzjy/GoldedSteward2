package com.ldnet.entities;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Alex on 2015/9/2.
 */
public class RoomInformation implements Serializable {
    public String Abbreviation;
    public String Buildarea;
    public List<Meter> Meter;

    public String getAbbreviation() {
        return Abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        Abbreviation = abbreviation;
    }

    public String getBuildarea() {
        return Buildarea;
    }

    public void setBuildarea(String buildarea) {
        Buildarea = buildarea;
    }

    public List<Meter> getMeter() {
        return Meter;
    }

    public void setMeter(List<Meter> meter) {
        Meter = meter;
    }
}
