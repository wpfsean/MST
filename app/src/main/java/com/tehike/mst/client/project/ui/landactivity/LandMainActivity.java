package com.tehike.mst.client.project.ui.landactivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.tehike.mst.client.project.R;
import com.tehike.mst.client.project.base.App;
import com.tehike.mst.client.project.base.BaseActivity;
import com.tehike.mst.client.project.cms.AmmoRequestCallBack;
import com.tehike.mst.client.project.cms.SendEmergencyAlarmToServerThrad;
import com.tehike.mst.client.project.entity.SipBean;
import com.tehike.mst.client.project.entity.VideoBean;
import com.tehike.mst.client.project.global.AppConfig;
import com.tehike.mst.client.project.linphone.Linphone;
import com.tehike.mst.client.project.linphone.PhoneCallback;
import com.tehike.mst.client.project.linphone.RegistrationCallback;
import com.tehike.mst.client.project.linphone.SipManager;
import com.tehike.mst.client.project.linphone.SipService;
import com.tehike.mst.client.project.services.ReceiverEmergencyAlarmService;
import com.tehike.mst.client.project.services.RequestWebApiDataService;
import com.tehike.mst.client.project.services.ServiceUtils;
import com.tehike.mst.client.project.services.TimingAutoUpdateService;
import com.tehike.mst.client.project.services.TimingCheckSipStatus;
import com.tehike.mst.client.project.services.TimingSendHbService;
import com.tehike.mst.client.project.sysinfo.SysInfoBean;
import com.tehike.mst.client.project.sysinfo.SysinfoUtils;
import com.tehike.mst.client.project.utils.CryptoUtil;
import com.tehike.mst.client.project.utils.FileUtil;
import com.tehike.mst.client.project.utils.GsonUtils;
import com.tehike.mst.client.project.utils.Logutil;
import com.tehike.mst.client.project.utils.NetworkUtils;
import com.tehike.mst.client.project.utils.ToastUtils;
import com.tehike.mst.client.project.utils.WriteLogToFile;

import org.linphone.core.LinphoneCall;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 描述：横屏的首页面
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2018/10/18 15:54
 */
public class LandMainActivity extends BaseActivity {

    /**
     * 显示当前时间分秒
     */
    @BindView(R.id.home_show_current_time_layout)
    TextView disPlayCurrentTimeLayout;

    /**
     * 显示当前的年月日
     */
    @BindView(R.id.show_display_year_layout)
    TextView disPlayCurrentYearLayout;

    /**
     * 显示sip状态的布局
     */
    @BindView(R.id.home_sip_online_layout)
    ImageView disPlaysipIconLayout;

    /**
     * 电池电量状态图标
     */
    @BindView(R.id.home_battery_infor_layout)
    ImageView disPlayBatteryIconLayout;

    /**
     * 信息状态图标
     */
    @BindView(R.id.home_signal_infor_layout)
    ImageView disPlaysipMessIconLayout;

    /**
     * 显示加载动画
     */
    @BindView(R.id.open_box_loading_icon_layout)
    ImageView disPlayLoadingIconLayout;

    /**
     * 线程是否正在运行
     */
    boolean timeThreadIsRun = true;

    /**
     * 显示报警的弹窗
     */
    PopupWindow disPlayAlarmPopuWindow = null;

    /**
     * 本机Ip
     */
    String nativeIpAddrees = "";

    /**
     * 用于接收video资源缓存成功后的广播
     */
    RefreshSipDataBroadcast mRefreshVideoDataBroadcast;

    /**
     * 本地缓存的所有的视频数制（视频字典）
     */
    List<SipBean> allSipSourcesList;

