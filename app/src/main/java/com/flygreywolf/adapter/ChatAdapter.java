package com.flygreywolf.adapter;

import android.content.Context;
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

    // 定义两个类别标志
    private static final int MY_MSG = 0;
    private static final int OTHER_MSG = 1;
    private static int typeCnt = 2;

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
        return i;
    }


    @Override
    public int getItemViewType(int position) {
        if (chatList.get(position).isMe() == true) {
            return MY_MSG;
        } else {
            return OTHER_MSG;
        }
    }

    @Override
    public int getViewTypeCount() {
        return typeCnt;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        int type = getItemViewType(position);
        MyMsgViewHolder myMsgViewHolder = null;
        OtherMsgViewHolder otherMsgViewHolder = null;


        if (convertView == null) {
            switch (type) {
                case MY_MSG:
                    myMsgViewHolder = new MyMsgViewHolder();
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.my_msg_record_item_layout, parent, false);
                    myMsgViewHolder.content = convertView.findViewById(R.id.my_msg_record);
                    convertView.setTag(myMsgViewHolder);
                    break;
                case OTHER_MSG:
                    otherMsgViewHolder = new OtherMsgViewHolder();
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.other_msg_record_item_layout, parent, false);
                    otherMsgViewHolder.content = convertView.findViewById(R.id.other_msg_record);
                    convertView.setTag(otherMsgViewHolder);
                    break;
            }
        } else {
            switch (type) {
                case MY_MSG:
                    myMsgViewHolder = (MyMsgViewHolder) convertView.getTag();
                    break;

                case OTHER_MSG:
                    otherMsgViewHolder = (OtherMsgViewHolder) convertView.getTag();
                    break;
            }
        }


        switch (type) {
            case MY_MSG:
                myMsgViewHolder.content.setText(chatList.get(position).getMsg());
                break;

            case OTHER_MSG:
                otherMsgViewHolder.content.setText(chatList.get(position).getMsg());
                break;
        }

        return convertView;
    }
}

class MyMsgViewHolder {
    TextView content;
}

class OtherMsgViewHolder {
    TextView content;
}
