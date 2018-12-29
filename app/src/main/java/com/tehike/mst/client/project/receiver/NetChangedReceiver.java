package com.tehike.mst.client.project.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import com.tehike.mst.client.project.base.BaseActivity;
import com.tehike.mst.client.project.utils.NetworkUtils;

/**
 * Created by Jie on 2018/1/3.
 * 广播接收器 监听网络变化
 */

public class NetChangedReceiver extends BroadcastReceiver {

    private NetStatusChangeEvent event;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        // 这个监听包括WiFi和移动数据的打开和关闭
        assert action != null;
        if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            int state = NetworkUtils.getNetworkType(context);
            String name = NetworkUtils.getNetworkTypeName(context);
            if (event == null) {
                event = BaseActivity.event;
            }
            event.onNetChange(state, name);
        }
    }
    public interface NetStatusChangeEvent {
        void onNetChange(int state, String name);
    }
}
