package com.ldnet.entities;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by zxs on 2016/3/1.
 */
public class CommunityServices implements Serializable {
    //    "Id":"f54f77c7a0234cd3956cf3e13bb48d82",
//            "Date":"2016-01-12T17:39:01.69",
//            "Title":"陕西金贝儿母婴家政服务有限公司",
//            "Phone":"82216563",
//            "Img"
//            "Tel_Count":3,
//            "Address":"陕西省西安市雁塔区紫竹小区"
    public String Id;
    public String Date;
    public String Title;
    public String Phone;
    public String Img;
    public String Memo;
    public String Tel_Count;
    public String Address;
    public String Lat;
    public String Lng;

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getImg() {
        return Img;
    }

    public void setImg(String img) {
        Img = img;
    }

    public String getMemo() {
        return Memo;
    }

    public void setMemo(String memo) {
        Memo = memo;
    }

    public String getTel_Count() {
        return Tel_Count;
    }

    public void setTel_Count(String tel_Count) {
        Tel_Count = tel_Count;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getLat() {
        return Lat;
    }

    public void setLat(String lat) {
        Lat = lat;
    }

    public String getLng() {
        return Lng;
    }

    public void setLng(String lng) {
        Lng = lng;
    }
}
