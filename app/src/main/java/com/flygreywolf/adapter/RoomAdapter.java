package com.flygreywolf.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.myapplication.R;
import com.flygreywolf.bean.Room;

import java.util.List;

public class RoomAdapter extends BaseAdapter {

    private List<Room> roomList;
    private Context mContext;
    private int tagId = 0;

    public RoomAdapter(List<Room> roomList, Context mContext) {
        this.roomList = roomList;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        Log.e("----", roomList.size() + "");
        return roomList.size();
    }

    @Override
    public Object getItem(int i) {
        return roomList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.e("xxx", "aaaaa");
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.room_item_layout, parent, false);

            Log.d("位置" + position, "创建新convertView,设置tagId:" + tagId);
            convertView.setTag(tagId++);
        } else {
            Log.d("位置" + position, convertView.getTag() + " 复用convertView");
        }

        TextView roomNameText = (TextView) convertView.findViewById(R.id.room_name);
        roomNameText.setText(roomList.get(position).getRoomName()); // 房间名


        TextView roomMsgText = (TextView) convertView.findViewById(R.id.room_msg);
        roomMsgText.setText(roomList.get(position).getRoomMsg()); // 房间信息

        return convertView;
    }
}