    @Override
    protected int intiLayout() {
        return R.layout.activity_land_main;
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {

        //方向标识
        AppConfig.APP_DIRECTION = 2;

        //初始化所有服务
        initializeAllService();

        //显示当前日期
        initializeCurrentDate();

        //先判断本地的视频源数据是否存在(报异常)
        try {
            String videoSourceStr = FileUtil.readFile(AppConfig.SOURCES_SIP).toString();
            allSipSourcesList = GsonUtils.GsonToList(CryptoUtil.decodeBASE64(videoSourceStr), SipBean.class);
        } catch (Exception e) {
            //异常后，注册广播监听videoSource数据是否初始化成功
            registerRefreshSipDataBroadcast();
        }
        //向sip服务器注册信息
        registerSipWithSipServer();
    }

    /**
     * 广播（用于接收Sip數據字典缓存成功后，适配本页面数据）
     */
    private void registerRefreshSipDataBroadcast() {
        mRefreshVideoDataBroadcast = new RefreshSipDataBroadcast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AppConfig.RESOLVE_VIDEO_DONE_ACTION);
        this.registerReceiver(mRefreshVideoDataBroadcast, intentFilter);
    }

    /**
     * SipSources字典广播
     */
    class RefreshSipDataBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                Logutil.d("Sip數據緩存成功");
                //取出本地缓存的所有的Video数据
                allSipSourcesList = GsonUtils.GsonToList(CryptoUtil.decodeBASE64(FileUtil.readFile(AppConfig.SOURCES_SIP).toString()), SipBean.class);
            } catch (Exception e) {
                Logutil.e("取video字典广播异常---->>>" + e.getMessage());
            }
        }
    }

    /**
     * 显示时间及日期
     */
    private void initializeCurrentDate() {
        //启动显示时间的线程
        TimingThread timeThread = new TimingThread();
        new Thread(timeThread).start();

        //显示当前的年月日
        SimpleDateFormat dateD = new SimpleDateFormat("yyyy年MM月dd日");
        long time = System.currentTimeMillis();
        Date date = new Date(time);
        disPlayCurrentYearLayout.setText(dateD.format(date).toString());

    }

    /**
     * 开启必要的服务
     */
    private void initializeAllService() {
        //获取本机ip
        if (NetworkUtils.isConnected())
            nativeIpAddrees = NetworkUtils.getIPAddress(true);

        //启动获取Webapi的数据
        if (!ServiceUtils.isServiceRunning(RequestWebApiDataService.class))
            ServiceUtils.startService(RequestWebApiDataService.class);

        //定时发送心跳
        if (!ServiceUtils.isServiceRunning(TimingSendHbService.class))
            ServiceUtils.startService(TimingSendHbService.class);

        //启动接收报警的服务
        if (!ServiceUtils.isServiceRunning(ReceiverEmergencyAlarmService.class))
            ServiceUtils.startService(ReceiverEmergencyAlarmService.class);

        //启动Sip保活的服务
        if (!ServiceUtils.isServiceRunning(TimingCheckSipStatus.class))
            ServiceUtils.startService(TimingCheckSipStatus.class);

        //启动Sip保活的服务
//        if (!ServiceUtils.isServiceRunning(TimingAutoUpdateService.class))
//            ServiceUtils.startService(TimingAutoUpdateService.class);

    }

    /**
     * 注册Sip信息
     */
    private void registerSipWithSipServer() {

        if (AppConfig.SIP_STATUS) {
            Logutil.d("当前Sip在线");
            return;
        }
        //获取sysinfo接口数据
        SysInfoBean mSysInfoBean = SysinfoUtils.getSysinfo();
        //判断sysinfo对象是否为空
        if (mSysInfoBean == null) {
            Logutil.e("注册Sip时信息缺失！");
            WriteLogToFile.info("注册Sip时信息缺失！");
            return;
        }
        //获取sip的所有数据
        final String sipNumber = mSysInfoBean.getSipUsername();
        final String sipPwd = mSysInfoBean.getSipPassword();
        final String sipServer = mSysInfoBean.getSipServer();
        //判断sip数据是否为空
        if (TextUtils.isEmpty(sipNumber) || TextUtils.isEmpty(sipPwd) || TextUtils.isEmpty(sipServer)) {
            Logutil.e("注册Sip时信息缺失！");
            WriteLogToFile.info("注册Sip时信息缺失！");
            return;
        }
        if (!SipService.isReady()) {
            Linphone.startService(this);
            Linphone.setAccount(sipNumber, sipPwd, sipServer);
            Linphone.login();
        }
    }

    /**
     * 控制按键的点击事件
     */
    @OnClick({R.id.seting_icon_layout, R.id.the_standby_play_btn1, R.id.video_intercom_btn, R.id.video_btn, R.id.video_phone_btn, R.id.video_live_meeting_btn, R.id.cluster_intercom_btn, R.id.emergency_call_btn, R.id.alarm_btn, R.id.apply_for_play_btn, R.id.the_standby_play_btn2})
    public void clickEvent(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            //进行设置中心按键
            case R.id.seting_icon_layout:
                if (intent != null) {
                    disPlayVerifyPwdDialog(intent);
                }
                break;
            //视频对讲
            case R.id.video_intercom_btn:
                if (intent != null) {
                    intent.setClass(LandMainActivity.this, LandSipGroupActivity.class);
                    startActivity(intent);
                }
                break;
            //视频监控
            case R.id.video_btn:
                if (intent != null) {
                    intent.setClass(LandMainActivity.this, LandMutilScreenActivity.class);
                    startActivity(intent);
                }
                break;
            //t备用功能（即时通信）
            case R.id.video_phone_btn:
                showProgressSuccess("正在开发!");
                break;
            //会议直播
            case R.id.video_live_meeting_btn:
                showProgressSuccess("正在开发!");
                break;
            //集群对讲
            case R.id.cluster_intercom_btn:
                showProgressSuccess("正在开发!");
                break;
            //应急呼叫
            case R.id.emergency_call_btn:
                emergencyCallDutyRoom();
                break;
            //应急报警
            case R.id.alarm_btn:
                DisplayAlarmSelectDialog();
                break;
            //申请供弹
            case R.id.apply_for_play_btn:
                OpenBlueBox();
                break;
            case R.id.the_standby_play_btn1:
                showProgressSuccess("正在开发!");
                break;
            case R.id.the_standby_play_btn2:
                showProgressSuccess("正在开发!");
                break;
        }
    }

    /**
     * 弹出验证密码框
     */
    private void disPlayVerifyPwdDialog(final Intent intent) {

        //显示的view
        View view = LayoutInflater.from(this).inflate(R.layout.prompt_verification_pwd_layout, null);
        //控件显示内容
        final EditText editTextPwd = view.findViewById(R.id.verification_pwd_layout);
        //确认按键
        TextView sureVerifyBtn = view.findViewById(R.id.verification_sure_layout);
        //popuwindow显示
        final PopupWindow popu = new PopupWindow(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        popu.setContentView(view);
        //显示所在的位置
        View rootview = LayoutInflater.from(LandMainActivity.this).inflate(R.layout.activity_land_main, null);
        popu.showAtLocation(rootview, Gravity.CENTER, 0, 0);
        popu.setBackgroundDrawable(new BitmapDrawable());
        popu.setFocusable(true);
        popu.setTouchable(true);
        popu.setOutsideTouchable(true);
        popu.update();
        //设置透明背景
        final WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.4f;
        getWindow().setAttributes(lp);
        //消失后背景透明度恢复
        popu.setOnDismissListener(new PopupWindow.OnDismissListener() {
            //在dismiss中恢复透明度
            public void onDismiss() {
                lp.alpha = 1f;
                getWindow().setAttributes(lp);
            }
        });
        //确认按键监听
        sureVerifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取输入的口令
                String edPwd = editTextPwd.getText().toString().trim();
                if (TextUtils.isEmpty(edPwd)) {
                    handler.sendEmptyMessage(21);
                    return;
                }
                //获取当前用户登录时的口令
                String pwd = SysinfoUtils.getUserPwd();
                //判断输入的口令是否正确
                if (edPwd.equals(pwd)) {
                    //正确就消失popu并跳转
                    if (popu.isShowing()) {
                        popu.dismiss();
                    }
                    intent.setClass(LandMainActivity.this, LandSettingActivity.class);
                    LandMainActivity.this.startActivity(intent);
                } else {
                    //不正确就提示
                    handler.sendEmptyMessage(22);
                }
            }
        });
    }

    /**
     * 申请开启蓝牙弹箱
     */
    private void OpenBlueBox() {
        //加载动画
        Animation mLoadingAnim = AnimationUtils.loadAnimation(this, R.anim.loading);
        disPlayLoadingIconLayout.setVisibility(View.VISIBLE);
        disPlayLoadingIconLayout.setAnimation(mLoadingAnim);
        //子线程去请求开启
        AmmoRequestCallBack ammoRequestCallBack = new AmmoRequestCallBack(new AmmoRequestCallBack.GetDataListern() {
            @Override
            public void getDataInformation(String result) {
                //判断返回的结果是否为空
                if (TextUtils.isEmpty(result)) {
                    Logutil.e("开箱结果为null");
                    WriteLogToFile.info("申请开户子弹箱失败,返回结果为NUll!");
                    return;
                }
                Message message = new Message();
                message.what = 18;
                message.obj = result;
                handler.sendMessage(message);
            }
        });
        ammoRequestCallBack.start();
    }

    /**
     * 弹出报警选择窗口
     */
    private void DisplayAlarmSelectDialog() {

        //要加载的布局view
        View view = LayoutInflater.from(this).inflate(R.layout.activity_land_home_alert_dialog_layout, null);
        //popu显示
        disPlayAlarmPopuWindow = new PopupWindow(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        disPlayAlarmPopuWindow.setContentView(view);
        //popu要显示的位置
        View rootview = LayoutInflater.from(LandMainActivity.this).inflate(R.layout.activity_land_main, null);
        disPlayAlarmPopuWindow.showAtLocation(rootview, Gravity.TOP | Gravity.CENTER, 0, 40);
        disPlayAlarmPopuWindow.setBackgroundDrawable(new BitmapDrawable());
        disPlayAlarmPopuWindow.setFocusable(true);
        disPlayAlarmPopuWindow.setTouchable(false);
        disPlayAlarmPopuWindow.setOutsideTouchable(false);
        //设置透明度
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.4f;
        getWindow().setAttributes(lp);
        //popu内的点击事件
        popuClickEvent(view, disPlayAlarmPopuWindow);
    }

    /**
     * popuwindow显示时，阻止activity事件分发机制
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (disPlayAlarmPopuWindow != null && disPlayAlarmPopuWindow.isShowing()) {
            return false;
        }
        return super.dispatchTouchEvent(event);
    }

    /**
     * 弹窗内按键的点击事件
     */
    private void popuClickEvent(View view, final PopupWindow popu) {
        //脱逃
        view.findViewById(R.id.btn_takeoff).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (popu != null && popu.isShowing()) {
                    sendEmergencyAlarm("脱逃");
                    popu.dismiss();
                }
            }
        });
        //暴狱
        view.findViewById(R.id.btn_prison).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (popu != null && popu.isShowing()) {
                    sendEmergencyAlarm("暴狱");
                    popu.dismiss();
                }
            }
        });
        //袭击
        view.findViewById(R.id.btn_attack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (popu != null && popu.isShowing()) {
                    sendEmergencyAlarm("袭击");
                    popu.dismiss();
                }
            }
        });
        //灾害
        view.findViewById(R.id.btn_disaster).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (popu != null && popu.isShowing()) {
                    sendEmergencyAlarm("灾害");
                    popu.dismiss();

                }
            }
        });
        //挟持
        view.findViewById(R.id.btn_hold).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (popu != null && popu.isShowing()) {
                    sendEmergencyAlarm("挟持");
                    popu.dismiss();
                }
            }
        });
        //突发
        view.findViewById(R.id.btn_burst).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (popu != null && popu.isShowing()) {
                    sendEmergencyAlarm("突发");
                    popu.dismiss();
                }
            }
        });
        //应急
        view.findViewById(R.id.btn_emergency).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (popu != null && popu.isShowing()) {
                    sendEmergencyAlarm("应急");
                    popu.dismiss();
                }
            }
        });
        //关闭
        view.findViewById(R.id.btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (popu != null && popu.isShowing()) {
                    WindowManager.LayoutParams lp = getWindow().getAttributes();
                    lp.alpha = 1.0f;
                    getWindow().setAttributes(lp);
                    popu.dismiss();

                }
            }
        });
    }

    /**
     * 向后台服务器发送报警消息
     */
    private void sendEmergencyAlarm(String type) {

        disPlayAlarmPopuWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            //在dismiss中恢复透明度
            public void onDismiss() {
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1f;
                getWindow().setAttributes(lp);
            }
        });


        //判断网络是否连接
        if (!NetworkUtils.isConnected()) {
            handler.sendEmptyMessage(5);
            return;
        }
        //获取本机ip
        String ip = NetworkUtils.getIPAddress(true);
        if (TextUtils.isEmpty(ip)) {
            handler.sendEmptyMessage(5);
            return;
        }
        VideoBean videoBean = null;

        String nativeSipNumber = SysinfoUtils.getSysinfo().getSipUsername();

        //遍历找到本机的视频源
        for (int i = 0; i < allSipSourcesList.size(); i++) {
            if (allSipSourcesList.get(i).getNumber().equals(nativeSipNumber)) {
                videoBean = allSipSourcesList.get(i).getVideoBean();
                break;
            }
        }
        //若本机没有面部视频信息（模拟一个假的面部视频（不可用））
        if (videoBean == null) {
            Logutil.e("本机无视频源");
            handler.sendEmptyMessage(23);
            return;
        }
        //启动子线程去发送报警信息
        SendEmergencyAlarmToServerThrad sendEmergencyAlarmToServer = new SendEmergencyAlarmToServerThrad(videoBean, type, new SendEmergencyAlarmToServerThrad.Callback() {
            @Override
            public void getCallbackData(String result) {
                if (TextUtils.isEmpty(result)) {
                    Logutil.e("应急报警返回信息为Null:");
                    WriteLogToFile.info("发送应急报警时，返回的信息为null");
                    return;
                }
                Message callbackMessage = new Message();
                callbackMessage.what = 9;
                callbackMessage.obj = result;
                handler.sendMessage(callbackMessage);
            }
        });
        new Thread(sendEmergencyAlarmToServer).start();
    }

    /**
     * 应急呼叫值班室
     */
    private void emergencyCallDutyRoom() {

        //值班室号码
        String duryNumber = "";

        if (allSipSourcesList != null && allSipSourcesList.size() > 0) {
            for (int i = 0; i < allSipSourcesList.size(); i++) {
                if (allSipSourcesList.get(i).getSentryId().equals("0")) {
                    duryNumber = allSipSourcesList.get(i).getNumber();
                    break;
                }
            }
            if (TextUtils.isEmpty(duryNumber)) {
                showProgressFail("未获取到值班室信息!");
                return;
            }

            //sip服务是否已启动
            if (!SipService.isReady() || !SipManager.isInstanceiated()) {
                Linphone.startService(App.getApplication());
            }
            //网络是否正常
            if (!NetworkUtils.isConnected()) {
                showProgressFail("网络异常!");
                return;
            }
            //当前sip是否在线
            if (!AppConfig.SIP_STATUS) {
                showProgressFail("无呼叫权限!");
                return;
            }

            //打电话并跳转
            Linphone.callTo(duryNumber, false);
            Linphone.toggleSpeaker(true);
            Intent intent = new Intent();
            intent.putExtra("isMakingVideoCall", false);
            intent.putExtra("callerNumber", duryNumber);
            intent.putExtra("isMakingCall", true);
            intent.setClass(LandMainActivity.this, LandSingleCallActivity.class);
            startActivity(intent);
        } else {
            showProgressFail("未获取到值班室信息!");
        }
    }

    /**
     * Linphone状态监听回调
     */
    private void linphoneStatusCallback() {

        if (!SipService.isReady() || !SipManager.isInstanceiated()) {
            Linphone.startService(App.getApplication());
        }

        //linphone注册状态及电话状态的监听
        Linphone.addCallback(new RegistrationCallback() {
            @Override
            public void registrationProgress() {
            }

            @Override
            public void registrationOk() {
                handler.sendEmptyMessage(2);
            }

            @Override
            public void registrationFailed() {
                handler.sendEmptyMessage(3);
            }
        }, new PhoneCallback() {
            @Override
            public void incomingCall(LinphoneCall linphoneCall) {
                Logutil.i("linphoneCall");
            }

            @Override
            public void outgoingInit() {
                Logutil.i("outgoingInit");
            }

            @Override
            public void callConnected() {
                Logutil.i("callConnected");
            }

            @Override
            public void callEnd() {
                Logutil.i("callEnd");
            }

            @Override
            public void callReleased() {
                Logutil.i("callReleased");
            }

            @Override
            public void error() {
                Logutil.i("error");
            }
        });
    }

    /**
     * 提示开箱结果
     */
    private void disPlayOpenBoxResult(Message msg) {
        //得到开箱结果
        String result = (String) msg.obj;
        //消失动画
        disPlayLoadingIconLayout.clearAnimation();
        disPlayLoadingIconLayout.setVisibility(View.GONE);
        //提示开箱结果并写入log日志
        if (result.contains("Execption")) {
            showProgressFail("开箱失败！");
        } else {
            showProgressSuccess("开箱成功!");
        }
        WriteLogToFile.info("开箱结果:" + result);
    }

    /**
     * 提示报警结果
     */
    private void disPlayAlarmResult(Message msg) {
        String result = (String) msg.obj;
        if (!TextUtils.isEmpty(result)) {
            //提示报警结果并写入日志
            if (result.contains("error")) {
                showProgressFail("报警失败！");
            } else {
                showProgressSuccess("报警成功！");
            }
            WriteLogToFile.info("报警结果：" + result);
        }
    }

    /**
     * 返回键屏蔽
     */
    @Override
    public void onBackPressed() {
//       super.onBackPressed();
    }

    /**
     * 网络状态变化回调
     */
    @Override
    public void onNetChange(int state, String name) {
        if (state == -1 || state == 5) {
            handler.sendEmptyMessage(5);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //修改状态提示图标
        upDateStatusPromptIcon();

        //添加sip状态回调
        linphoneStatusCallback();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //poPu消失
        if (disPlayAlarmPopuWindow != null) {
            disPlayAlarmPopuWindow.dismiss();
        }
        //取消广播
        if (mRefreshVideoDataBroadcast != null) {
            this.unregisterReceiver(mRefreshVideoDataBroadcast);
        }
        //刷新时间的线程停止
        timeThreadIsRun = false;
        //handler移除监听
        if (handler != null)
            handler.removeCallbacksAndMessages(null);
    }

    /**
     * 显示时间的线程(每秒刷新一下时间)
     */
    class TimingThread extends Thread {
        @Override
        public void run() {
            super.run();
            do {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Logutil.i("Thread error:" + e.getMessage());
                }
                handler.sendEmptyMessage(1);
            } while (timeThreadIsRun);
        }

    }

    /**
     * 显示当前的时间
     */
    private void displayCurrentTime() {
        Date date = new Date();
        SimpleDateFormat timeD = new SimpleDateFormat("HH:mm:ss");
        String currentTime = timeD.format(date).toString();
        if (!TextUtils.isEmpty(currentTime)) {
            if (isVisible)
                disPlayCurrentTimeLayout.setText(currentTime);
        }
    }

    /**
     * 修改状态提示的图标
     */
    private void upDateStatusPromptIcon() {

        //Sip状态
        if (AppConfig.SIP_STATUS) {
            disPlaysipIconLayout.setBackgroundResource(R.mipmap.icon_connection_normal);
        } else {
            disPlaysipIconLayout.setBackgroundResource(R.mipmap.icon_connection_disable);
        }
        //电池电量信息
        int level = AppConfig.DEVICE_BATTERY;
        if (level >= 75 && level <= 100) {
            disPlayBatteryIconLayout.setBackgroundResource(R.mipmap.icon_electricity_a);
        }
        if (level >= 50 && level < 75) {
            disPlayBatteryIconLayout.setBackgroundResource(R.mipmap.icon_electricity_b);
        }
        if (level >= 25 && level < 50) {
            disPlayBatteryIconLayout.setBackgroundResource(R.mipmap.icon_electricity_c);
        }
        if (level >= 0 && level < 25) {
            disPlayBatteryIconLayout.setBackgroundResource(R.mipmap.icon_electricity_disable);
        }
        //信号 状态
        int rssi = AppConfig.DEVICE_WIFI;
        if (rssi > -50 && rssi < 0) {
            disPlaysipMessIconLayout.setBackgroundResource(R.mipmap.icon_network);
        } else if (rssi > -70 && rssi <= -50) {
            disPlaysipMessIconLayout.setBackgroundResource(R.mipmap.icon_network_a);
        } else if (rssi < -70) {
            disPlaysipMessIconLayout.setBackgroundResource(R.mipmap.icon_network_b);
        } else if (rssi == -200) {
            disPlaysipMessIconLayout.setBackgroundResource(R.mipmap.icon_network_disable);
        }
    }

    /**
     * Handler处理子线程发送的消息
     */
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case 1://显示当前时间
                    if (isVisible)
                        displayCurrentTime();
                    break;
                case 2://sip连接正常状态提示
                    if (isVisible)
                        disPlaysipIconLayout.setBackgroundResource(R.mipmap.icon_connection_normal);
                    break;
                case 3://sip断开状态提示
                    disPlaysipIconLayout.setBackgroundResource(R.mipmap.icon_connection_disable);
                    break;
                case 4://linphone服务未开启
                    showProgressFail("电话功能未启动！");
                    //开始服务
                    Linphone.startService(App.getApplication());
                    break;
                case 5://网络异常提示
                    if (isVisible)
                        showProgressFail("请检查网络！");
                    break;
                case 6://未获取值班室信息
                    showProgressFail("未获取到值班室信息！");
                    break;
                case 7://本机sip未注册
                    showProgressFail("没有拨号权限！");
                    break;
                case 8://提示值班室不在线
                    showProgressFail("值班室忙或未在线！");
                    break;
                case 9://提示报警结果
                    if (isVisible) {
                        disPlayAlarmResult(msg);
                    }
                    break;
                case 18://提示开箱结果
                    if (isVisible) {
                        disPlayOpenBoxResult(msg);
                    }
                    break;
                case 20://提示密码错误
                    ToastUtils.showShort("密码错误！");
                    break;
                case 21://提示密码不能为空
                    ToastUtils.showShort("不能为空!");
                    break;
                case 22://提示密码不正确
                    ToastUtils.showShort("密码不正确!");
                    break;
                case 23:
                    showProgressFail("本机无视频源");
                    break;

            }
        }
    };
}

