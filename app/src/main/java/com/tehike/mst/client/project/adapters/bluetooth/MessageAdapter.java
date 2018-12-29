package com.tehike.mst.client.project.adapters.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tehike.mst.client.project.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Root on 2018/7/20.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MsgHolder>{
    private Context mContext;
    private List<String> msgList;


    public MessageAdapter(Context mContext) {
        this.mContext = mContext;
        msgList = new ArrayList<>();
    }

    @Override
    public MessageAdapter.MsgHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MessageAdapter.MsgHolder(LayoutInflater.from(mContext).inflate(R.layout.bluetooth_device_item,parent,false));
    }

    @Override
    public void onBindViewHolder(MessageAdapter.MsgHolder holder, final int position) {
        holder.nameTv.setText(msgList.get(position));
    }

    @Override
    public int getItemCount() {
        return msgList.size();
    }

    public void addMessage(String msg) {
        msgList.add(msg);
        notifyItemInserted(msgList.size()-1);
    }

    public void clearMsgList(){
        msgList.clear();
        notifyDataSetChanged();
    }

    public interface OnItemClickListener{
        void onClick(BluetoothDevice device);
    }

    class MsgHolder extends RecyclerView.ViewHolder{
        private TextView nameTv;
        public MsgHolder(View itemView) {
            super(itemView);
            nameTv = itemView.findViewById(R.id.name);
        }
    }
}