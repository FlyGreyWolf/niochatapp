package com.flygreywolf.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.flygreywolf.constant.Constant;
import com.flygreywolf.niosocket.NioSocketClient;

public class MainActivity extends AppCompatActivity {

    private EditText textInput = null;
    private Button connectBnt = null;
    private Button sendBnt = null;
    private NioSocketClient client = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textInput = findViewById(R.id.textInput);
        connectBnt = findViewById(R.id.connect);
        sendBnt = findViewById(R.id.send);


        sendBnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (client != null && client.getIsConnected() == true) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            client.send();
                        }
                    }).start();
                }
            }
        });


        //监听connectBnt事件
        connectBnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (connectBnt.getText().equals(Constant.Make_Connect)) { // 如果connectBnt的文字是 “建立连接”
                    connectBnt.setEnabled(false);
                    client = new NioSocketClient(Constant.Host, Constant.port); // 客户端连接
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            boolean isSuccess = client.connect();
                            if (isSuccess == true) { // 连接成功
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this, Constant.Connect_Success, Toast.LENGTH_SHORT).show();
                                        connectBnt.setText(Constant.Dis_Connect);
                                        connectBnt.setEnabled(true);
                                    }
                                });

                                new Thread(client).start(); // 开启监听读数据的线程

                            } else { // 连接失败
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this, Constant.Connect_Fail, Toast.LENGTH_SHORT).show();
                                        connectBnt.setEnabled(true);
                                    }
                                });
                            }

                        }
                    }).start();
                } else if (connectBnt.getText().equals(Constant.Dis_Connect)) { // 如果connectBnt的文字是 “断开连接”
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            client.disConnect();
                            connectBnt.setText(Constant.Make_Connect);
                        }
                    });
                }
            }
        });

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideInput(v, ev)) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }


    public boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = {0, 0};
            //获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            // 点击的是输入框区域，保留点击EditText的事件
            return !(event.getX() > left) || !(event.getX() < right)
                    || !(event.getY() > top) || !(event.getY() < bottom);
        }
        return false;
    }


}