package com.ldnet.entities;

import java.io.Serializable;
import java.util.List;

/**
 * Created by lee on 2016/7/1.
 */
public class HouseProperties1 implements Serializable {
    public List<Orientation> Orientation;
    public List<FitmentType> FitmentType;
    public List<RoomType> RoomType;
    public List<RoomDeploy> RoomDeploy;
    public List<RentType> RentType;

    public List<Orientation> getOrientation() {
        return Orientation;
    }

    public void setOrientation(List<Orientation> orientation) {
        Orientation = orientation;
    }

    public List<FitmentType> getFitmentType() {
        return FitmentType;
    }

    public void setFitmentType(List<FitmentType> fitmentType) {
        FitmentType = fitmentType;
    }

    public List<RoomType> getRoomType() {
        return RoomType;
    }

    public void setRoomType(List<RoomType> roomType) {
        RoomType = roomType;
    }

    public List<RoomDeploy> getRoomDeploy() {
        return RoomDeploy;
    }

    public void setRoomDeploy(List<RoomDeploy> roomDeploy) {
        RoomDeploy = roomDeploy;
    }

    public List<RentType> getRentType() {
        return RentType;
    }

    public void setRentType(List<RentType> rentType) {
        RentType = rentType;
    }
}
