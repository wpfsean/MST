package com.tehike.mst.client.project.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.tehike.mst.client.project.base.App;
import com.tehike.mst.client.project.global.AppConfig;
import com.tehike.mst.client.project.linphone.Linphone;
import com.tehike.mst.client.project.linphone.SipManager;
import com.tehike.mst.client.project.linphone.SipService;
import com.tehike.mst.client.project.sysinfo.SysinfoUtils;
import com.tehike.mst.client.project.utils.Logutil;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 描述：Sip保活（定时去检查sip状态,如果未在线就去尝试注册）
 * ===============================
 * @author wpfse wpfsean@126.com
 * @Create at:2018/12/29 16:07
 * @version V1.0
 */

public class TimingCheckSipStatus extends Service {

    //定时任务的线程池
    ScheduledExecutorService mScheduledExecutorService = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //延迟10秒后每30秒执行一次
        if (mScheduledExecutorService == null) {
            mScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            mScheduledExecutorService.scheduleWithFixedDelay(new CheckSipStatusThread(), 10, 15 * 1000, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mScheduledExecutorService.shutdown();
    }


    class CheckSipStatusThread extends Thread {
        @Override
        public void run() {

            //获取sip注册时相关的信息
            String sipNum = SysinfoUtils.getSysinfo().getSipUsername();
            String sipServer = SysinfoUtils.getSysinfo().getSipServer();
            String sipPwd = SysinfoUtils.getSysinfo().getSipPassword();

            //判断是否为空
            if (TextUtils.isEmpty(sipNum) || TextUtils.isEmpty(sipServer) || TextUtils.isEmpty(sipPwd)) {
                Logutil.e("AAAA未获取到sip信息");
                return;
            }
            Logutil.i("SIP_STATUS"+AppConfig.SIP_STATUS);
            //当前sip是否在线
            if (AppConfig.SIP_STATUS) {
                 Logutil.d("当前Sip在线");
                return;
            }
            //当前sip服务是否已启动
            if (!SipService.isReady() || !SipManager.isInstanceiated()) {
                Linphone.startService(App.getApplication());
            }
            Linphone.setAccount(sipNum, sipPwd, sipServer);
            Linphone.login();
            Logutil.i("AAAASip重新登录");
        }
    }
}
