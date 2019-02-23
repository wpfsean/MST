package com.tehike.mst.client.project.base;

import android.app.Application;
import android.os.Looper;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.tehike.mst.client.project.execption.Cockroach;
import com.tehike.mst.client.project.execption.CrashLog;
import com.tehike.mst.client.project.execption.ExceptionHandler;
import com.tehike.mst.client.project.linphone.SipManager;
import com.tehike.mst.client.project.linphone.SipService;
import com.tehike.mst.client.project.receiver.CpuAndRamUtils;
import com.tehike.mst.client.project.services.BatteryAndWifiService;
import com.tehike.mst.client.project.services.ReceiverEmergencyAlarmService;
import com.tehike.mst.client.project.services.RequestWebApiDataService;
import com.tehike.mst.client.project.services.ServiceUtils;
import com.tehike.mst.client.project.services.TimingAutoUpdateService;
import com.tehike.mst.client.project.services.TimingCheckSipStatus;
import com.tehike.mst.client.project.services.TimingSendHbService;
import com.tehike.mst.client.project.utils.ActivityUtils;
import com.tehike.mst.client.project.utils.Logutil;
import com.tehike.mst.client.project.utils.ToastUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 描述：Application全局配置
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2018/10/8 10:34
 */

public class App extends Application {
    /**
     * 上下文
     */
    private static App mContext;

    /**
     * 线程池
     */
    public static ExecutorService mThreadPoolService = null;

    /**
     * Volly请求队列
     */
    public static RequestQueue mRequestQueue = null;

    /**
     * 线程数（Returns the number of processors available to the Java virtual machine.）
     */
    int maxThreadCount = -1;

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = this;

        //获取最大的可用线程数
        maxThreadCount = Runtime.getRuntime().availableProcessors();

        Logutil.i("线程最大数量：" + maxThreadCount);

        if (mThreadPoolService == null) {
            //线程池内运行程序可执行的最大线程数
            mThreadPoolService = Executors.newFixedThreadPool(maxThreadCount);
        }
        if (mRequestQueue == null) {
            //实例请求队列
            mRequestQueue = Volley.newRequestQueue(this);
        }
        //启动电量和信号监听
        if (!ServiceUtils.isServiceRunning(BatteryAndWifiService.class))
            ServiceUtils.startService(BatteryAndWifiService.class);

        if (!ServiceUtils.isServiceRunning(TimingAutoUpdateService.class))
            ServiceUtils.startService(TimingAutoUpdateService.class);

        CpuAndRamUtils.getInstance().init(getApplicationContext(), 5 * 1000L);
        CpuAndRamUtils.getInstance().start();

        install();
    }

    /**
     * 用于捕捉全局的异常
     */
    private void install() {
        final Thread.UncaughtExceptionHandler sysExcepHandler = Thread.getDefaultUncaughtExceptionHandler();
        Cockroach.install(new ExceptionHandler() {
            @Override
            protected void onUncaughtExceptionHappened(Thread thread, final Throwable throwable) {
                Log.e("AndroidRuntime", "--->onUncaughtExceptionHappened:" + thread + "<---", throwable);
                //把崩溃异常写入文件
                CrashLog.saveCrashLog(mContext, throwable);
                Logutil.e(throwable.getMessage());
            }

            @Override
            protected void onBandageExceptionHappened(Throwable throwable) {
                throwable.printStackTrace();//打印警告级别log，该throwable可能是最开始的bug导致的，无需关心
                ToastUtils.showShort("Arrest!");
            }

            @Override
            protected void onEnterSafeMode() {

            }

            @Override
            protected void onMayBeBlackScreen(Throwable e) {
                Thread thread = Looper.getMainLooper().getThread();
                Log.e("AndroidRuntime", "--->onUncaughtExceptionHappened:" + thread + "<---", e);
                //黑屏时建议直接杀死app
                sysExcepHandler.uncaughtException(thread, new RuntimeException("black screen"));
            }

        });
    }

    /**
     * 全局上下文
     */
    public static App getApplication() {
        return mContext;
    }

    /**
     * 线程池
     */
    public static ExecutorService getExecutorService() {
        return mThreadPoolService;
    }

    /**
     * Volly队列
     */
    public static RequestQueue getQuest() {
        return mRequestQueue;
    }

    /**
     * 退出应用
     */
    public static void exit() {

        //移除栈中所有的activiry
        ActivityUtils.removeAllActivity();

        //关闭电量监听服务
        if (ServiceUtils.isServiceRunning(BatteryAndWifiService.class))
            ServiceUtils.stopService(BatteryAndWifiService.class);
        //关闭被动远程喊话的服务
        if (ServiceUtils.isServiceRunning(ReceiverEmergencyAlarmService.class)) {
            ServiceUtils.stopService(ReceiverEmergencyAlarmService.class);
        }
        //关闭服务器接收报警的服务
        if (ServiceUtils.isServiceRunning(RequestWebApiDataService.class))
            ServiceUtils.stopService(RequestWebApiDataService.class);
        //关闭发送心跳的服务
        if (ServiceUtils.isServiceRunning(TimingSendHbService.class))
            ServiceUtils.stopService(TimingSendHbService.class);
        //关闭定时的检测Sip服务
        if (ServiceUtils.isServiceRunning(TimingCheckSipStatus.class))
            ServiceUtils.stopService(TimingCheckSipStatus.class);
        //定时更新服务
        if (ServiceUtils.isServiceRunning(TimingAutoUpdateService.class))
            ServiceUtils.stopService(TimingAutoUpdateService.class);

        if (ServiceUtils.isServiceRunning(SipService.class)) {
            SipService.removeRegistrationCallback();
            SipService.removeMessageCallback();
            SipService.removePhoneCallback();
            SipManager.destroy();
            ServiceUtils.stopService(SipService.class);
        }

    }

    @Override
    public void onLowMemory() {
        Logutil.d("onLowMemory");
        if (ServiceUtils.isServiceRunning(SipService.class)) {
            SipService.removeRegistrationCallback();
            SipService.removeMessageCallback();
            SipService.removePhoneCallback();
        }
        super.onLowMemory();
    }

    @Override
    public void onTerminate() {
        Logutil.i("程序终止");
        exit();
        super.onTerminate();
    }


}
