package com.flygreywolf.msg;

public class PayLoad {

    private byte[] length; // 4字节表示content的长度
    private byte[] content;

    private int position; // content位置
    private int lengthSize; // length数组的长度


    public byte[] getLength() {
        return length;
    }

    public void setLength(byte[] length) {
        this.length = length;
    }

    /**
     * 以16进制打印content的内容
     *
     * @return
     */
    public byte[] getContent() {
        return content;
    }

    public void printContent() {
        System.out.print("printContent:");
        for (int i = 0; i < content.length; i++) {
            String hex = Integer.toHexString(content[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            System.out.print(hex.toUpperCase() + " ");
        }
        System.out.println();
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getLengthSize() {
        return lengthSize;
    }

    public void setLengthSize(int lengthSize) {
        this.lengthSize = lengthSize;
    }


}
