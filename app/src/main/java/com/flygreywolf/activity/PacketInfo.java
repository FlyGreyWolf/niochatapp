package com.flygreywolf.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.flygreywolf.adapter.RedPacketAdapter;
import com.flygreywolf.bean.RedPacket;
import com.flygreywolf.util.Application;

public class PacketInfo extends AppCompatActivity {


    private TextView redpacketInfo;
    private ListView redpacketGetListView;
    private RedPacketAdapter redPacketAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_packet_info);

        redpacketInfo = findViewById(R.id.redpacket_info);
        redpacketGetListView = findViewById(R.id.redpacket_get_list_view);

        Log.e("redpacketInfo", redpacketInfo + "");


        RedPacket redPacket = (RedPacket) Application.appMap.get(Application.RED_PACKET_INFO);


        redpacketInfo.setText(redPacket.getTotalNum() + "个红包共" + redPacket.getTotalMoney() + "元");


        redPacketAdapter = new RedPacketAdapter(redPacket.getUsersGet(), redPacket.getMoneyGet(), PacketInfo.this);


        redpacketGetListView.setAdapter(redPacketAdapter);


    }
}