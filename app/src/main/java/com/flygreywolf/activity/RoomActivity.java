package com.flygreywolf.activity;

import android.content.Context;
import android.content.Intent;
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
import com.flygreywolf.bean.Image;
import com.flygreywolf.bean.Msg;
import com.flygreywolf.bean.RedPacket;
import com.flygreywolf.bean.Room;
import com.flygreywolf.constant.Constant;
import com.flygreywolf.keyboard.GlobalLayoutListener;
import com.flygreywolf.keyboard.OnKeyboardChangedListener;
import com.flygreywolf.niosocket.NioSocketClient;
import com.flygreywolf.util.Application;
import com.flygreywolf.util.Convert;
import com.flygreywolf.util.ImgUtil;
import com.flygreywolf.util.ListViewUtil;
import com.huantansheng.easyphotos.EasyPhotos;
import com.huantansheng.easyphotos.callback.SelectCallback;
import com.huantansheng.easyphotos.models.album.entity.Photo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RoomActivity extends AppCompatActivity {

    private static final int REQUEST_SELECT_IMAGES_CODE = 0x01;
    public volatile boolean isActivityDestroy = false;
    private Room room = null;

    private NioSocketClient client = null;
    private ArrayList<Photo> selectedPhotoList = new ArrayList<>();
    private NioSocketClient imgClient = null;

    private Button sendMsgBnt; // 发送信息按钮
    private EditText msgTextInput; // 信息输入框
    private Button sendPacketBnt; // 发包按钮
    private NioSocketClient getBigImgClient = null;

    private ChatAdapter chatAdapter = null;
    private List<Msg> chatList = new ArrayList<>();
    private ListView chatListView = null;

    private LinearLayout linearLayout;
    private Button selectImg;
    private ArrayList<String> mImagePaths;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);


        linearLayout = findViewById(R.id.activity_room_root);


        client = (NioSocketClient) Application.appMap.get("nioSocketClient");
        if (client != null) {
            client.setActivity(RoomActivity.this);
        }
        imgClient = (NioSocketClient) Application.appMap.get(Application.imgNioSocketClient);
        if (imgClient != null) {
            imgClient.setActivity(RoomActivity.this);
        }
        getBigImgClient = (NioSocketClient) Application.appMap.get(Application.getBigImgClientNioSocketClient);
        if (getBigImgClient != null) {
            getBigImgClient.setActivity(RoomActivity.this);
        }


        room = (Room) Application.appMap.get("room");

        if (room != null) {
            setTitle(room.getRoomName());

        }

        sendMsgBnt = findViewById(R.id.send_msg);
        msgTextInput = findViewById(R.id.msg_text_input);
        sendPacketBnt = findViewById(R.id.send_packet);
        selectImg = findViewById(R.id.select_img);

        chatAdapter = new ChatAdapter(chatList, RoomActivity.this, client, imgClient, getBigImgClient);

        chatListView = findViewById(R.id.chat_list);

        chatListView.setAdapter(chatAdapter);


        sendMsgBnt.setOnClickListener(new View.OnClickListener() { // 本人发送信息
            @Override
            public void onClick(View view) { // 发送文本消息监听

                if (client != null && client.getIsConnected() == true) {

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Msg msg = new Chat(room.getRoomId(), -1, Constant.MY_TEXT_TYPE, msgTextInput.getText().toString());
                            boolean isSendSuccess = client.send(Convert.shortToBytes(Constant.SEND_CHAT_CMD), JSON.toJSONString(msg));
                        }
                    }).start();
                }
            }
        });


        sendPacketBnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 发送红包消息监听
                Intent intent = new Intent(RoomActivity.this, SendPacketActivity.class);
                startActivity(intent);
            }
        });


        selectImg.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                EasyPhotos.createAlbum(RoomActivity.this, true, GlideEngine.getInstance())
                        .setFileProviderAuthority("com.huantansheng.easyphotos.demo.fileprovider")
                        .setCount(22)
                        .start(new SelectCallback() {
                            @Override
                            public void onResult(ArrayList<Photo> photos, boolean isOriginal) {
                                selectedPhotoList.clear();
                                selectedPhotoList.addAll(photos);

                                for (final Photo p : photos) {

                                    final File file = new File(p.path);
                                    if (file.exists()) {


                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {

                                                Image img = new Image(room.getRoomId(), -1, Constant.MY_IMG_TYPE, ImgUtil.getBytes(p.path, (int) file.length()));
                                                Log.e("sbaa", JSON.toJSONString(img));
                                                ((NioSocketClient) Application.appMap.get(Application.imgNioSocketClient)).send(Convert.shortToBytes(Constant.SEND_IMG_CMD), JSON.toJSONString(img));
                                            }
                                        }).start();

                                    }

                                }
                            }
                        });
            }
        });


        new Thread(new Runnable() { // 在房间内的心跳包，没1s发送一次，指令为 0x0002
            @Override
            public void run() {
                while (!isActivityDestroy) {
                    if (client == null) {
                        return;
                    }

                    boolean isSendSuccess = client.send(Convert.shortToBytes(Constant.ENTER_ROOM_CMD), room.getRoomId() + ""); // 房间内的心跳包
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
                            chatListView.setSelection(chatAdapter.getCount() - 1);


                        } else {
                            Toast.makeText(RoomActivity.this, "软键盘收起来了", Toast.LENGTH_SHORT).show();

                        }
                    }
                }));


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
     * @param msgObj
     */
    public void updateChatList(Msg msgObj) {
        if (!msgObj.getRoomId().equals(room.getRoomId())) { // 信息的roomId和用户当前所在的roomId不符合
            return;
        }

        chatList.add(msgObj);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                boolean flag = ListViewUtil.isListViewReachBottomEdge(chatListView);
                chatAdapter.notifyDataSetChanged();

                if (flag == true) { // chatListView已经到达底部
                    chatListView.setSelection(chatListView.getBottom()); // chatListView 滑到最底
                }
            }
        });

    }

    public void turnToPacketInfo(RedPacket redPacket) {
        Application.appMap.put(Application.RED_PACKET_INFO, redPacket);
        Intent intent = new Intent(RoomActivity.this, PacketInfo.class);

        startActivity(intent);
    }



    @Override
    protected void onDestroy() {
        System.out.println("销毁了。。。。");
        isActivityDestroy = true;
        super.onDestroy();
    }



}