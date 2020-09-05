package com.flygreywolf.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSON;
import com.example.myapplication.R;
import com.flygreywolf.adapter.ChatAdapter;
import com.flygreywolf.bean.Chat;
import com.flygreywolf.bean.Room;
import com.flygreywolf.constant.Constant;
import com.flygreywolf.niosocket.NioSocketClient;
import com.flygreywolf.util.Application;
import com.flygreywolf.util.Convert;

import java.util.ArrayList;
import java.util.List;

public class RoomActivity extends AppCompatActivity {


    public volatile boolean isActivityDestroy = false;
    private Room room = null;
    private NioSocketClient client = null;
    private Button sendMsgBnt;
    private EditText msgTextInput;

    private ChatAdapter chatAdapter = null;
    private List<Chat> chatList = new ArrayList<>();
    private ListView chatListView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        client = (NioSocketClient) Application.appMap.get("nioSocketClient");
        if (client != null) {
            client.setActivity(RoomActivity.this);
        }

        room = (Room) Application.appMap.get("room");

        if (room != null) {
            setTitle(room.getRoomName());

        }

        sendMsgBnt = (Button) findViewById(R.id.send_msg);
        msgTextInput = (EditText) findViewById(R.id.msg_text_input);

        chatAdapter = new ChatAdapter(chatList, RoomActivity.this);

        chatListView = (ListView) findViewById(R.id.chat_list);

        chatListView.setAdapter(chatAdapter);


        sendMsgBnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (client != null && client.getIsConnected() == true) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Chat chat = new Chat(room.getRoomId(), msgTextInput.getText().toString(), true);
                            boolean isSendSuccess = client.send(Convert.shortToBytes(Constant.SEND_MSG_CMD), JSON.toJSONString(chat));
                        }
                    }).start();
                }

            }
        });


        new Thread(new Runnable() { // 在房间内的心跳包，没1s发送一次，指令为 0x0002
            @Override
            public void run() {
                while (!isActivityDestroy) {
                    if (client == null) {
                        return;
                    }

                    boolean isSendSuccess = client.send(Convert.shortToBytes(Constant.ENTER_ROOM_CMD), room.getRoomId() + "");
                    if (isSendSuccess == false) {
                        return;
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

    }

    public void updateTitle(final String peopleCnt) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setTitle(room.getRoomName() + "(" + peopleCnt + ")");
            }
        });

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            ListView v = findViewById(R.id.chat_list);

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

        if (v != null && (v instanceof ListView)) {
            int[] leftTop = {0, 0};
            //获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            // 点击的是输入框区域，保留点击EditText的事件
            return (event.getX() > left) && (event.getX() < right)
                    && (event.getY() > top) && (event.getY() < bottom);
        }
        return false;
    }

    public void updateChatList(Chat chat) {
        chatList.add(chat);
        System.out.println("chatlist:" + chatList);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                chatAdapter.notifyDataSetChanged();
            }
        });

    }


    @Override
    protected void onDestroy() {
        System.out.println("销毁了。。。。");
        isActivityDestroy = true;
        super.onDestroy();
    }

}