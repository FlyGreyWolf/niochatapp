package com.flygreywolf.niosocket;

import android.util.Log;

import com.flygreywolf.constant.Constant;
import com.flygreywolf.msg.PayLoad;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class NioSocketClient extends Thread {

    private static Selector selector; // 唯一selector

    private SocketChannel socketChannel;
    private int clientId;
    private String host;
    private int port;
    private InetSocketAddress inetSocketAddress;
    private boolean isConnected = false;

    private static HashMap<SocketChannel, PayLoad> cache = new HashMap<SocketChannel, PayLoad>(); // 解决拆包、粘包的cache

    public NioSocketClient(String host, int port) {
        createSelector();
        inetSocketAddress = new InetSocketAddress(host, port);
    }

    public NioSocketClient(int clientId, String host, int port) {
        createSelector();
        inetSocketAddress = new InetSocketAddress(host, port);
        this.clientId = clientId;

    }

    public void createSelector() { // 创建selector
        if (selector == null) {
            synchronized (NioSocketClient.class) {
                if (selector == null) { //double check
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

                    if (tryTimes == 5) { // 尝试 5 次连接都失败了
                        disConnect();
                        return false;
                    }
                }
                socketChannel.register(selector, SelectionKey.OP_READ);
                setIsConnected(true);
                break;
            } catch (Exception e) {
                disConnect();
                return false;
            }
        }
        return true;
    }

    /**
     * int到byte[]
     *
     * @param
     * @return
     */
    public static byte[] intToBytes(int value) {
        byte[] result = new byte[4];
        // 由高位到低位
        result[0] = (byte) ((value >> 24) & 0xFF);
        result[1] = (byte) ((value >> 16) & 0xFF);
        result[2] = (byte) ((value >> 8) & 0xFF);
        result[3] = (byte) (value & 0xFF);
        return result;
    }

    /**
     * byte[]到int
     *
     * @param byteArr
     * @return
     */
    public static int byteArrToInteger(byte[] byteArr) {

        int res = 0;

        for (int i = 0; i < 4; i++) {
            res = res << 8 | byteArr[i];
        }
        return res;
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

                Log.e("read Thread--->", Thread.currentThread().getName());
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

            int contentLen = byteArrToInteger(length);
            if (contentLen > Constant.MAX_CONTENT_LEN) {
                Log.e("contentLen > 1500", "有可能是恶意攻击");
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
                System.out.println(payLoad.getContent());


            } else { // 读不完，发生拆包问题
                byte[] content = new byte[contentLen];
                System.arraycopy(byteArr, pos, content, 0, len - pos);
                payLoad.setContent(content);
                payLoad.setPosition(len - pos);
                pos = len;
//				System.out.println("发生拆包，只读取到一部分"+new String(content));
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

            ByteBuffer byteBuffer = ByteBuffer.allocate(128);

            int len = channel.read(byteBuffer); // 读到的长度


            int pos = 0;

            if (len > 0) {

                byte[] byteArr = byteBuffer.array();
                if (cache.containsKey(channel)) {

                    PayLoad payLoad = cache.get(channel);

                    if (payLoad.getLengthSize() == 4) { // 头部完整
                        int remainLen = byteArrToInteger(payLoad.getLength()) - payLoad.getPosition();

                        if (len >= remainLen) { // 可以读完
                            cache.remove(channel);
                            System.arraycopy(byteArr, pos, payLoad.getContent(), payLoad.getPosition(), remainLen);
                            pos = pos + remainLen;
                            System.out.println(payLoad.getContent());

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
                            int contentLen = byteArrToInteger(payLoad.getLength());
                            if (contentLen > Constant.MAX_CONTENT_LEN) {
                                Log.e("contentLen > 1500", "有可能是恶意攻击");
                            }
                            if (len - pos >= contentLen) { // 可以读完
                                cache.remove(channel);
                                byte[] content = new byte[contentLen];
                                System.arraycopy(byteArr, pos, content, 0, contentLen);
                                payLoad.setContent(content);
                                pos = pos + contentLen;
                                System.out.println(payLoad.getContent());

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
    public boolean send(String msg) {
        SocketChannel channel = this.socketChannel;
        ByteBuffer byteBuffer = null;
        try {
            byte[] msgByteArr = msg.getBytes(Constant.UTF8_Encode);
            int contentLen = msgByteArr.length;
            byteBuffer = ByteBuffer.allocate(4 + contentLen);
            byteBuffer.put(intToBytes(contentLen));
            byteBuffer.put(msgByteArr);
            byteBuffer.flip();
            System.out.println("[client] send:" + "-- " + contentLen + msg);
        } catch (UnsupportedEncodingException e) { // 不支持该编码
            e.printStackTrace();
            disConnect();
            return false;
        }

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
        return true;


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
}