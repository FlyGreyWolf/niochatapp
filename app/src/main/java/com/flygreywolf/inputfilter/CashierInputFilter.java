package com.flygreywolf.inputfilter;

import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CashierInputFilter implements InputFilter {

    //输入的最大金额
    private static final int MAX_VALUE = 200;
    //小数点后的位数
    private static final int POINTER_LENGTH = 2;
    private static final String POINTER = ".";
    private static final String ZERO = "0";
    Pattern mPattern;
    private EditText totalMoney;
    private EditText packetNum;
    private TextView totalMoneyView;

    public CashierInputFilter(EditText totalMoney, EditText packetNum, TextView totalMoneyView) {
        mPattern = Pattern.compile("([0-9]|\\.)*");
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

        //验证删除等按键
        if (TextUtils.isEmpty(sourceText)) {

            return "";
        }

        Matcher matcher = mPattern.matcher(source);
        //已经输入小数点的情况下，只能输入数字
        if (destText.contains(POINTER)) {
            if (!matcher.matches()) {
                return "";
            } else {
                if (POINTER.equals(source.toString())) {  //只能输入一个小数点
                    return "";
                }
            }

            //验证小数点精度，保证小数点后只能输入两位
            int index = destText.indexOf(POINTER);
            int length = dend - index;

            if (length > POINTER_LENGTH) {
                return dest.subSequence(dstart, dend);
            }
        } else {
            /**
             * 没有输入小数点的情况下，只能输入小数点和数字
             * 1. 首位不能输入小数点
             * 2. 如果首位输入0，则接下来只能输入小数点了
             * 3. 如果输入已经是最大值了，就不能输入小数点了
             */
            if (!matcher.matches()) {
                return "";
            } else {
                if ((POINTER.equals(source.toString())) && TextUtils.isEmpty(destText)) {  //首位不能输入小数点
                    return "";
                } else if (!POINTER.equals(source.toString()) && ZERO.equals(destText)) { //如果首位输入0，接下来只能输入小数点
                    return "";
                } else if (POINTER.equals(source.toString()) && new BigDecimal(destText).equals(new BigDecimal(MAX_VALUE))) { // 如果已经是最大值了，就不能再输入小数点了
                    return "";
                }
            }
        }

        //验证输入金额的大小
        BigDecimal sumText = new BigDecimal(destText + sourceText);
        if (sumText.compareTo(new BigDecimal(MAX_VALUE)) > 0) {

            return dest.subSequence(dstart, dend);
        }

        return dest.subSequence(dstart, dend) + sourceText;
    }

}
