package com.flygreywolf.inputfilter;

import android.graphics.Color;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PacketNumInputFilter implements InputFilter {

    //输入的最大数量
    private static final int MAX_VALUE = 9;
    // 输入的最小数量
    private static final int MIN_VALUE = 7;
    private static final String ZERO = "0";
    private static final String ONE = "1";
    Pattern mPattern;
    private EditText totalMoney;
    private EditText packetNum;
    private TextView totalMoneyView;


    public PacketNumInputFilter(EditText totalMoney, EditText packetNum, TextView totalMoneyView) {
        mPattern = Pattern.compile("([0-9])*");
        this.totalMoney = totalMoney;
        this.packetNum = packetNum;
        this.totalMoneyView = totalMoneyView;
    }


    /**
     * @param source 新输入的字符串
     * @param start  新输入的字符串起始下标，一般为0
     * @param end    新输入的字符串终点下标，一般为source长度-1
     * @param dest   输入之前文本框内容
     * @param dstart 原内容起始坐标，一般为0
     * @param dend   原内容终点坐标，一般为dest长度-1
     * @return 输入内容
     */
    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

        String sourceText = source.toString();
        String destText = dest.toString();

        Matcher matcher = mPattern.matcher(source);


        //验证删除等按键
        if (TextUtils.isEmpty(sourceText)) {
            return "";
        }

        if (!matcher.matches()) {
            return "";
        }

        if ((ZERO.equals(source.toString())) && TextUtils.isEmpty(destText)) {  //首位不能为0
            return "";
        }


        //验证输入数量的大小
        BigDecimal sumText = new BigDecimal(destText + sourceText);


        if (sumText.compareTo(new BigDecimal(MAX_VALUE)) > 0 || sumText.compareTo(new BigDecimal(MIN_VALUE)) < 0) {
            return dest.subSequence(dstart, dend);
        }

        this.packetNum.setTextColor(Color.parseColor("#000000"));
        return dest.subSequence(dstart, dend) + sourceText;
    }
}
