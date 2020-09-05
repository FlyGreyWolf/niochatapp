package com.flygreywolf.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.myapplication.R;
import com.flygreywolf.bean.Chat;

import java.util.List;

public class ChatAdapter extends BaseAdapter {

    private List<Chat> chatList;
    private Context mContext;
    private int tagId = 0;

    public ChatAdapter(List<Chat> chatList, Context mContext) {
        this.chatList = chatList;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return chatList.size();
    }

    @Override
    public Object getItem(int i) {
        return chatList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.e("xxx", "aaaaa");
        if (convertView == null) {

            if (chatList.get(position).isMe() == true) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.my_msg_record_item_layout, parent, false);
                TextView msg = convertView.findViewById(R.id.my_msg_record);
                msg.setText(chatList.get(position).getMsg());
            } else {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.other_msg_record_item_layout, parent, false);
                TextView msg = convertView.findViewById(R.id.other_msg_record);
                msg.setText(chatList.get(position).getMsg());
            }


            Log.d("位置" + position, "创建新convertView,设置tagId:" + tagId);
            convertView.setTag(tagId++);
        } else {
            Log.d("位置" + position, convertView.getTag() + " 复用convertView");
        }


        return convertView;
    }
}
