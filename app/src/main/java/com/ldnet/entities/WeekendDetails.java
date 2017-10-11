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
public class WeekendDetails implements Serializable {

    //ID
    public String Id;
    //标题
    public String Title;
    //发布人ID
    public String ResidentId;
    //状态，0 - 未开始 1 - 进行中 2 - 已结束
    public Integer Status;
    //费用
    public Float Cost;
    //所在城市ID
    public Integer CityId;
    //所在城市
    public String CityName;
    //开始时间
    public String StartDatetime;
    //结束时间
    public String EndDatetime;
    //活动地点
    public String ActiveAddress;
    //图片，最多5张
    public String[] Img;
    //报名人数
    public Integer MemberCount;
    //活动介绍
    public String Memo;
    //联系人
    public String ContractName;
    //联系电话
    public String ContractTel;

    public Boolean IsRecord;

//    //获取图片的IDs
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

    public String getResidentId() {
        return ResidentId;
    }

    public void setResidentId(String residentId) {
        ResidentId = residentId;
    }

    public Integer getStatus() {
        return Status;
    }

    public void setStatus(Integer status) {
        Status = status;
    }

    public Float getCost() {
        return Cost;
    }

    public void setCost(Float cost) {
        Cost = cost;
    }

    public Integer getCityId() {
        return CityId;
    }

    public void setCityId(Integer cityId) {
        CityId = cityId;
    }

    public String getCityName() {
        return CityName;
    }

    public void setCityName(String cityName) {
        CityName = cityName;
    }

    public String getStartDatetime() {
        return StartDatetime;
    }

    public void setStartDatetime(String startDatetime) {
        StartDatetime = startDatetime;
    }

    public String getEndDatetime() {
        return EndDatetime;
    }

    public void setEndDatetime(String endDatetime) {
        EndDatetime = endDatetime;
    }

    public String getActiveAddress() {
        return ActiveAddress;
    }

    public void setActiveAddress(String activeAddress) {
        ActiveAddress = activeAddress;
    }

    public String[] getImg() {
        return Img;
    }

    public void setImg(String[] img) {
        Img = img;
    }

    public Integer getMemberCount() {
        return MemberCount;
    }

    public void setMemberCount(Integer memberCount) {
        MemberCount = memberCount;
    }

    public String getMemo() {
        return Memo;
    }

    public void setMemo(String memo) {
        Memo = memo;
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

    public Boolean getIsRecord() {
        return IsRecord;
    }

    public void setIsRecord(Boolean isRecord) {
        IsRecord = isRecord;
    }
}
