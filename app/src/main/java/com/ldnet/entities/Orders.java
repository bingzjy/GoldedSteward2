package com.ldnet.entities;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by Alex on 2015/9/28.
 */
public class Orders implements Serializable {
    public String OID;//订单ID
    public Integer OVID;//订单子状态ID 1:待付款，3:已发货，4:已签收，5:待发货，6:已关闭，7：取消
    public String OVN;//订单子状态名称
    public Float AM;//总金额
    public Integer AC;//总件数
    public String BID;//商家ID
    public String BN;//商家名称
    public String BM;//商家电话
    public List<OD> OD;//订单明细表
    //----------以下在订单详细接口中才会有的数据---------------
    public String ONB;//订单号
    public Float KM;//配送费
    public String JYN;//交易号
    public String PD;//下单日期
    public String AN;//收货姓名
    public String AMP;//收货电话
    public String AR;//收货省
    public String ACT;//收货市
    public String AA;//收货区
    public String AAD;//收货详细
    public String CM;//取消订单原因
    public String ECode;//快递编码
    public String EName;//快递名称
    public String ENumber;//快递单号

    //是否选中
    public Boolean IsChecked = true;

    public String getOID() {
        return OID;
    }

    public void setOID(String OID) {
        this.OID = OID;
    }

    public Integer getOVID() {
        return OVID;
    }

    public void setOVID(Integer OVID) {
        this.OVID = OVID;
    }

    public String getOVN() {
        return OVN;
    }

    public void setOVN(String OVN) {
        this.OVN = OVN;
    }

    public Float getAM() {
        return AM;
    }

    public void setAM(Float AM) {
        this.AM = AM;
    }

    public Integer getAC() {
        return AC;
    }

    public void setAC(Integer AC) {
        this.AC = AC;
    }

    public String getBID() {
        return BID;
    }

    public void setBID(String BID) {
        this.BID = BID;
    }

    public String getBN() {
        return BN;
    }

    public void setBN(String BN) {
        this.BN = BN;
    }

    public String getBM() {
        return BM;
    }

    public void setBM(String BM) {
        this.BM = BM;
    }

    public List<OD> getOD() {
        return OD;
    }

    public void setOD(List<OD> OD) {
        this.OD = OD;
    }

    public String getONB() {
        return ONB;
    }

    public void setONB(String ONB) {
        this.ONB = ONB;
    }

    public Float getKM() {
        return KM;
    }

    public void setKM(Float KM) {
        this.KM = KM;
    }

    public String getJYN() {
        return JYN;
    }

    public void setJYN(String JYN) {
        this.JYN = JYN;
    }

    public String getPD() {
        return PD;
    }

    public void setPD(String PD) {
        this.PD = PD;
    }

    public String getAN() {
        return AN;
    }

    public void setAN(String AN) {
        this.AN = AN;
    }

    public String getAMP() {
        return AMP;
    }

    public void setAMP(String AMP) {
        this.AMP = AMP;
    }

    public String getAR() {
        return AR;
    }

    public void setAR(String AR) {
        this.AR = AR;
    }

    public String getACT() {
        return ACT;
    }

    public void setACT(String ACT) {
        this.ACT = ACT;
    }

    public String getAA() {
        return AA;
    }

    public void setAA(String AA) {
        this.AA = AA;
    }

    public String getAAD() {
        return AAD;
    }

    public void setAAD(String AAD) {
        this.AAD = AAD;
    }

    public String getCM() {
        return CM;
    }

    public void setCM(String CM) {
        this.CM = CM;
    }

    public String getECode() {
        return ECode;
    }

    public void setECode(String ECode) {
        this.ECode = ECode;
    }

    public String getEName() {
        return EName;
    }

    public void setEName(String EName) {
        this.EName = EName;
    }

    public String getENumber() {
        return ENumber;
    }

    public void setENumber(String ENumber) {
        this.ENumber = ENumber;
    }

    public Boolean getIsChecked() {
        return IsChecked;
    }

    public void setIsChecked(Boolean isChecked) {
        IsChecked = isChecked;
    }
}
