package com.flygreywolf.constant;

public class Constant {

    public static String Connect_Success = "连接成功";
    public static String Connect_Fail = "连接失败";

    public static String Make_Connect = "建立连接";
    public static String Dis_Connect = "断开连接";

    public final static int MY_IMG_TYPE = 5;
    public final static int OTHER_IMG_TYPE = 6;
    //public static String Host = "39.96.58.118";
    public static String Host = "10.21.20.42";
    public static int Connect_port = 6666;
    public static int Img_port = 6667;
    public static int Get_big_img_port = 6668;

    public static String UTF8_Encode = "UTF-8";

    public static int MAX_CONTENT_LEN = Integer.MAX_VALUE; // 消息内容最大长度是1500个字节


    /**
     * 指令
     */
    public static Short ROOM_LIST_CMD = 0X0001; // 表示携带的数据是房间数据
    public static Short ENTER_ROOM_CMD = 0X0002; // 表示在房间当中的心跳包
    public static Short NUM_OF_PEOPLE_IN_ROOM_CMD = 0X0003; // 表示在房间内的人数指令
    public static Short SEND_CHAT_CMD = 0X0004; // 发送消息的指令
    public static Short SNED_RED_PACKET_CMD = 0X0005; // 发送红包的指令
    public static Short GET_RED_PACKET_CMD = 0X0006; // 抢红包的指令
    public static Short SEND_IMG_CMD = 0X0007; // 发送图片
    public static Short IS_SEND_IMG_YOU_CMD = 0X0008; // 询问是否发送图片


    /**
     * 消息类型
     */
    public final static int MY_TEXT_TYPE = 1;
    public final static int OTHER_TEXT_TYPE = 2;
    public final static int MY_PACKET_TYPE = 3;
    public final static int OTHER_PACKET_TYPE = 4;



}
