package com.flygreywolf.bean;

public class Msg {

    private Integer roomId;
    private Integer msgId;
    private Integer msgType;

    public Msg() {
    }

    public Msg(Integer roomId, Integer msgId, Integer msgType) {
        this.roomId = roomId;
        this.msgId = msgId;
        this.msgType = msgType;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    public Integer getMsgId() {
        return msgId;
    }

    public void setMsgId(Integer msgId) {
        this.msgId = msgId;
    }

    public Integer getMsgType() {
        return msgType;
    }

    public void setMsgType(Integer msgType) {
        this.msgType = msgType;
    }
}
