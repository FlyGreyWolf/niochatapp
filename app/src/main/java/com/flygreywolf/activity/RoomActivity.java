package com.flygreywolf.activity;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSON;
import com.example.myapplication.R;
import com.flygreywolf.adapter.ChatAdapter;
import com.flygreywolf.bean.Chat;
import com.flygreywolf.bean.Room;
import com.flygreywolf.constant.Constant;
import com.flygreywolf.keyboard.GlobalLayoutListener;
import com.flygreywolf.keyboard.OnKeyboardChangedListener;
import com.flygreywolf.niosocket.NioSocketClient;
import com.flygreywolf.util.Application;
import com.flygreywolf.util.Convert;
import com.flygreywolf.util.ListViewUtil;

import java.util.ArrayList;
import java.util.List;

public class RoomActivity extends AppCompatActivity {


    public volatile boolean isActivityDestroy = false;
    private Room room = null;
    private NioSocketClient client = null;
    private Button sendMsgBnt; // 发送信息按钮
    private EditText msgTextInput; // 信息输入框

    private ChatAdapter chatAdapter = null;
    private List<Chat> chatList = new ArrayList<>();
    private ListView chatListView = null;

    private LinearLayout linearLayout;

    private Integer msgId = 0; // 每一条msg的唯一标识


    private int screenHeight; // 屏幕的高度

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        linearLayout = findViewById(R.id.activity_room_root);


        client = (NioSocketClient) Application.appMap.get("nioSocketClient");
        if (client != null) {
            client.setActivity(RoomActivity.this);
        }

        room = (Room) Application.appMap.get("room");

        if (room != null) {
            setTitle(room.getRoomName());

        }

        sendMsgBnt = findViewById(R.id.send_msg);
        msgTextInput = findViewById(R.id.msg_text_input);

        chatAdapter = new ChatAdapter(chatList, RoomActivity.this);

        chatListView = findViewById(R.id.chat_list);

        chatListView.setAdapter(chatAdapter);


        sendMsgBnt.setOnClickListener(new View.OnClickListener() { // 本人发送信息
            @Override
            public void onClick(View view) {

                if (client != null && client.getIsConnected() == true) {
                    final int curMsgId = msgId; // 保留当前的msgId
                    msgId = msgId + 1; // 全局的msgId+1
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Chat chat = new Chat(room.getRoomId(), curMsgId, msgTextInput.getText().toString(), true);
                            boolean isSendSuccess = client.send(Convert.shortToBytes(Constant.SEND_MSG_CMD), JSON.toJSONString(chat));
                        }
                    }).start();
                }
            }
        });

        msgTextInput.addTextChangedListener(new TextWatcher() {
            int curLineCnt = 1;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                chatListView.setSelection(chatListView.getBottom()); // chatListView 滑到最底
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                chatListView.post(new Runnable() {
                    @Override
                    public void run() {
                        // Select the last row so it will scroll into view...
                        chatListView.setSelection(chatAdapter.getCount() - 1);
                    }
                });
            }

            @Override
            public void afterTextChanged(Editable editable) {
                chatListView.setSelection(chatListView.getBottom()); // chatListView 滑到最底
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


        linearLayout.getViewTreeObserver().addOnGlobalLayoutListener(
                new GlobalLayoutListener(linearLayout, new OnKeyboardChangedListener() {
                    /**
                     * 键盘事件
                     *
                     * @param isShow         键盘是否展示
                     * @param keyboardHeight 键盘高度(当isShow为false时,keyboardHeight=0)
                     * @param screenWidth    屏幕宽度
                     * @param screenHeight   屏幕可用高度(不包含底部虚拟键盘NavigationBar), 即屏幕高度-键盘高度(keyboardHeight)
                     */
                    @Override
                    public void onChange(boolean isShow, int keyboardHeight, int screenWidth, int screenHeight) {
                        // do sth.
                        if (isShow == true) {
                            Toast.makeText(RoomActivity.this, "软键盘弹起来了", Toast.LENGTH_SHORT).show();
                            chatListView.setSelection(chatListView.getBottom()); // chatListView 滑到最底

                        } else {
                            Toast.makeText(RoomActivity.this, "软键盘收起来了", Toast.LENGTH_SHORT).show();

                        }
                    }
                }));
    }

    public void updateTitle(final String peopleCnt) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setTitle(room.getRoomName() + "(" + peopleCnt + ")");
            }
        });

    }

    /**
     * 事件分发
     *
     * @param ev
     * @return
     */
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

    /**
     * 判断是否该隐藏软键盘
     *
     * @param v
     * @param event
     * @return
     */
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

    /**
     * 更新chatListView
     *
     * @param chat
     */
    public void updateChatList(Chat chat) {
        chatList.add(chat);
        System.out.println("chatlist:");
        for (int i = 0; i < chatList.size(); i++) {
            System.out.print(chatList.get(i));
        }
        //System.out.println("chatlist:" + chatList);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                boolean flag = ListViewUtil.isListViewReachBottomEdge(chatListView);
                chatAdapter.notifyDataSetChanged();
                Log.e("gengxin", flag + "");
                if (flag == true) { // chatListView已经到达底部
                    Log.e("daoda","sss");
                    chatListView.setSelection(chatListView.getBottom()); // chatListView 滑到最底
                }
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