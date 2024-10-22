package com.flygreywolf.niosocket;

import android.app.Activity;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.flygreywolf.activity.MainActivity;
import com.flygreywolf.activity.RoomActivity;
import com.flygreywolf.bean.Chat;
import com.flygreywolf.bean.Image;
import com.flygreywolf.bean.Msg;
import com.flygreywolf.bean.RedPacket;
import com.flygreywolf.bean.Room;
import com.flygreywolf.constant.Constant;
import com.flygreywolf.msg.PayLoad;
import com.flygreywolf.util.Application;
import com.flygreywolf.util.Convert;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class NioSocketClient implements Parcelable, Runnable {

    private Selector selector; // 唯一selector

    private SocketChannel socketChannel;
    private int clientId;
    private String host;
    private int port;
    private InetSocketAddress inetSocketAddress;
    private boolean isConnected = false;


    public static final Creator<NioSocketClient> CREATOR = new Creator<NioSocketClient>() {
        @Override
        public NioSocketClient createFromParcel(Parcel in) {
            return new NioSocketClient(in);
        }

        @Override
        public NioSocketClient[] newArray(int size) {
            return new NioSocketClient[size];
        }
    };
    private HashMap<SocketChannel, PayLoad> cache = new HashMap<SocketChannel, PayLoad>(); // 解决拆包、粘包的cache
    private Activity activity;

    public NioSocketClient(String host, int port, Activity activity) {
        createSelector();
        inetSocketAddress = new InetSocketAddress(host, port);
        this.activity = activity;
    }

    public NioSocketClient(int clientId, String host, int port, Activity activity) {
        createSelector();
        inetSocketAddress = new InetSocketAddress(host, port);
        this.clientId = clientId;
        this.activity = activity;
    }

    protected NioSocketClient(Parcel in) {
        clientId = in.readInt();
        host = in.readString();
        port = in.readInt();
        isConnected = in.readByte() != 0;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public void createSelector() { // 创建selector
        if (selector == null) {
            synchronized (NioSocketClient.class) {
                if (selector == null) { // double check
                    try {
                        selector = Selector.open();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public boolean getIsConnected() {
        return this.isConnected;
    }

    public void setIsConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }



    public boolean connect() { // 建立连接过程，true：建立连接成功，false：建立连接失败
        int tryTimes = 0;
        while (true) {     // 死循环直到连接成功
            try {
                socketChannel = SocketChannel.open();
                socketChannel.configureBlocking(false);
                socketChannel.connect(inetSocketAddress);
                while (!socketChannel.finishConnect()) { // 如果建立连接不成功，返回false，建立连接成功，则返回true
                    Thread.sleep(1000); // 1秒1次
                    tryTimes = tryTimes + 1;

                    if (tryTimes == 3) { // 尝试 5 次连接都失败了
                        disConnect();
                        return false;
                    }
                }
                socketChannel.register(selector, SelectionKey.OP_READ);
                setIsConnected(true);
                break;
            } catch (Exception e) {
                e.printStackTrace();
                disConnect();
                return false;
            }
        }
        return true;
    }



    /**
     * 循环读数据的线程
     */
    public void run() { // 不停循环看看有没有读取事件，因为只有读取事件是被动的，其他都是主动的行为

        // 连接建立成功了
        while (true) {
            try {
                if (this.getIsConnected() == false) {
                    return;
                }
                int key = selector.select(1);

                //Log.e("read Thread--->", Thread.currentThread().getName());
                if (key > 0) {
                    Set<SelectionKey> keySet = selector.selectedKeys();
                    Iterator<SelectionKey> iter = keySet.iterator();
                    while (iter.hasNext()) {
                        SelectionKey selectionKey = iter.next();
                        iter.remove();
                        if (selectionKey.isReadable()) {
                            read(selectionKey);
                        }
                    }
                }
            } catch (Exception e) {
                disConnect();
                e.printStackTrace();
                return;
            }
        }
    }

    /**
     * 处理收到的字节数组
     *
     * @param byteArr 字节数组
     * @param pos     当前读到位置
     * @param len     长度
     * @param channel 通道
     */
    public void handleByteArr(byte[] byteArr, int pos, int len, SocketChannel channel) {

        while (len - pos >= 4) {
            byte[] length = new byte[4];
            System.arraycopy(byteArr, pos, length, 0, 4);

            int contentLen = Convert.byteArrToInteger(length);
//            System.out.println("hehehe" + contentLen);
            if (contentLen > Constant.MAX_CONTENT_LEN) {
                Log.e("contentLen > " + Constant.MAX_CONTENT_LEN, "有可能是恶意攻击");
                return;
            }

            PayLoad payLoad = new PayLoad();
            payLoad.setLengthSize(4);
            payLoad.setLength(length);


            pos = pos + 4;

            if (len - pos >= contentLen) { // 可以读完
                byte[] content = new byte[contentLen];
                System.arraycopy(byteArr, pos, content, 0, contentLen);
                payLoad.setContent(content);
                pos = pos + contentLen;

                parse(payLoad, socketChannel);

            } else { // 读不完，发生拆包问题
                byte[] content = new byte[contentLen];
                System.arraycopy(byteArr, pos, content, 0, len - pos);
                payLoad.setContent(content);
                payLoad.setPosition(len - pos);
                pos = len;
                //System.out.println("发生拆包，只读取到一部分"+new String(content));
                cache.put(channel, payLoad);
            }
        }

        // 头部不全
        if (len - pos > 0 && len - pos < 4) {
            byte[] length = new byte[4];
            PayLoad payLoad = new PayLoad();
            System.arraycopy(byteArr, pos, length, 0, len - pos);
            payLoad.setLengthSize(len - pos);
            payLoad.setLength(length);
            pos = len;
            cache.put(channel, payLoad);
        }
    }

    public void read(SelectionKey selectionKey) {
        SocketChannel channel = (SocketChannel) selectionKey.channel();
        try {

            ByteBuffer byteBuffer = ByteBuffer.allocate(256);

            int len = channel.read(byteBuffer); // 读到的长度


            int pos = 0;

            if (len > 0) {

                byte[] byteArr = byteBuffer.array();
                if (cache.containsKey(channel)) {

                    PayLoad payLoad = cache.get(channel);

                    if (payLoad.getLengthSize() == 4) { // 头部完整
                        int remainLen = Convert.byteArrToInteger(payLoad.getLength()) - payLoad.getPosition();

                        if (len >= remainLen) { // 可以读完
                            cache.remove(channel);
                            System.arraycopy(byteArr, pos, payLoad.getContent(), payLoad.getPosition(), remainLen);
                            pos = pos + remainLen;

                            parse(payLoad, socketChannel);

                            // 还要把剩下的字节处理完
                            handleByteArr(byteArr, pos, len, channel);

                        } else { // 读不完，发生拆包问题
                            System.arraycopy(byteArr, pos, payLoad.getContent(), payLoad.getPosition(), len - pos);
                            payLoad.setPosition(payLoad.getPosition() + len - pos);
                        }
                    } else { // 头部不完整
                        int headRemainBytes = 4 - payLoad.getLengthSize();

                        if (len >= headRemainBytes) { // 可以组装成完整的头部了
                            System.arraycopy(byteArr, pos, payLoad.getLength(), payLoad.getLengthSize(), headRemainBytes);
                            payLoad.setLengthSize(4);
                            pos = pos + headRemainBytes;
                            int contentLen = Convert.byteArrToInteger(payLoad.getLength());
                            if (contentLen > Constant.MAX_CONTENT_LEN) {
                                Log.e("contentLen > " + Constant.MAX_CONTENT_LEN, "有可能是恶意攻击");
                            }
                            if (len - pos >= contentLen) { // 可以读完
                                cache.remove(channel);
                                byte[] content = new byte[contentLen];
                                System.arraycopy(byteArr, pos, content, 0, contentLen);
                                payLoad.setContent(content);
                                pos = pos + contentLen;

                                parse(payLoad, socketChannel);

                                // 还要把剩下的字节处理完
                                handleByteArr(byteArr, pos, len, channel);
                            } else { // 读不完，发生拆包问题
                                byte[] content = new byte[contentLen];
                                System.arraycopy(byteArr, pos, content, 0, len - pos);
                                payLoad.setContent(content);
                                payLoad.setPosition(len - pos);
                                pos = len;
                                //System.out.println("发生拆包，只读取到一部分"+new String(content));
                            }

                        } else { // 还是没能组装成完整头部
                            System.arraycopy(byteArr, pos, payLoad.getLength(), payLoad.getLengthSize(), len - pos);
                            payLoad.setLengthSize(payLoad.getLengthSize() + len - pos);
                            pos = len;
                        }
                    }

                } else { // 无缓存，代表是新的数据包
                    handleByteArr(byteArr, pos, len, channel);
                }
            } else {
                channel.close();
                selectionKey.cancel();
                Log.d("fuck", "服务器断开连接");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送数据
     *
     * @param msg
     * @return true/false
     */
    public boolean send(byte[] cmd, String msg) {
        SocketChannel channel = this.socketChannel;
        ByteBuffer byteBuffer = null;
        try {
            byte[] msgByteArr = msg.getBytes(Constant.UTF8_Encode);
            byte[] combine = new byte[cmd.length + msgByteArr.length];
            System.arraycopy(cmd, 0, combine, 0, cmd.length);
            System.arraycopy(msgByteArr, 0, combine, cmd.length, msgByteArr.length);


            int contentLen = combine.length;
            byteBuffer = ByteBuffer.allocate(4 + contentLen);

            byteBuffer.put(Convert.intToBytes(contentLen));

            byteBuffer.put(combine);

            byteBuffer.flip();
            System.out.println("contentLen" + contentLen);
            //System.out.println("[client] send " + contentLen + "bytes--->" + ByteArrPrint.printByteArr(combine));
        } catch (UnsupportedEncodingException e) { // 不支持该编码
            e.printStackTrace();
            disConnect();
            return false;
        }

        synchronized (this) {
            while (byteBuffer.hasRemaining()) {
                try {
                    channel.write(byteBuffer);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    disConnect();
                    return false;
                }
            }
        }

        return true;
    }

    public void parse(PayLoad payLoad, SocketChannel socketChannel) {
        short cmd = Convert.byteArrToShort(payLoad.getContent()); // 0和1个字节代表cmd
        String msg = null;
        try {
            msg = new String(payLoad.getContent(), 2, payLoad.getContent().length - 2, Constant.UTF8_Encode);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        System.out.println("cmd:" + cmd);
        if (cmd == Constant.ROOM_LIST_CMD) { // 收到roomList
            List<Room> roomList = JSONArray.parseArray(msg, Room.class);
            Application.appMap.put("roomList", roomList);
            ((MainActivity) activity).updateRoomListView((ArrayList<Room>) roomList); // 更新房间列表
        } else if (cmd == Constant.NUM_OF_PEOPLE_IN_ROOM_CMD) {
            ((RoomActivity) activity).updateTitle(msg); // 更新房间列表
        } else if (cmd == Constant.SEND_CHAT_CMD) { // 发送文字消息成功了
            Msg msgObj = JSON.parseObject(msg, Msg.class);
            if (msgObj.getMsgType() == Constant.MY_TEXT_TYPE || msgObj.getMsgType() == Constant.OTHER_TEXT_TYPE) { // 是文本类型，就转为Chat类型对象
                msgObj = JSON.parseObject(msg, Chat.class);
            }
            ((RoomActivity) activity).updateChatList(msgObj);

        } else if (cmd == Constant.SNED_RED_PACKET_CMD) { // 发送红包消息成功了
            Msg msgObj = JSON.parseObject(msg, Msg.class);
            if (msgObj.getMsgType() == Constant.MY_PACKET_TYPE || msgObj.getMsgType() == Constant.OTHER_PACKET_TYPE) { // 是红包类型，就转为RedPacket类型对象
                msgObj = JSON.parseObject(msg, RedPacket.class);
            }
            ((RoomActivity) activity).updateChatList(msgObj);

        } else if (cmd == Constant.GET_RED_PACKET_CMD) {
            RedPacket redPacket = JSON.parseObject(msg, RedPacket.class);
            ((RoomActivity) activity).turnToPacketInfo(redPacket);
        } else if (cmd == Constant.SEND_IMG_CMD) { // img端口
            Msg msgObj = JSON.parseObject(msg, Msg.class);
            Log.e("sl", msgObj + "");

            if (msgObj.getMsgType() == Constant.MY_IMG_TYPE || msgObj.getMsgType() == Constant.OTHER_IMG_TYPE) { // 是图片类型，就转为Image类型对象
                msgObj = JSON.parseObject(msg, Image.class);
            }
            ((RoomActivity) activity).updateChatList(msgObj);


            if (msgObj.getMsgType() == Constant.MY_IMG_TYPE) {
                List<String> params = new ArrayList<>(2);
                params.add(msgObj.getMsgId() + "");
                params.add(msgObj.getRoomId() + "");
                ((NioSocketClient) Application.appMap.get(Application.param1)).
                        send(Convert.shortToBytes(Constant.IS_SEND_IMG_YOU_CMD), JSON.toJSONString(params));
            }

        }
    }

    public SocketChannel getSocketChannel() {
        return this.socketChannel;
    }

    /**
     * 断开连接
     */
    public void disConnect() {
        setIsConnected(false);

        try {
            if (socketChannel != null) {
                socketChannel.close();
                socketChannel = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(clientId);
        parcel.writeString(host);
        parcel.writeInt(port);
        parcel.writeByte((byte) (isConnected ? 1 : 0));

    }

}