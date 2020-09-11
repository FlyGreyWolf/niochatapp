package com.flygreywolf.bean;

public class Chat extends Msg {


    private String content;

    public Chat() {
        super();
    }

    public Chat(Integer roomId, Integer msgId, Integer msgType, String content) {
        super(roomId, msgId, msgType);
        this.content = content;
    }


    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


}
