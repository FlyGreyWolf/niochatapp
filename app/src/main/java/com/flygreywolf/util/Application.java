package com.flygreywolf.util;

import com.flygreywolf.niosocket.NioSocketClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Application {

    public static HashMap<String, Object> appMap = new HashMap<>();

    public static final String BIG_IMG_INFO = "bigImgInfo";
    public static final String RED_PACKET_INFO = "redPacketInfo";
    public static String param1 = "nioSocketClient"; // nioSocketClient对象
    public static String param2 = "roomList"; // roomList
    public static String param3 = "room"; // room对象
    public static final List<NioSocketClient> nioclientList = new ArrayList<>();
    public static String imgNioSocketClient = "imgNioSocketClient";
    public static String getBigImgClientNioSocketClient = "getBigImgClientNioSocketClient";
}
