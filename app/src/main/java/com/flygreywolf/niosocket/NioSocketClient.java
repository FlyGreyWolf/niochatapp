package com.flygreywolf.niosocket;

import android.util.Log;

import com.flygreywolf.constant.Constant;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
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

    public void read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        int len = channel.read(byteBuffer);
        if (len > 0) {
            byteBuffer.flip();
            byte[] byteArray = new byte[byteBuffer.limit()];
            byteBuffer.get(byteArray);
            System.out.println("client[" + clientId + "]" + "receive from server:");
            System.out.println(new String(byteArray));
            len = channel.read(byteBuffer);
            byteBuffer.clear();
        }
        key.interestOps(SelectionKey.OP_READ);
    }

    public void run() { // 不停循环看看有没有读取事件，因为只有读取事件是被动的，其他都是主动的行为

        // 连接建立成功了
        while (true) {
            try {
                int key = selector.select(1);
                if (this.getIsConnected() == false) {
                    return;
                }
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