package com.ldnet.entities;

import android.text.TextUtils;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Alex on 2015/9/16.
 */
public class FreaMarketDetails implements Serializable {

    public String Id;
    public String Title;
    public String Memo;
    public String CommunityId;
    public String ContractName;
    public String ContractTel;
    public String ResidentId;
    public String[] Img;
    public String Address;
    public String OrgPrice;
    public Float Price;
    public String Updated;
    public String url;
    //public String Status;
    //public Boolean IsRecord;
    //public String CityName;
    //public int CityId;

    //获取图片的IDs
//    public List<String> ImageIds() {
//        if (!TextUtils.isEmpty(Img)) {
//            try {
//                List<String> imageIds = new ArrayList<String>();
//                JSONArray array = new JSONArray(Img);
//                for (int i = 0; i < array.length(); i++) {
//                    imageIds.add(array.getString(i));
//                }
//                return imageIds;
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//        return null;
//    }

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

    public String getCommunityId() {
        return CommunityId;
    }

    public void setCommunityId(String communityId) {
        CommunityId = communityId;
    }

    public String getContractName() {
        return ContractName;
    }

    public void setContractName(String contractName) {
        ContractName = contractName;
    }

    public String getContractTel() {
        return ContractTel;
    }

    public void setContractTel(String contractTel) {
        ContractTel = contractTel;
    }

    public String getResidentId() {
        return ResidentId;
    }

    public void setResidentId(String residentId) {
        ResidentId = residentId;
    }

    public String[] getImg() {
        return Img;
    }

    public void setImg(String[] img) {
        Img = img;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getOrgPrice() {
        return OrgPrice;
    }

    public void setOrgPrice(String orgPrice) {
        OrgPrice = orgPrice;
    }

    public Float getPrice() {
        return Price;
    }

    public void setPrice(Float price) {
        Price = price;
    }

    public String getUpdated() {
        return Updated;
    }

    public void setUpdated(String updated) {
        Updated = updated;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
