package com.flygreywolf.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.myapplication.R;
import com.flygreywolf.bean.Chat;
import com.flygreywolf.bean.Msg;
import com.flygreywolf.bean.RedPacket;
import com.flygreywolf.constant.Constant;
import com.flygreywolf.niosocket.NioSocketClient;
import com.flygreywolf.util.Convert;

import java.util.List;

public class ChatAdapter extends BaseAdapter {


    private static int typeCnt = 4;
    private Context mContext;
    private int tagId = 0;
    private List<Msg> chatList;
    private NioSocketClient client;


    public ChatAdapter(List<Msg> chatList, Context mContext, NioSocketClient client) {
        this.chatList = chatList;
        this.mContext = mContext;
        this.client = client;
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
    public View getView(final int position, View convertView, ViewGroup parent) {

        int type = getItemViewType(position);
        MyTextTypeViewHolder myTextTypeViewHolder = null;
        OtherTextTypeViewHolder otherTextTypeViewHolder = null;
        MyPacketTypeViewHolder myPacketTypeViewHolder = null;
        OtherPacketTypeViewHolder otherPacketTypeViewHolder = null;

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
                case Constant.MY_PACKET_TYPE:
                    myPacketTypeViewHolder = new MyPacketTypeViewHolder();
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.my_redpacket_item_layout, parent, false);

                    myPacketTypeViewHolder.redpacket_money = convertView.findViewById(R.id.my_redpacket_money);
                    myPacketTypeViewHolder.redpacket_num = convertView.findViewById(R.id.my_redpacket_num);
                    myPacketTypeViewHolder.myRedpacketTop = convertView.findViewById(R.id.my_redpacket_top);
                    myPacketTypeViewHolder.myRedpacketBottom = convertView.findViewById(R.id.my_redpacket_bottom);

                    convertView.setTag(myPacketTypeViewHolder);
                    break;
                case Constant.OTHER_PACKET_TYPE:
                    otherPacketTypeViewHolder = new OtherPacketTypeViewHolder();
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.other_redpacket_item_layout, parent, false);
                    otherPacketTypeViewHolder.redpacket_money = convertView.findViewById(R.id.other_redpacket_money);
                    otherPacketTypeViewHolder.redpacket_num = convertView.findViewById(R.id.other_redpacket_num);
                    convertView.setTag(otherPacketTypeViewHolder);
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

                case Constant.MY_PACKET_TYPE:
                    myPacketTypeViewHolder = (MyPacketTypeViewHolder) convertView.getTag();
                    break;

                case Constant.OTHER_PACKET_TYPE:
                    otherPacketTypeViewHolder = (OtherPacketTypeViewHolder) convertView.getTag();
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
            case Constant.MY_PACKET_TYPE:
                RedPacket myRedPacket = (RedPacket) chatList.get(position);
                myPacketTypeViewHolder.redpacket_money.setText(myRedPacket.getTotalMoney() + "元");
                myPacketTypeViewHolder.redpacket_num.setText(myRedPacket.getTotalNum() + "包");
                LinearLayout[] myRedPacketLayout = new LinearLayout[2];
                myRedPacketLayout[0] = myPacketTypeViewHolder.myRedpacketTop;
                myRedPacketLayout[1] = myPacketTypeViewHolder.myRedpacketBottom;

                for (int i = 0; i < myRedPacketLayout.length; i++) {
                    myRedPacketLayout[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.e("nuM:", (chatList.get(position)).getMsgId() + "");
                                    client.send(Convert.shortToBytes(Constant.GET_RED_PACKET_CMD), (chatList.get(position)).getMsgId() + "");

                                }
                            }).start();
                        }
                    });
                }
                break;

            case Constant.OTHER_PACKET_TYPE:
                RedPacket otherRedPacket = (RedPacket) chatList.get(position);
                otherPacketTypeViewHolder.redpacket_money.setText(otherRedPacket.getTotalMoney() + "元");
                otherPacketTypeViewHolder.redpacket_num.setText(otherRedPacket.getTotalNum() + "包");
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

class MyPacketTypeViewHolder {
    TextView redpacket_money;
    TextView redpacket_num;
    LinearLayout myRedpacketTop;
    LinearLayout myRedpacketBottom;
    RedPacket redPacket;
}

class OtherPacketTypeViewHolder {
    TextView redpacket_money;
    TextView redpacket_num;
}