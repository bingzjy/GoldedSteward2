package com.ldnet.entities;

import java.io.Serializable;

/**
 * Created by zxs on 2016/3/1.
 */
public class Item implements Serializable {
    //    "Id":"10631c764be24d88a73bafc622373613",
//            "Name":"金牌月嫂",
//            "Cost":"12600/26天"
    public String Id;
    public String Name;
    public String Cost;

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getCost() {
        return Cost;
    }

    public void setCost(String cost) {
        Cost = cost;
    }
}
