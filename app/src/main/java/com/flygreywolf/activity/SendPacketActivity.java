package com.flygreywolf.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.flygreywolf.inputfilter.CashierInputFilter;
import com.flygreywolf.inputfilter.PacketNumInputFilter;

import java.math.BigDecimal;

public class SendPacketActivity extends AppCompatActivity {


    private static final BigDecimal EACH_MIN_MONEY = new BigDecimal("0.01");
    private EditText totalMoney;
    private EditText packetNum;
    private TextView totalMoneyView;
    private Button sendPacketBnt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_packet);


        totalMoney = findViewById(R.id.totalMoney);
        packetNum = findViewById(R.id.packetNum);
        totalMoneyView = findViewById(R.id.totalMoneyView);
        sendPacketBnt = findViewById(R.id.send_packet_bnt);
        sendPacketBnt.setEnabled(false); // 发包按钮设置为不可用

        totalMoney.setFilters(new InputFilter[]{new CashierInputFilter(totalMoney, packetNum, totalMoneyView)});

        packetNum.setFilters(new InputFilter[]{new PacketNumInputFilter(totalMoney, packetNum, totalMoneyView)});


        totalMoney.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (checkValid(s.toString(), packetNum.getText().toString()) == false) {
                    sendPacketBnt.setEnabled(false);
                } else {
                    sendPacketBnt.setEnabled(true);
                }


            }

            @Override
            public void afterTextChanged(Editable s) {
                String money = s.toString();
                if (money.equals("")) {
                    totalMoneyView.setText("￥0.00");
                    return;
                }

                if (!money.contains(".")) { // 没有小数点
                    totalMoneyView.setText("￥" + s.toString() + ".00");
                } else {
                    String[] splitMoney = money.split("\\.");

                    if (splitMoney.length == 1) { // 小数点后有0位
                        totalMoneyView.setText("￥" + s.toString() + "00");

                    } else if (splitMoney[1].length() == 1) { // 小数点后有1位
                        totalMoneyView.setText("￥" + s.toString() + "0");
                    } else { // 小数点后有2位
                        totalMoneyView.setText("￥" + s.toString());
                    }
                }
            }
        });

        packetNum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (checkValid(totalMoney.getText().toString(), s.toString()) == false) {
                    sendPacketBnt.setEnabled(false);
                } else {
                    sendPacketBnt.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {


            }
        });


        sendPacketBnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SendPacketActivity.this, RoomActivity.class);
                intent.putExtra("money", totalMoney.getText().toString());
                intent.putExtra("packetNum", packetNum.getText().toString());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        });
    }


    /**
     * 检查金额和包的个数的合法性
     *
     * @param money
     * @param num
     * @return
     */
    public boolean checkValid(String money, String num) {


        if (money.equals("") || num.equals("")) {
            return false;
        }


        if (money.charAt(money.length() - 1) == '.') {
            money = money.substring(0, money.length() - 1);
        }


        BigDecimal moneyBigDecimal = new BigDecimal(money);
        BigDecimal numDecimal = new BigDecimal(num);


        if (moneyBigDecimal.divide(numDecimal, 2, BigDecimal.ROUND_DOWN).compareTo(EACH_MIN_MONEY) < 0) {
            Toast.makeText(SendPacketActivity.this, "单个红包金额不可低于0.01元", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;


    }


}