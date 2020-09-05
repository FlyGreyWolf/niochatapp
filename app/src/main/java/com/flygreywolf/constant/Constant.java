package com.flygreywolf.constant;

public class Constant {

    public static String Connect_Success = "连接成功";
    public static String Connect_Fail = "连接失败";

    public static String Make_Connect = "建立连接";
    public static String Dis_Connect = "断开连接";

    public static String Host = "192.168.31.253";
    public static int Connect_port = 8888;

    public static String UTF8_Encode = "UTF-8";

    public static int MAX_CONTENT_LEN = 1500; // 消息内容最大长度是1500个字节

    public static Short CONNECT_SUCCESS_CMD = 0x0000;

    public static Short ROOM_LIST_CMD = 0X0001; // 表示携带的数据是房间数据
    public static Short ENTER_ROOM_CMD = 0X0002; // 表示在房间当中的心跳包
    public static Short NUM_OF_PEOPLE_IN_ROOM_CMD = 0X0003; // 表示在房间内的人数指令

    public static Short SEND_MSG_CMD = 0X0004; // 发送消息的指令


    /**
     * 广播
     */
    public static String ROOM_LIST_BR_RECEIVER = "com.flygreywolf.broadcast.RoomListBRReceiver";
}
