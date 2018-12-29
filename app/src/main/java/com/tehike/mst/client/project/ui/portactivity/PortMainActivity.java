package com.tehike.mst.client.project.ui.portactivity;

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

import com.tehike.mst.client.project.R;
import com.tehike.mst.client.project.base.App;
import com.tehike.mst.client.project.base.BaseActivity;
import com.tehike.mst.client.project.cms.AmmoRequestCallBack;
import com.tehike.mst.client.project.cms.SendEmergencyAlarmToServerThrad;
import com.tehike.mst.client.project.entity.SipBean;
import com.tehike.mst.client.project.entity.VideoBean;
import com.tehike.mst.client.project.linphone.Linphone;
import com.tehike.mst.client.project.linphone.MessageCallback;
import com.tehike.mst.client.project.linphone.PhoneCallback;
import com.tehike.mst.client.project.linphone.RegistrationCallback;
import com.tehike.mst.client.project.linphone.SipManager;
import com.tehike.mst.client.project.linphone.SipService;
import com.tehike.mst.client.project.services.RequestWebApiDataService;
import com.tehike.mst.client.project.services.ServiceUtils;
import com.tehike.mst.client.project.services.TimingSendHbService;
import com.tehike.mst.client.project.sysinfo.SysInfoBean;
import com.tehike.mst.client.project.global.AppConfig;
import com.tehike.mst.client.project.sysinfo.SysinfoUtils;
import com.tehike.mst.client.project.utils.CryptoUtil;
import com.tehike.mst.client.project.utils.FileUtil;
import com.tehike.mst.client.project.utils.GsonUtils;
import com.tehike.mst.client.project.utils.HttpBasicRequest;
import com.tehike.mst.client.project.utils.Logutil;
import com.tehike.mst.client.project.utils.NetworkUtils;
import com.tehike.mst.client.project.utils.SharedPreferencesUtils;
import com.tehike.mst.client.project.utils.ToastUtils;
import com.tehike.mst.client.project.utils.WriteLogToFile;

import org.json.JSONObject;
import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneChatMessage;
import org.linphone.core.LinphoneChatRoom;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 描述：竖屏的主界面
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2018/10/29 14:25
 */

public class PortMainActivity extends BaseActivity {
    /**
     * 电量信息
     */
    @BindView(R.id.icon_electritity_show)
    ImageView batteryIcon;

    /**
     * 信号强度
     */
    @BindView(R.id.icon_network)
    ImageView rssiIcon;

    /**
     * 显示当前时间分秒
     */
    @BindView(R.id.sipinfor_title_time_layout)
    TextView currentTimeLayout;

    /**
     * 显示当前的年月日
     */
    @BindView(R.id.sipinfor_title_date_layout)
    TextView currentYearLayout;

    /**
     * 消息图标
     */
    @BindView(R.id.icon_message_show)
    ImageView messageIcon;

    /**
     * 连接状态
     */
    @BindView(R.id.icon_connection_show)
    ImageView connetIcon;

    /**
     * 蓝牙开箱的简易动画布局
     */
    @BindView(R.id.open_box_loading_icon_land_layout)
    ImageView loadingIcon;

    /**
     * 加载动画
     */
    Animation mLoadingAnim;

    /**
     * 本机的ip
     */
    String nativeIP = "";

    /**
     * 显示时间的显示是否正在运行
     */
    boolean threadIsRun = true;

    /**
     * 记录双击退出时间
     */
    private long recordingExitTime = 0;

    /**
     * 本机Sip信息
     */
    String nativeSipNumber = "";

    PopupWindow popu;

    /**
     * 应急报警弹窗
     */
    PopupWindow alarmPopuWindow = null;


    RefreshVideoDataBroadcast broadcast;

    /**
     * 本地缓存的所有的视频数制（视频字典）
     */
    List<SipBean> allSipSourcesList;

