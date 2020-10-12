package com.flygreywolf.bean;

public class Image extends Msg {


    private byte[] content;

    public Image() {
    }

    public Image(byte[] content) {
        this.content = content;
    }

    public Image(Integer roomId, Integer msgId, Integer msgType, byte[] content) {
        super(roomId, msgId, msgType);
        this.content = content;
    }


    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

}
