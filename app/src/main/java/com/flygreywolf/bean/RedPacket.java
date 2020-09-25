package com.flygreywolf.bean;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 红包消息类
 */
public class RedPacket extends Msg {

    private BigDecimal totalMoney;
    private String totalNum;
    private String content;
    private BigDecimal remainMoney;
    private String remainNum;
    private List<User> usersGet;
    private List<BigDecimal> moneyGet;

    public RedPacket() {
        super();
    }

    public RedPacket(BigDecimal totalMoney, String totalNum, String content) {
        this.totalMoney = totalMoney;
        this.totalNum = totalNum;
        this.content = content;
        this.usersGet = new ArrayList<>(Integer.parseInt(totalNum));
        this.moneyGet = new ArrayList<>(Integer.parseInt(totalNum));
    }

    public RedPacket(BigDecimal totalMoney, String totalNum, String content, BigDecimal remainMoney, String remainNum) {
        this.totalMoney = totalMoney;
        this.totalNum = totalNum;
        this.content = content;
        this.remainMoney = remainMoney;
        this.remainNum = remainNum;
        this.usersGet = new ArrayList<>(Integer.parseInt(totalNum));
        this.moneyGet = new ArrayList<>(Integer.parseInt(totalNum));
    }

    public RedPacket(Integer roomId, Integer msgId, Integer msgType, BigDecimal totalMoney, String totalNum, String content, BigDecimal remainMoney, String remainNum) {
        super(roomId, msgId, msgType);
        this.totalMoney = totalMoney;
        this.totalNum = totalNum;
        this.content = content;
        this.remainMoney = remainMoney;
        this.remainNum = remainNum;
        this.usersGet = new ArrayList<>(Integer.parseInt(totalNum));
        this.moneyGet = new ArrayList<>(Integer.parseInt(totalNum));
    }

    public BigDecimal getTotalMoney() {
        return totalMoney;
    }

    public void setTotalMoney(BigDecimal totalMoney) {
        this.totalMoney = totalMoney;
    }

    public String getTotalNum() {
        return totalNum;
    }

    public void setTotalNum(String totalNum) {
        this.totalNum = totalNum;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public BigDecimal getRemainMoney() {
        return remainMoney;
    }

    public void setRemainMoney(BigDecimal remainMoney) {
        this.remainMoney = remainMoney;
    }

    public String getRemainNum() {
        return remainNum;
    }

    public void setRemainNum(String remainNum) {
        this.remainNum = remainNum;
    }

    public List<User> getUsersGet() {
        return usersGet;
    }

    public void setUsersGet(List<User> usersGet) {
        this.usersGet = usersGet;
    }

    public List<BigDecimal> getMoneyGet() {
        return moneyGet;
    }

    public void setMoneyGet(List<BigDecimal> moneyGet) {
        this.moneyGet = moneyGet;
    }

    @Override
    public String toString() {
        return "RedPacket{" +
                "totalMoney=" + totalMoney +
                ", totalNum='" + totalNum + '\'' +
                ", content='" + content + '\'' +
                ", remainMoney=" + remainMoney +
                ", remainNum='" + remainNum + '\'' +
                ", usersGet=" + usersGet +
                ", moneyGet=" + moneyGet +
                '}';
    }
}
