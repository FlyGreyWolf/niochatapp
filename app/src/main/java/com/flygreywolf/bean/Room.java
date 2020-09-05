package com.flygreywolf.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class Room implements Parcelable {
    private Integer roomId;
    private String roomName;
    private String roomMsg;

    public Room() {
    }

    public Room(Integer roomId, String roomName, String roomMsg) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.roomMsg = roomMsg;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getRoomMsg() {
        return roomMsg;
    }

    public void setRoomMsg(String roomMsg) {
        this.roomMsg = roomMsg;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

    }
}
