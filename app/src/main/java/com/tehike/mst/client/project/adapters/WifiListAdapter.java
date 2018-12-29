package com.tehike.mst.client.project.adapters;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tehike.mst.client.project.R;
import com.tehike.mst.client.project.utils.Logutil;
import com.tehike.mst.client.project.utils.WiFiUtil;

import java.util.List;

/**
 * Created by Jerry.Zou
 */


public class WifiListAdapter extends RecyclerView.Adapter<WifiListAdapter.ViewHolder> {

    private Context context;
    List<ScanResult> dataResources;
    private int currentItem = -1; //用于记录点击的 Item 的 position，是控制 item 展开的核心
    private WifiManager mWifiManager;


    public WifiListAdapter(Context context, WifiManager mWifiManager, List<ScanResult> dataResources) {
        super();
        this.context = context;
        this.mWifiManager = mWifiManager;
        this.dataResources = dataResources;
    }

    public void addDevice(ScanResult device) {
        dataResources.add(device);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new WifiListAdapter.ViewHolder(LayoutInflater.from(context).inflate(R.layout.wifi_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.showName.setText(dataResources.get(position).SSID);
        holder.showArea.setTag(position);
        //根据 currentItem 记录的点击位置来设置"对应Item"的可见性（在list依次加载列表数据时，每加载一个时都看一下是不是需改变可见性的那一条）
        if (currentItem == position) {
            holder.hideArea.setVisibility(View.VISIBLE);
        } else {
            holder.hideArea.setVisibility(View.GONE);
        }
        holder.showArea.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                //用 currentItem 记录点击位置
                int tag = (Integer) view.getTag();
                if (tag == currentItem) { //再次点击
                    currentItem = -1; //给 currentItem 一个无效值
                } else {
                    currentItem = tag;
                }
                notifyDataSetChanged(); //必须有的一步
            }
        });
        holder.wifiDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Logutils.i("详情ing");
//                WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
//                if (dataResources.get(position).BSSID.equals(wifiInfo.getBSSID())){
//                    Logutils.i("ssid: " + wifiInfo.getSSID());
//                    Logutils.i( "bssid: " + wifiInfo.getBSSID());
//                    Logutils.i( "mac address: " + wifiInfo.getMacAddress());
//                    Logutils.i( "speed: " + wifiInfo.getLinkSpeed());
//                    Logutils.i("ip address: " + wifiInfo.getIpAddress());
//                    Logutils.i("netwok id: " + wifiInfo.getNetworkId());
//                }
//                else
//                    Logutils.i("此对象未连接");
            }
        });
        holder.wifiConnet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logutil.i("连接ing");

                ScanResult currentItemWif = dataResources.get(position);
                if (currentItemWif.capabilities.contains("WPA2") || currentItemWif.capabilities.contains("WPA-PSK")) {
                    WiFiUtil.getInstance(context).addWiFiNetwork(currentItemWif.SSID, "Zkth123456789", WiFiUtil.Data.WIFI_CIPHER_WPA2);
                } else if (currentItemWif.capabilities.contains("WPA")) {
                    WiFiUtil.getInstance(context).addWiFiNetwork(currentItemWif.SSID, "Zkth123456789", WiFiUtil.Data.WIFI_CIPHER_WPA);
                } else if (currentItemWif.capabilities.contains("WEP")) {
                    /* WIFICIPHER_WEP 加密 */
                    WiFiUtil.getInstance(context).addWiFiNetwork(currentItemWif.SSID, "Zkth123456789", WiFiUtil.Data.WIFI_CIPHER_WEP);
                } else {
                    /* WIFICIPHER_OPEN NOPASSWORD 开放无加密 */
                    WiFiUtil.getInstance(context).addWiFiNetwork(currentItemWif.SSID, "", WiFiUtil.Data.WIFI_CIPHER_NOPASS);
                }

            }
        });
        holder.wifiDisConnet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
                if (dataResources.get(position).BSSID.equals(wifiInfo.getBSSID())){
                    int wifiId = wifiInfo.getNetworkId();
                    mWifiManager.disableNetwork(wifiId);
                    mWifiManager.disconnect();
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return dataResources.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView showName;
        private LinearLayout showArea;
        private RelativeLayout hideArea;
        private TextView wifiConnet;
        private TextView wifiDisConnet;
        private TextView wifiDetails;

        public ViewHolder(View itemView) {
            super(itemView);
            showName = itemView.findViewById(R.id.remote_device_name_layout);
            showArea = (LinearLayout) itemView.findViewById(R.id.layout_showArea);
            hideArea = (RelativeLayout) itemView.findViewById(R.id.layout_hideArea);
            wifiConnet = itemView.findViewById(R.id.wifi_connet_layout);
            wifiDisConnet = itemView.findViewById(R.id.wifi_disconnet_layout);
            wifiDetails = itemView.findViewById(R.id.wifi_details_layout);
        }
    }
}
