package com.tehike.mst.client.project.adapters.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

public class DevicesAdapter extends RecyclerView.Adapter<DevicesAdapter.RvHolder>{
    private Context mContext;
    private List<BluetoothDevice> mDevices;
    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public DevicesAdapter(Context mContext) {
        this.mContext = mContext;
        mDevices = new ArrayList<>();
    }

    @Override
    public RvHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RvHolder(LayoutInflater.from(mContext).inflate(R.layout.bluetooth_device_item,parent,false));
    }

    @Override
    public void onBindViewHolder(RvHolder holder, final int position) {
    //    holder.itemView.setBackgroundResource(R.drawable.ripple_bg);
        holder.nameTv.setText(mDevices.get(position).getName()+":"+mDevices.get(position).getAddress()+"\t"+bltStatus(mDevices.get(position).getBondState()));
        //点击事件 点击配对
        if (onItemClickListener!=null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i("TAG",mDevices.get(position).getAddress());
                    onItemClickListener.onClick(mDevices.get(position));
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mDevices.size();
    }

    public void addDevice(BluetoothDevice device) {
        mDevices.add(device);
        notifyItemInserted(mDevices.size()-1);
    }

    public void clearDevices(){
        mDevices.clear();
        notifyDataSetChanged();
    }

    public interface OnItemClickListener{
        void onClick(BluetoothDevice device);
    }

    class RvHolder extends RecyclerView.ViewHolder{
        private TextView nameTv;
        public RvHolder(View itemView) {
            super(itemView);
            nameTv = itemView.findViewById(R.id.name);
        }
    }
    public String bltStatus(int status) {
        String a = "未知状态";
        switch (status) {
            case BluetoothDevice.BOND_BONDING:
                a = "连接中";
                break;
            case BluetoothDevice.BOND_BONDED:
                a = "连接完成";
                break;
            case BluetoothDevice.BOND_NONE:
                a = "未连接";
                break;
        }
        return a;
    }
}