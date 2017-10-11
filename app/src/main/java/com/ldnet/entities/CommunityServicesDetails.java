package com.ldnet.entities;

import android.text.TextUtils;

import java.io.Serializable;
import java.util.List;

/**
 * Created by zxs on 2016/3/1.
 */
public class CommunityServicesDetails implements Serializable {
    //    "Id":"6cbce4b2be794a0fa23cbd44f592645a",
//            "Title":"陕西金贝儿母婴家政服务有限公司",
//            "Memo"
//    "Address":"陕西省西安市雁塔区紫竹花园大厦",
//            "Images":"1243fa3efa0643499d0a133ab41d6807",
//            "Latitude":"34.231641",
//            "Longitude":"108.924856",
//            "Tel_Count":25,
//            "Phone":"82216563",
//            "Tel":"",
//            "Item":
    public String Id;
    public String Title;
    public String Memo;
    public String Address;
    public String Images;
    public String Latitude;
    public String Longitude;
    public String Tel_Count;
    public String Phone;
    public String Tel;
    public List<Item> Item;

    public String getThumbnail() {
        if (!TextUtils.isEmpty(Images)) {
            String[] ImageIds = Images.split(",");
            if (ImageIds.length > 0) {
                return ImageIds[0];
            }
        }
        return null;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getMemo() {
        return Memo;
    }

    public void setMemo(String memo) {
        Memo = memo;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getImages() {
        return Images;
    }

    public void setImages(String images) {
        Images = images;
    }

    public String getLatitude() {
        return Latitude;
    }

    public void setLatitude(String latitude) {
        Latitude = latitude;
    }

    public String getLongitude() {
        return Longitude;
    }

    public void setLongitude(String longitude) {
        Longitude = longitude;
    }

    public String getTel_Count() {
        return Tel_Count;
    }

    public void setTel_Count(String tel_Count) {
        Tel_Count = tel_Count;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getTel() {
        return Tel;
    }

    public void setTel(String tel) {
        Tel = tel;
    }

    public List<Item> getItem() {
        return Item;
    }

    public void setItem(List<Item> item) {
        Item = item;
    }
}
