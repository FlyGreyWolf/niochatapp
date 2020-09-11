package com.flygreywolf.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.myapplication.R;
import com.flygreywolf.bean.Chat;
import com.flygreywolf.bean.Msg;
import com.flygreywolf.constant.Constant;

import java.util.List;

public class ChatAdapter extends BaseAdapter {

    // 定义两个类别标志 8
    private static int typeCnt = 4;
    private Context mContext;
    private int tagId = 0;
    private List<Msg> chatList;

    public ChatAdapter(List<Msg> chatList, Context mContext) {
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

        return chatList.get(position).getMsgType();

    }

    @Override
    public int getViewTypeCount() {
        return typeCnt;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        int type = getItemViewType(position);
        MyTextTypeViewHolder myTextTypeViewHolder = null;
        OtherTextTypeViewHolder otherTextTypeViewHolder = null;


        if (convertView == null) {
            switch (type) {
                case Constant.MY_TEXT_TYPE:
                    myTextTypeViewHolder = new MyTextTypeViewHolder();
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.my_msg_record_item_layout, parent, false);
                    myTextTypeViewHolder.content = convertView.findViewById(R.id.my_msg_record);
                    convertView.setTag(myTextTypeViewHolder);
                    break;
                case Constant.OTHER_TEXT_TYPE:
                    otherTextTypeViewHolder = new OtherTextTypeViewHolder();
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.other_msg_record_item_layout, parent, false);
                    otherTextTypeViewHolder.content = convertView.findViewById(R.id.other_msg_record);
                    convertView.setTag(otherTextTypeViewHolder);
                    break;
            }
        } else {
            switch (type) {
                case Constant.MY_TEXT_TYPE:
                    myTextTypeViewHolder = (MyTextTypeViewHolder) convertView.getTag();
                    break;

                case Constant.OTHER_TEXT_TYPE:
                    otherTextTypeViewHolder = (OtherTextTypeViewHolder) convertView.getTag();
                    break;
            }
        }

        switch (type) {
            case Constant.MY_TEXT_TYPE:
                myTextTypeViewHolder.content.setText(((Chat) chatList.get(position)).getContent());
                break;

            case Constant.OTHER_TEXT_TYPE:
                otherTextTypeViewHolder.content.setText(((Chat) chatList.get(position)).getContent());
                break;
        }

        return convertView;
    }
}

class MyTextTypeViewHolder {
    TextView content;
}

class OtherTextTypeViewHolder {
    TextView content;
}
