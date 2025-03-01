package com.flygreywolf.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.flygreywolf.adapter.RoomAdapter;
import com.flygreywolf.bean.Room;
import com.flygreywolf.constant.Constant;
import com.flygreywolf.niosocket.NioSocketClient;
import com.flygreywolf.util.Application;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private NioSocketClient client = null;
    private NioSocketClient imgClient = null;
    private NioSocketClient getBigImgClient = null;

    private int tagId = 0;
    private EditText textInput = null;
    private Button connectBnt = null;
    private Button sendBnt = null;
    private RoomAdapter roomAdapter = null;
    private List<Room> roomList = null;
    private ListView roomListView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);
        textInput = findViewById(R.id.textInput);
        connectBnt = findViewById(R.id.connect);
        sendBnt = findViewById(R.id.send);

        /**
         * 发送数据按钮监听器
         */
        sendBnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (client != null && client.getIsConnected() == true) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            //boolean isSendSuccess = client.send(textInput.getText().toString());
                        }
                    }).start();
                }
            }
        });


        //监听connectBnt事件
        connectBnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectBnt.setEnabled(false);
                if (connectBnt.getText().equals(Constant.Make_Connect)) { // 如果connectBnt的文字是 “建立连接”

                    client = new NioSocketClient(Constant.Host, Constant.Connect_port, MainActivity.this); // 客户端连接
                    imgClient = new NioSocketClient(Constant.Host, Constant.Img_port, MainActivity.this); // 处理图片客户端连接
                    getBigImgClient = new NioSocketClient(Constant.Host, Constant.Get_big_img_port, MainActivity.this); // 处理查看大图的请求
                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            if (client.connect() == true && imgClient.connect() == true && getBigImgClient.connect() == true) { // 连接成功
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this, Constant.Connect_Success, Toast.LENGTH_SHORT).show();
                                        connectBnt.setText(Constant.Dis_Connect);
                                        connectBnt.setEnabled(true);
                                    }
                                });

                                new Thread(client).start(); // 开启监听读数据的线程
                                new Thread(imgClient).start(); // 开启监听读数据的线程
                                new Thread(getBigImgClient).start(); // 开启监听读数据的线程

                                new Thread(new Runnable() { // 心跳包发送线程
                                    @Override
                                    public void run() {
                                        while (true) {
                                            boolean isSendSuccess = client.send(new byte[0], ""); // 连接的心跳包，空包
                                            Log.e("心跳包发送", String.valueOf(isSendSuccess));
                                            if (isSendSuccess == false) {
                                                imgClient.disConnect();
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Toast.makeText(MainActivity.this, Constant.Dis_Connect, Toast.LENGTH_SHORT).show();
                                                        connectBnt.setText(Constant.Make_Connect);
                                                        connectBnt.setEnabled(true);
                                                    }
                                                });
                                                return;
                                            }
                                            try {
                                                Thread.sleep(1000); // 每1s发一次
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }).start();

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

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            client.disConnect();
                            imgClient.disConnect();
                            getBigImgClient.disConnect();
                        }
                    }).start();
                }
            }
        });

    }

    public void updateRoomListView(final ArrayList<Room> roomList) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                roomAdapter = new RoomAdapter(roomList, MainActivity.this);

                roomListView = findViewById(R.id.roomListView);

                roomListView.setAdapter(roomAdapter);
                roomListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                        Toast.makeText(MainActivity.this, "点击了第" + position + "条数据", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this, RoomActivity.class);


                        Application.appMap.put("nioSocketClient", client);
                        Application.appMap.put(Application.imgNioSocketClient, imgClient);
                        Application.appMap.put(Application.getBigImgClientNioSocketClient, getBigImgClient);


                        Application.appMap.put("room", roomAdapter.getItem(position));
                        startActivity(intent);
                    }
                });

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


    /**
     * 按系统返回键主界面不退出
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


}