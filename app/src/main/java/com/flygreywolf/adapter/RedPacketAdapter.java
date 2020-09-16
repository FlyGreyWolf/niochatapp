package com.flygreywolf.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.myapplication.R;
import com.flygreywolf.bean.User;

import java.math.BigDecimal;
import java.util.List;

public class RedPacketAdapter extends BaseAdapter {

    private List<User> usersGet;
    private List<BigDecimal> moneyGet;
    private Context mContext;
    private int tagId = 0;

    public RedPacketAdapter(List<User> usersGet, List<BigDecimal> moneyGet, Context mContext) {
        this.usersGet = usersGet;
        this.moneyGet = moneyGet;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return usersGet.size();
    }

    @Override
    public Object getItem(int i) {
        return usersGet.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.activity_packet_info_item, parent, false);
            convertView.setTag(tagId++);
        }

        TextView userHost = convertView.findViewById(R.id.user_host);
        userHost.setText(usersGet.get(position).getHost()); // 用户host


        TextView userGet = convertView.findViewById(R.id.user_get);
        userGet.setText(moneyGet.get(position) + "元"); // 用户抢到的钱

        return convertView;
    }
}
