package com.flygreywolf.util;

/**
 * @author FlyGreyWolf
 * @since 2020/9/4
 */
public class ByteArrPrint {

    /**
     * 字节数组以16进制的格式返回
     *
     * @param byteArr
     * @return String
     */
    public static String printByteArr(byte[] byteArr) {

        String res = "";
        for (int i = 0; i < byteArr.length; i++) {
            String hex = Integer.toHexString(byteArr[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            res = res + hex.toUpperCase() + " ";
        }
        return res;
    }
}