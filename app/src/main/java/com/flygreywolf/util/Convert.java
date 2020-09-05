package com.flygreywolf.util;

public class Convert {

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

        int value = 0;
        for (int i = 0; i < 4; i++) {
            int shift = (3 - i) * 8;
            value += (byteArr[i] & 0xFF) << shift;
        }
        return value;
    }

    /**
     * short到byte[]
     *
     * @param value
     * @return
     */
    public static byte[] shortToBytes(short value) {
        byte[] result = new byte[2];
        result[0] = (byte) ((value >> 8) & 0xFF);
        result[1] = (byte) (value & 0xFF);
        return result;
    }


    /**
     * byte[]到short
     *
     * @param byteArr
     * @return
     */
    public static short byteArrToShort(byte[] byteArr) {

        short value = 0;
        for (int i = 0; i < 2; i++) {
            int shift = (1 - i) * 8;
            value += (byteArr[i] & 0xFF) << shift;
        }
        return value;
    }


}