    @Override
    protected int intiLayout() {
        return R.layout.activity_port_main;
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {

        //设置屏幕方向标识
        AppConfig.APP_DIRECTION = 1;

        initializeParamater();
        startAllService();

        //显示当前的时间
        initializeTime();

        //先判断本地的视频源数据是否存在(报异常)
        try {
            String videoSourceStr = FileUtil.readFile(AppConfig.SOURCES_SIP).toString();
            allSipSourcesList = GsonUtils.GsonToList(CryptoUtil.decodeBASE64(videoSourceStr), SipBean.class);
        } catch (Exception e) {
            //异常后，注册广播监听videoSource数据是否初始化成功
            registerRefreshVideoDataBroadcast();
        }
    }

    /**
     * 广播（用于接收视频字典缓存成功后，适配本页面数据）
     */
    private void registerRefreshVideoDataBroadcast() {
        broadcast = new RefreshVideoDataBroadcast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AppConfig.RESOLVE_VIDEO_DONE_ACTION);
        this.registerReceiver(broadcast, intentFilter);
    }

    /**
     * SipSources字典广播
     */
    class RefreshVideoDataBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                //取出本地缓存的所有的Video数据
                allSipSourcesList = GsonUtils.GsonToList(CryptoUtil.decodeBASE64(FileUtil.readFile(AppConfig.SOURCES_SIP).toString()), SipBean.class);
            } catch (Exception e) {
                Logutil.e("取video字典广播异常---->>>" + e.getMessage());
            }
        }
    }

    /**
     * 按钮的点击事件
     */
    @OnClick({R.id.port_reserve_btn1, R.id.port_reserve_btn2, R.id.port_reserve_btn3, R.id.port_reserve_btn4, R.id.btn_living_meeting, R.id.btn_cluster_intercom, R.id.emergency_call_btn, R.id.btn_alarm_call, R.id.btn_video_intercom, R.id.btn_video, R.id.btn_apply_for_play, R.id.btn_setup})
    public void onclickEvent(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.btn_video_intercom:
                //sip通话（跳转指定的fragment）
                JumpSpecifiPage(0);
                break;
            case R.id.btn_video:
                //视频 监控
                JumpSpecifiPage(1);
                break;
            case R.id.btn_apply_for_play:
                //申请开箱
                openBluetoothBox();
                break;
            case R.id.btn_setup:
                //设置中心
                VerifyPwdDialog(intent);
                break;
            case R.id.btn_alarm_call:
                //应急报警
                displayAlarmSelectionPopupWindow();
                break;
            case R.id.emergency_call_btn:
                //应急呼叫
                //  emergencyCallDutyRoom();
                break;
            case R.id.btn_living_meeting:
                showProgressSuccess("正在开发！");
                break;
            case R.id.btn_cluster_intercom:
                showProgressSuccess("正在开发！");
                break;
            case R.id.port_reserve_btn1:
                showProgressSuccess("正在开发！");
                break;
            case R.id.port_reserve_btn2:
                showProgressSuccess("正在开发！");
                break;
            case R.id.port_reserve_btn3:
                showProgressSuccess("正在开发！");
                break;
            case R.id.port_reserve_btn4:
                showProgressSuccess("正在开发！");
                break;
        }
    }

    /**
     * 跳转指定页面
     */
    private void JumpSpecifiPage(int current) {
        Intent intent = new Intent();
           intent.setClass(PortMainActivity.this, PortMainFragmentActivity.class);
        intent.putExtra("current", current);
        PortMainActivity.this.startActivity(intent);
    }

    /**
     * 初始化数据
     */
    private void initializeParamater() {
        //获取本机的ip
        if (NetworkUtils.isConnected())
            nativeIP = NetworkUtils.getIPAddress(true);
        //加载动画
        mLoadingAnim = AnimationUtils.loadAnimation(this, R.anim.loading);
    }

    /**
     * 弹出验证密码框
     *
     * @param intent
     */
    private void VerifyPwdDialog(final Intent intent) {
        //显示的view
        View view = LayoutInflater.from(this).inflate(R.layout.prompt_verification_pwd_layout, null);
        //控件显示内容
        final EditText editTextPwd = view.findViewById(R.id.verification_pwd_layout);
        //确认按键
        TextView sureVerifyBtn = view.findViewById(R.id.verification_sure_layout);
        //popuwindow显示
        popu = new PopupWindow(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        popu.setContentView(view);
        //显示所在的位置
        View rootview = LayoutInflater.from(PortMainActivity.this).inflate(R.layout.activity_port_main, null);
        popu.showAtLocation(rootview, Gravity.CENTER, 0, 0);
        popu.setBackgroundDrawable(new BitmapDrawable());
        popu.setFocusable(true);
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
                //取出本地的密碼
                String userPwd = (String) SharedPreferencesUtils.getObject(App.getApplication(), "userPwd", "");
                if (TextUtils.isEmpty(userPwd))
                    userPwd = AppConfig.PWD;
                //判断输入的口令是否正确
                if (edPwd.equals(userPwd)) {
                    //正确就消失popu并跳转
                    if (popu.isShowing()) {
                        popu.dismiss();
                    }
                      intent.setClass(PortMainActivity.this, PortSettingActivity.class);
                    PortMainActivity.this.startActivity(intent);
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
    private void openBluetoothBox() {
        //显示动画
        loadingIcon.setVisibility(View.VISIBLE);
        loadingIcon.setAnimation(mLoadingAnim);

        //子线程去执行开启弹箱的操作
        AmmoRequestCallBack ammoRequestCallBack = new AmmoRequestCallBack(new AmmoRequestCallBack.GetDataListern() {
            @Override
            public void getDataInformation(String result) {
                if (TextUtils.isEmpty(result)) {
                    Logutil.i("开箱结果为null");
                    return;
                }
                Message message = new Message();
                message.what = 12;
                message.obj = result;
                handler.sendMessage(message);
            }
        });
        ammoRequestCallBack.start();
    }

    /**
     * 显示报警选择窗口
     */
    private void displayAlarmSelectionPopupWindow() {
        //加载报警选择布局的view
        View view = LayoutInflater.from(this).inflate(R.layout.activity_port_home_alert_dialog_layout, null);
        //popuwindow显示
        alarmPopuWindow = new PopupWindow(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        alarmPopuWindow.setContentView(view);
        //在当前根布局中显示view
        View rootview = LayoutInflater.from(PortMainActivity.this).inflate(R.layout.activity_port_main, null);
        //显示位置
        alarmPopuWindow.showAtLocation(rootview, Gravity.CENTER, 0, 0);
        alarmPopuWindow.setBackgroundDrawable(new BitmapDrawable());
        //设置不可点击不可获取焦点
        alarmPopuWindow.setFocusable(false);
        alarmPopuWindow.setTouchable(false);
        //设置点击外部不可取消
        alarmPopuWindow.setOutsideTouchable(false);
        //设置背景的透明度
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.4f;
        getWindow().setAttributes(lp);
        //设置popuwindow中view的点击事件
        clickEvent(view, alarmPopuWindow);
    }

    /**
     * 弹窗内点击事件
     */
    private void clickEvent(View view, final PopupWindow popu) {
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
                    popu.dismiss();
                }
            }
        });
        popu.setOnDismissListener(new PopupWindow.OnDismissListener() {
            //在dismiss中恢复透明度
            public void onDismiss() {
                //设置透明背景
                final WindowManager.LayoutParams lp = getWindow().getAttributes();
                getWindow().setAttributes(lp);
                lp.alpha = 1f;
                getWindow().setAttributes(lp);
            }
        });
    }

    /**
     * 向后台服务器发送报警消息
     *
     * @param type
     */
    private void sendEmergencyAlarm(String type) {
        //判断网络是否通
        if (!NetworkUtils.isConnected()) {
            handler.sendEmptyMessage(2);
            return;
        }
        //判断当前获取的ip是否这空
        if (TextUtils.isEmpty(nativeIP)) {
            handler.sendEmptyMessage(2);
            return;
        }

        if (allSipSourcesList != null && allSipSourcesList.size() > 0) {
            Logutil.i("本机无面部视频");
            return;
        }
        VideoBean videoBean = null;
        for (int i = 0; i < allSipSourcesList.size(); i++) {
            if (allSipSourcesList.get(i).getNumber().equals("1017")) {
                videoBean = allSipSourcesList.get(i).getVideoBean();
                break;
            }
        }

        try {
            if (videoBean == null || TextUtils.isEmpty(videoBean.toString())) {
                String videoSourceStr = FileUtil.readFile(AppConfig.SOURCES_VIDEO).toString();
                List<VideoBean> allVideoSources = GsonUtils.GsonToList(CryptoUtil.decodeBASE64(videoSourceStr), VideoBean.class);
                videoBean = allVideoSources.get(0);
            }

        } catch (Exception e) {

        }
        SendEmergencyAlarmToServerThrad sendEmergencyAlarmToServer = new SendEmergencyAlarmToServerThrad(videoBean, type, new SendEmergencyAlarmToServerThrad.Callback() {
            @Override
            public void getCallbackData(String result) {
                //判断请求结果是否为空
                if (TextUtils.isEmpty(result)) {
                    Logutil.e("应急报警返回信息为Null:");
                    return;
                }
                //handler发送消息去处理
                Message callbackMessage = new Message();
                callbackMessage.what = 9;
                callbackMessage.obj = result;
                handler.sendMessage(callbackMessage);
            }
        });
        //执行子线程
        new Thread(sendEmergencyAlarmToServer).start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (popu != null && popu.isShowing()) {
            popu.dismiss();
            popu = null;
        }
        return super.onTouchEvent(event);
    }

    /**
     * popupwindow显示时，阻止activty的点击事件
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        //当popuwindow显示时阻止当前页面事件分发（分发机制）
        if (alarmPopuWindow != null && alarmPopuWindow.isShowing()) {
            return false;
        }
        return super.dispatchTouchEvent(event);
    }





    private void registerSipToServer(SysInfoBean sysInfoBean) {
    }


    /**
     * 显示当前的时间
     */
    private void initializeTime() {
        //时间显示
        CurrentHourTimeThead timeThread = new CurrentHourTimeThead();
        new Thread(timeThread).start();

        //显示当前的年月日
        displayCurrentYearTime();
    }

    //显示时间的线程
    class CurrentHourTimeThead extends Thread {
        @Override
        public void run() {
            super.run();
            do {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                handler.sendEmptyMessage(4);
            } while (threadIsRun);
        }
    }

    /**
     * 显示当前的年月日
     */
    private void displayCurrentYearTime() {
        //时间格式
        SimpleDateFormat dateD = new SimpleDateFormat("yyyy年MM月dd日");
        Date date = new Date();
        //显示
        currentYearLayout.setText(dateD.format(date).toString());
    }

    /**
     * 显示当前的时分秒显示
     */
    private void displayCurrentHourTime() {
        //时间
        Date date = new Date();
        //时间格式
        SimpleDateFormat timeD = new SimpleDateFormat("HH:mm:ss");
        //formt日期
        String currentTime = timeD.format(date).toString();
        //显示时分秒
        if (!TextUtils.isEmpty(currentTime)) {
            currentTimeLayout.setText(currentTime);
        }
    }

    /**
     * 启动服务
     */
    private void startAllService() {
        //启动获取Webapi的数据
        if (!ServiceUtils.isServiceRunning(RequestWebApiDataService.class))
            ServiceUtils.startService(RequestWebApiDataService.class);

        //定时发送心跳
        if (!ServiceUtils.isServiceRunning(TimingSendHbService.class))
            ServiceUtils.startService(TimingSendHbService.class);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //显示状态图标
        disPlayAppStatusIcon();

        //linphone状态回调
        linphoneCallback();
    }

    /**
     * 显示当前app的状态图标
     */
    private void disPlayAppStatusIcon() {

        int level = AppConfig.DEVICE_BATTERY;
        if (level >= 75 && level <= 100) {
            updateUi(batteryIcon, R.mipmap.icon_electricity_a);
        }
        if (level >= 50 && level < 75) {
            updateUi(batteryIcon, R.mipmap.icon_electricity_b);
        }
        if (level >= 25 && level < 50) {
            updateUi(batteryIcon, R.mipmap.icon_electricity_c);
        }
        if (level >= 0 && level < 25) {
            updateUi(batteryIcon, R.mipmap.icon_electricity_disable);
        }

        int rssi = AppConfig.DEVICE_WIFI;

        if (rssi > -50 && rssi < 0) {
            updateUi(rssiIcon, R.mipmap.icon_network);
        } else if (rssi > -70 && rssi <= -50) {
            updateUi(rssiIcon, R.mipmap.icon_network_a);
        } else if (rssi < -70) {
            updateUi(rssiIcon, R.mipmap.icon_network_b);
        } else if (rssi == -200) {
            updateUi(rssiIcon, R.mipmap.icon_network_disable);
        }

        if (SipService.isReady() || SipManager.isInstanceiated()) {
            LinphoneChatRoom[] rooms = SipManager.getLc().getChatRooms();
            if (TextUtils.isEmpty(nativeSipNumber)) {
                return;
            }
            for (int j = 0; j < rooms.length; j++) {
                int unRead = rooms[j].getUnreadMessagesCount();
                Logutil.i("unRead:" + unRead);
                if (unRead > 0)
                    handler.sendEmptyMessage(16);
                else
                    handler.sendEmptyMessage(15);
            }
        }

        if (AppConfig.SIP_STATUS) {
            handler.sendEmptyMessage(6);
        } else {
            handler.sendEmptyMessage(7);
        }
    }

    /**
     * 更新UI
     */
    public void updateUi(final ImageView imageView, final int n) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageView.setBackgroundResource(n);
            }
        });
    }


    /**
     * Linphone状态回调
     */
    private void linphoneCallback() {

        //linphone状态回调
        Linphone.addCallback(new RegistrationCallback() {
            @Override
            public void registrationProgress() {
                Logutil.i("registering");
            }

            @Override
            public void registrationOk() {
                updateUi(connetIcon, R.mipmap.icon_connection_normal);
            }

            @Override
            public void registrationFailed() {
                updateUi(connetIcon, R.mipmap.icon_connection_disable);
            }
        }, new PhoneCallback() {
            @Override
            public void incomingCall(LinphoneCall linphoneCall) {

            }

            @Override
            public void outgoingInit() {

            }

            @Override
            public void callConnected() {
            }

            @Override
            public void callEnd() {
            }

            @Override
            public void callReleased() {

            }

            @Override
            public void error() {

            }
        });

        //添加消息回调并显示消息提醒
        SipService.addMessageCallback(new MessageCallback() {
            @Override
            public void receiverMessage(LinphoneChatMessage linphoneChatMessage) {
                handler.sendEmptyMessage(10);
            }
        });

        //判断当前用户是否还有未读消息

        if (SipService.isReady() || SipManager.isInstanceiated()) {
            //获取所有的聊天室
            LinphoneChatRoom[] rooms = SipManager.getLc().getChatRooms();
            if (rooms.length > 0) {
                //遍历聊天室
                for (LinphoneChatRoom room : rooms) {
                    //使未读消息为空已读
                    int unReadMessCount = room.getUnreadMessagesCount();
                    //判断是否有未读的消息数量
                    if (unReadMessCount > 0) {
                        handler.sendEmptyMessage(10);
                    } else {
                        handler.sendEmptyMessage(11);
                    }
                }
            }
        }
    }


    @Override
    protected void onDestroy() {

        if (popu != null && popu.isShowing()) {
            popu.dismiss();
            popu = null;
        }

        //停止时间线程
        threadIsRun = false;

        if (broadcast != null){
            this.unregisterReceiver(broadcast);
        }

        //移除handler监听
        if (handler != null)
            handler.removeCallbacksAndMessages(null);

        super.onDestroy();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2:
                    //提示无网络
                    if (isVisible)
                        showProgressFail(getString(R.string.view_network_error));
                    break;
                case 3:
                    break;
                case 4://显示当前的时间
                    if (isVisible)
                        displayCurrentHourTime();
                    break;
                case 5:

                    break;
                case 10:
                    if (isVisible)
                        messageIcon.setBackgroundResource(R.mipmap.newmessage);
                    break;
                case 11:
                    if (isVisible)
                        messageIcon.setBackgroundResource(R.mipmap.message);
                    break;
                case 21://提示密码不能为空
                    ToastUtils.showShort("不能为空!");
                    break;
                case 22://提示密码不正确
                    ToastUtils.showShort("密码不正确!");
                    break;
            }
        }
    };

    @Override
    public void onNetChange(int state, String name) {

    }
}
