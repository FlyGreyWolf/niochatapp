package com.flygreywolf.bean;

public class Chat {

    private Integer roomId;
    private Integer msgId;
    private String msg;
    private boolean isMe;

    public Chat() {
    }

    public Chat(String msg, boolean isMe) {
        this.msg = msg;
        this.isMe = isMe;
    }

    public Chat(Integer roomId, String msg, boolean isMe) {
        this.roomId = roomId;
        this.msg = msg;
        this.isMe = isMe;
    }

    public Chat(Integer roomId, Integer msgId, String msg, boolean isMe) {
        this.roomId = roomId;
        this.msgId = msgId;
        this.msg = msg;
        this.isMe = isMe;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public boolean isMe() {
        return isMe;
    }

    public void setMe(boolean me) {
        isMe = me;
    }

    public Integer getMsgId() {
        return msgId;
    }

    public void setMsgId(Integer msgId) {
        this.msgId = msgId;
    }

    @Override
    public String toString() {
        return "Chat{" +
                "roomId=" + roomId +
                ", msgId=" + msgId +
                ", msg='" + msg + '\'' +
                ", isMe=" + isMe +
                '}';
    }
}
