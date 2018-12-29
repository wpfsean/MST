package com.tehike.mst.client.project.ui.portactivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.tehike.mst.client.project.R;
import com.tehike.mst.client.project.adapters.WifiListAdapter;
import com.tehike.mst.client.project.base.BaseActivity;
import com.tehike.mst.client.project.ui.widget.SpaceItemDecoration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import butterknife.BindView;


/**
 * 竖屏中的wifi设置功能
 * <p>
 * wifi列表
 */
public class PortWifiActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener{

    /**
     * wifi管理器
     */
    private WifiManager mWiFiManager = null;

    /**
     * wifi列表
     */
    @BindView(R.id.wifi_recyclerview)
    RecyclerView mRecyclerView;


    @BindView(R.id.wifi_swiperefreshlayout)
    SwipeRefreshLayout sw;

    /**
     * 盛放扫描到的wifi对象
     */
    private List<ScanResult> list = new ArrayList<ScanResult>();

    /**
     * wifi适配器
     */
    private WifiListAdapter myAdapter;


    @Override
    protected int intiLayout() {
        return R.layout.activity_port_wifi;
    }

    @SuppressLint("WifiManagerLeak")
    @Override
    protected void afterCreate(Bundle savedInstanceState) {

        //设置下拉 颜色
        sw.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        //设置下拉 刷新
        sw.setOnRefreshListener(this);


        // 获取WiFi管理者对象
        if (mWiFiManager == null)
            mWiFiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);


        mRecyclerView.addItemDecoration(new SpaceItemDecoration(0, 0));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        myAdapter = new WifiListAdapter(this, mWiFiManager, list);
        mRecyclerView.setAdapter(myAdapter);


        // 判断是否已经打开WiFi
        if (!mWiFiManager.isWifiEnabled()) {
            // 打开Wifi连接
            mWiFiManager.setWifiEnabled(true);
        }

        //判断wifi是否启用
        if (mWiFiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {
            return;
        }

        //开始扫描
        mWiFiManager.startScan();

        // 返回结果是当前设备所在区域能搜索出来的WiFi列表
        List<ScanResult> results = mWiFiManager.getScanResults();

//        for (int i = 0; i < results.size() - 1; i++) {
//            for (int j = results.size() - 1; j > i; j--) {
//                if (results.get(j).SSID.equals(results.get(i).SSID)) {
//                    if (results.get(j).level < results.get(i).level)
//                        results.remove(j);
//                }
//            }
//        }

        results = filterScanResult(results);

        for (ScanResult s : results) {
                myAdapter.addDevice(s);

//            if (s.SSID.contains("ZKTH") || s.SSID.contains("CPB")){
//                myAdapter.addDevice(s);
//                Logutils.i("s:" + s.toString());
//            }
        }
    }


    /**
     * 根据信息强度去除重复的热点
     *
     * @param list
     * @return
     */
    public static List<ScanResult> filterScanResult(final List<ScanResult> list) {
        LinkedHashMap<String, ScanResult> linkedMap = new LinkedHashMap<>(list.size());
        for (ScanResult rst : list) {
            if (linkedMap.containsKey(rst.SSID)) {
                if (rst.level > linkedMap.get(rst.SSID).level) {
                    linkedMap.put(rst.SSID, rst);
                }
                continue;
            }
            linkedMap.put(rst.SSID, rst);
        }
        list.clear();
        list.addAll(linkedMap.values());
        return list;
    }

    @Override
    public void onNetChange(int state, String name) {

    }

    @Override
    public void onRefresh() {
        super.onRefresh();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (sw !=
                        null)
                    sw.setRefreshing(false);
            }
        }, 2 * 1000);
    }
}
