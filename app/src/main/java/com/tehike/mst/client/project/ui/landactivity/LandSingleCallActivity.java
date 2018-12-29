package com.tehike.mst.client.project.ui.landactivity;

import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tehike.mst.client.project.R;
import com.tehike.mst.client.project.base.App;
import com.tehike.mst.client.project.base.BaseActivity;
import com.tehike.mst.client.project.entity.SipBean;
import com.tehike.mst.client.project.entity.VideoBean;
import com.tehike.mst.client.project.global.AppConfig;
import com.tehike.mst.client.project.linphone.Linphone;
import com.tehike.mst.client.project.linphone.PhoneCallback;
import com.tehike.mst.client.project.linphone.RegistrationCallback;
import com.tehike.mst.client.project.services.BatteryAndWifiService;
import com.tehike.mst.client.project.sysinfo.SysinfoUtils;
import com.tehike.mst.client.project.utils.ActivityUtils;
import com.tehike.mst.client.project.utils.BatteryUtils;
import com.tehike.mst.client.project.utils.CryptoUtil;
import com.tehike.mst.client.project.utils.FileUtil;
import com.tehike.mst.client.project.utils.GsonUtils;
import com.tehike.mst.client.project.utils.Logutil;
import com.tehike.mst.client.project.utils.SharedPreferencesUtils;
import com.tehike.mst.client.project.utils.TimeUtils;

import org.linphone.core.LinphoneCall;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import cn.nodemedia.NodePlayer;
import cn.nodemedia.NodePlayerDelegate;
import cn.nodemedia.NodePlayerView;
import me.lake.librestreaming.core.listener.RESConnectionListener;
import me.lake.librestreaming.filter.hardvideofilter.BaseHardVideoFilter;
import me.lake.librestreaming.filter.hardvideofilter.HardVideoGroupFilter;
import me.lake.librestreaming.ws.StreamAVOption;
import me.lake.librestreaming.ws.StreamLiveCameraView;
import me.lake.librestreaming.ws.filter.hardfilter.GPUImageBeautyFilter;
import me.lake.librestreaming.ws.filter.hardfilter.extra.GPUImageCompatibleFilter;

/**
 * 描述：横屏的电话通话界面
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2018/10/16 15:09
 */
public class LandSingleCallActivity extends BaseActivity implements View.OnClickListener, NodePlayerDelegate {

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
     * 来电时间显示
     */
    @BindView(R.id.show_call_time)
    public TextView show_call_time;

    /**
     * 挂电话Icon
     */
    @BindView(R.id.btn_handup_icon)
    public ImageButton hangupButton;

    /**
     * 静音
     */
    @BindView(R.id.btn_mute)
    public ImageButton btn_mute;

    /**
     * 声音加
     */
    @BindView(R.id.btn_volumeadd)
    public ImageButton btn_volumeadd;

    /**
     * 摄像头切换
     */
    @BindView(R.id.btn_camera)
    public ImageButton btn_camera;

    /**
     * 声音减
     */
    @BindView(R.id.btn_volumelow)
    public ImageButton btn_volumelow;

    /**
     * 网络状态
     */
    @BindView(R.id.icon_network)
    public ImageView rssiIcon;

    /**
     * 电量
     */
    @BindView(R.id.icon_electritity_show)
    ImageView batteryIcon;

    /**
     * sip连接状态
     */
    @BindView(R.id.icon_connection_show)
    ImageView connetIcon;

    /**
     * 正在与谁通话
     */
    @BindView(R.id.text_who_is_calling_information)
    public TextView text_who_is_calling_information;

    /**
     * 远端播放器
     */
    @BindView(R.id.remotevideo_layout)
    NodePlayerView remoteView;

    /**
     * 远端播放器进度条
     */
    @BindView(R.id.main_progressbar)
    ProgressBar remotePr;

    /**
     * 显示远端播放器加载信息
     */
    @BindView(R.id.remote_loading_infor)
    TextView remoteLoading;

    /**
     * 远端视频显示的主区域
     */
    @BindView(R.id.main_player_framelayout)
    FrameLayout remoteFrameLyout;

    /**
     * 本地端的视频区域
     */
    @BindView(R.id.second_player_relativelayout)
    RelativeLayout nativeRelativeLayout;

    /**
     * 显示通话对象
     */
    @BindView(R.id.show_who_calling_layout)
    TextView show_who_calling_layout;

    /**
     * 获取所有的sip数据(用于查找对方的视频源)
     */
    List<SipBean> rtspSources = new ArrayList<>();

    /**
     * 本地采集视频预览区域
     */
    @BindView(R.id.secodary_surfacevie)
    StreamLiveCameraView mLiveCameraView;

    /**
     * 声音控制对象
     */
    AudioManager mAudioManager = null;

    /**
     * 远端播放器
     */
    NodePlayer remotePlayer;

    /**
     * 来源是打电话还是接电话，true为打电话，false为接电话
     */
    boolean isCall = true;

    /**
     * 线程是否正在运行
     */
    boolean threadIsRun = true;

    /**
     * 来电者
     */
    String userName = "";

    /**
     * 是否是视频电话
     */
    boolean isVideo = false;

    /**
     * 显示当前的电量
     */
    @BindView(R.id.prompt_electrity_values_land_layout)
    TextView displayCurrentBattery;

    /**
     * 来电是否是视频电话
     */
    boolean isComingVideo = false;

    /**
     * 是否静音
     */
    boolean isSilent = false;

    /**
     * 时间显示线程是否正在远行
     */
    boolean mWorking = false;

    /**
     * 计时的子线程
     */
    Thread mThread = null;

    /**
     * 时间戳
     */
    int num = 0;

    /**
     * rtmp推流设置对象
     */
    private StreamAVOption streamAVOption;


    /**
     * 对方视频源
     */
    String remoteRtsp = "";

    /**
     * 来电者的哨位名称
     */
    String remoteDeviceName = "";

    /**
     * 本机摄像头采集图像传递的地址(rtmp地址)
     */
    private String nativeRTMP = "";


    @Override
    protected int intiLayout() {
        return R.layout.activity_land_calling;
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {

        //初始化推流的rtmp地址
        initializeParamater();

        //初始化View
        initializeView();

        //显示时间
        initializeTime();

        //电话监听
        callingMonitoring();

        //初始化本页面数据
        initializeData();
    }

    /**
     * 初始化本页面数据
     */
    private void initializeData() {

        //获取Intent传值
        isCall = this.getIntent().getBooleanExtra("isMakingCall", false);//是打电话还是接电话
        userName = this.getIntent().getStringExtra("callerNumber");//对方号码
        isVideo = this.getIntent().getBooleanExtra("isMakingVideoCall", false);
        isComingVideo = this.getIntent().getBooleanExtra("isVideoCall", false);

        //初始化数据
        initData();

        //电话是否接通
        boolean isConnect = this.getIntent().getBooleanExtra("isCallConnected", false);
        if (isConnect) {
            handler.sendEmptyMessage(4);
        }
        //拨打视频电话
        if (isCall && isVideo) {
            Logutil.i("拨打视频电话");
            outGoingWithVideo();
        }
        //拨打语音电话
        if (isCall && !isVideo) {
            Logutil.i("拨打语音电话");
            outGoingWithOutVideo();
        }
        //接视频电话
        if (!isCall && isComingVideo) {
            Logutil.i("接视频电话");
            inComingWithVideo();
        }
    }

    /**
     * 初始化rtmp推流通参数
     */
    private void initializeParamater() {
        //服务器地址
        String serverIp = SysinfoUtils.getSysinfo().getWebresourceServer();
        if (TextUtils.isEmpty(serverIp))
            serverIp = AppConfig.SERVERIP;

        //本机sip号码
        String sipNum = SysinfoUtils.getSysinfo().getSipUsername();

        //sips服务器地址
        String sipServerIp = SysinfoUtils.getSysinfo().getSipServer();

        //拼接rtmp地址
        nativeRTMP = "rtmp://" + serverIp + "/oflaDemo/" + sipNum + "@" + sipServerIp;
    }

    /**
     * 接视频电话
     */
    private void inComingWithVideo() {
        //初始化rtmp参数
        initRtmpParamater();

        //开启推流通
        if (!mLiveCameraView.isStreaming()) {
            mLiveCameraView.startStreaming(nativeRTMP);
        }
        //挂断图标
        hangupButton.setBackgroundResource(R.drawable.btn_hangup_select);
        remoteFrameLyout.setVisibility(View.VISIBLE);
        nativeRelativeLayout.setVisibility(View.VISIBLE);
        text_who_is_calling_information.setVisibility(View.GONE);
        //播放对方的视频源
        if (!TextUtils.isEmpty(remoteRtsp)) {
            remotePlayer.setInputUrl(remoteRtsp);
            remotePlayer.setAudioEnable(false);
            remotePlayer.start();
        } else {
            //提示未加载到对方的视频源
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    remotePr.setVisibility(View.GONE);
                    remoteLoading.setVisibility(View.VISIBLE);
                    remoteLoading.setText("未加载到对方的视频资源...");
                }
            });
        }
    }

    /**
     * 向外打语音电话
     */
    private void outGoingWithOutVideo() {
        //显示与谁正在通话
        text_who_is_calling_information.setVisibility(View.VISIBLE);
        nativeRelativeLayout.setVisibility(View.GONE);
        //显示与哪个设备正在通话
        if (!TextUtils.isEmpty(remoteDeviceName)) {
            show_who_calling_layout.setText("正在与《  " + remoteDeviceName + "  》 通话");
            text_who_is_calling_information.setText("正在与《  " + remoteDeviceName + "  》 通话");
        } else {
            show_who_calling_layout.setText("正在与《  " + remoteDeviceName + "  》 通话");
            text_who_is_calling_information.setText("正在与《  " + userName + "  》 通话");
        }
    }

    /**
     * 初始化时间显示+
     */
    private void initializeTime() {
        //时间线程
        TimingThread timeThread = new TimingThread();
        new Thread(timeThread).start();
        //显示当前的年月日
        SimpleDateFormat dateD = new SimpleDateFormat("yyyy年MM月dd日");
        long time = System.currentTimeMillis();
        Date date = new Date(time);
        currentYearLayout.setText(dateD.format(date).toString());
        //显示当前的电量
        int electricityValues = BatteryUtils.getSystemBattery(App.getApplication());
        displayCurrentBattery.setText(electricityValues + "");
    }

    /**
     * 向外拨打视频电话
     */
    private void outGoingWithVideo() {
        //初始化rtmp参数
        initRtmpParamater();

        //开始推流
        if (!mLiveCameraView.isStreaming()) {
            mLiveCameraView.startStreaming(nativeRTMP);
        }
        //隐藏或显示不同的布局view
        remoteFrameLyout.setVisibility(View.VISIBLE);
        text_who_is_calling_information.setVisibility(View.GONE);
        nativeRelativeLayout.setVisibility(View.VISIBLE);
        //播放对方的视频源
        if (!TextUtils.isEmpty(remoteRtsp)) {
            remotePlayer.setInputUrl(remoteRtsp);
            remotePlayer.setAudioEnable(false);
            remotePlayer.start();
        } else {
            remotePr.setVisibility(View.GONE);
            remoteLoading.setText("未加载到对方的视频资源...");
        }
        //显示与哪个设备通话
        if (!TextUtils.isEmpty(remoteDeviceName)) {
            show_who_calling_layout.setText("正在与《  " + remoteDeviceName + "  》 通话");
            text_who_is_calling_information.setText("正在与《  " + remoteDeviceName + "  》 通话");
        } else {
            show_who_calling_layout.setText("正在与《  " + remoteDeviceName + "  》 通话");
            text_who_is_calling_information.setText("正在与《  " + userName + "  》 通话");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //更新app状态图标
        updateAppStatusIcon();
    }

    /**
     * 更改状态栏图标
     */
    private void updateAppStatusIcon() {
        //Sip注册状态
        if (AppConfig.SIP_STATUS) {
            connetIcon.setBackgroundResource(R.mipmap.icon_connection_normal);
        } else {
            connetIcon.setBackgroundResource(R.mipmap.icon_connection_disable);
        }
        //信号强度
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
    }

    /**
     * 初始化本页面控件
     */
    private void initializeView() {
        //视频播放器的回调
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        remotePlayer = new NodePlayer(this);
        remotePlayer.setPlayerView(remoteView);
        remotePlayer.setNodePlayerDelegate(this);
        hangupButton.setOnClickListener(this);
        btn_camera.setOnClickListener(this);
        btn_volumelow.setOnClickListener(this);
        btn_mute.setOnClickListener(this);
        btn_volumeadd.setOnClickListener(this);
    }

    /**
     * rtmp数据传输设置
     */
    private void initRtmpParamater() {
        //推流参数设置
        streamAVOption = new StreamAVOption();
        streamAVOption.streamUrl = nativeRTMP;
        mLiveCameraView.init(App.getApplication(), streamAVOption);
        //推流状态回调
        mLiveCameraView.addStreamStateListener(resConnectionListener);
        LinkedList<BaseHardVideoFilter> files = new LinkedList<>();
        files.add(new GPUImageCompatibleFilter(new GPUImageBeautyFilter()));
        mLiveCameraView.setHardVideoFilter(new HardVideoGroupFilter(files));
    }

    /**
     * 来电监听
     */
    private void callingMonitoring() {

        Linphone.addCallback(new RegistrationCallback() {
            @Override
            public void registrationOk() {
                handler.sendEmptyMessage(8);
            }

            @Override
            public void registrationFailed() {
                handler.sendEmptyMessage(9);
            }
        }, new PhoneCallback() {
            @Override
            public void incomingCall(LinphoneCall linphoneCall) {

                Logutil.i("来电");

            }

            @Override
            public void outgoingInit() {
                Logutil.i("打电话");
                handler.sendEmptyMessage(6);
            }

            @Override
            public void callConnected() {
                Logutil.i("电话接通");
                handler.sendEmptyMessage(4);
            }

            @Override
            public void callEnd() {
                Logutil.i("电话结束");
                handler.sendEmptyMessage(5);

            }

            @Override
            public void callReleased() {
                Logutil.i("电话释放");
                handler.sendEmptyMessage(5);

            }

            @Override
            public void error() {
                Logutil.i("电话错误");
                handler.sendEmptyMessage(5);
            }
        });
    }

    /**
     * 计时线程开启
     */
    public void threadStart() {
        Logutil.i("启动线程计时...");
        mWorking = true;
        if (mThread != null && mThread.isAlive()) {
            Logutil.i("start: thread is alive");
        } else {
            mThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (mWorking) {

                        try {
                            Thread.sleep(1 * 1000);
                            handler.sendEmptyMessage(1);
                        } catch (InterruptedException e) {
                            Logutil.e("线程异常:" + e.getMessage());
                        }
                    }
                }
            });
            mThread.start();
        }
    }

    /**
     * 计时线程停止
     */
    public void threadStop() {
        if (mWorking) {
            if (mThread != null && mThread.isAlive()) {
                mThread.interrupt();
                mThread = null;
            }
            show_call_time.setText("00:00");
            mWorking = false;
        }
    }

    /**
     * 关闭播放器
     */
    public void closePlayer() {
        if (remotePlayer != null) {
            remotePlayer.pause();
            remotePlayer.stop();
            remotePlayer.release();
            remotePlayer = null;
        }
    }

    /**
     * 加载数据
     */
    private void initData() {
        if (rtspSources != null && rtspSources.size() > 0) {
            rtspSources.clear();
        }
        try {
            rtspSources = GsonUtils.GsonToList(CryptoUtil.decodeBASE64(FileUtil.readFile(AppConfig.SOURCES_SIP).toString()), SipBean.class);
        } catch (Exception e) {
        }

        if (rtspSources == null || rtspSources.size() == 0) {
            return;
        }
        for (SipBean mDevice : rtspSources) {
            if (mDevice.getNumber().equals(userName)) {
                String rtsp = mDevice.getVideoBean().getRtsp();
                if (!TextUtils.isEmpty(rtsp)) {
                    remoteRtsp = rtsp;
                }
                String deviceType = mDevice.getName();
                if (!TextUtils.isEmpty(deviceType)) {
                    remoteDeviceName = deviceType;
                }
            }
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
     * 屏蔽返回键
     */
    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }


    /**
     * 推流状态回调
     */
    RESConnectionListener resConnectionListener = new RESConnectionListener() {
        @Override
        public void onOpenConnectionResult(int result) {
            //result 0成功  1 失败
            Logutil.i("打开推流连接 状态：" + result);
        }

        @Override
        public void onWriteError(int errno) {
            Logutil.i("推流出错,请尝试重连");
        }

        @Override
        public void onCloseConnectionResult(int result) {
            //result 0成功  1 失败
            Logutil.i("关闭推流连接 状态：" + result);
        }
    };

    /**
     * 显示时间的线程
     */
    class TimingThread extends Thread {
        @Override
        public void run() {
            super.run();
            do {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
                handler.sendEmptyMessage(7);
            } while (threadIsRun);
        }
    }

    /**
     * 显示当前的时间
     */
    private void displayCurrentTime() {
        long time = System.currentTimeMillis();
        Date date = new Date(time);
        SimpleDateFormat timeD = new SimpleDateFormat("HH:mm:ss");
        String currentTime = timeD.format(date).toString();
        if (!TextUtils.isEmpty(currentTime)) {
            if (isVisible)
                currentTimeLayout.setText(currentTime);
        }
    }


    /***
     * 播放器状态回调
     *
     *
     * @param player
     * @param event
     * @param msg
     */
    @Override
    public void onEventCallback(NodePlayer player, int event, String msg) {
        if (player == remotePlayer) {
            if (event == 1001) {
                handler.sendEmptyMessage(2);
            }
            if (event == 1003) {
                handler.sendEmptyMessage(3);
            }
        }
    }


    /**
     * 网络状态变化回调
     *
     * @param state
     * @param name
     */
    @Override
    public void onNetChange(int state, String name) {

    }

    /**
     * 挂电话
     */
    private void hangUpPhone() {
        Linphone.hangUp();
        num = 0;
        threadStop();
        if (mLiveCameraView != null)
            mLiveCameraView.destroy();
        LandSingleCallActivity.this.finish();
        ActivityUtils.removeActivity(this);
    }

    /**
     * 接通电话
     */
    private void phoneConnected() {
        if (isVisible) {
            threadStart();
            hangupButton.setBackgroundResource(R.drawable.btn_hangup_select);

            if (!TextUtils.isEmpty(remoteDeviceName)) {
                show_who_calling_layout.setText("正在与《  " + remoteDeviceName + "  》 通话");
                text_who_is_calling_information.setText("正在与《  " + remoteDeviceName + "  》 通话");
            } else {
                show_who_calling_layout.setText("正在与《  " + remoteDeviceName + "  》 通话");
                text_who_is_calling_information.setText("正在与《  " + userName + "  》 通话");
            }
        }
    }


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1://通话时间显示
                    num++;
                    if (isVisible)
                        show_call_time.setText(TimeUtils.getTime(num) + "");
                    break;
                case 2://去除加载对方视频信息的load
                    if (isVisible) {
                        remoteLoading.setVisibility(View.GONE);
                        remotePr.setVisibility(View.GONE);
                    }
                    break;
                case 3://显示加载对方视频信息异常
                    if (isVisible) {
                        remoteLoading.setVisibility(View.VISIBLE);
                        remotePr.setVisibility(View.GONE);
                    }
                    break;
                case 4://电话接通
                    phoneConnected();
                    break;
                case 5://停止推流
                    if (mLiveCameraView.isStreaming()) {
                        mLiveCameraView.stopStreaming();
                        mLiveCameraView.destroy();
                    }

                    threadStop();
                    num = 0;
                    show_call_time.setText("00:00");
                    hangupButton.setBackgroundResource(R.drawable.btn_answer_select);
                    closePlayer();
                    LandSingleCallActivity.this.finish();
                    break;

                case 6://显示正在与谁通话
                    if (isVisible) {
                        hangupButton.setBackgroundResource(R.drawable.btn_hangup_select);
                        if (!TextUtils.isEmpty(remoteDeviceName))
                            text_who_is_calling_information.setText("正在呼叫《  " + remoteDeviceName + "  》 ");
                        else
                            text_who_is_calling_information.setText("正在呼叫《  " + userName + "  》 ");
                    }
                    break;
                case 7://显示当前的时间
                    if (isVisible)
                        displayCurrentTime();
                    break;
                case 8://显示sip连接正常
                    if (isVisible)
                        connetIcon.setBackgroundResource(R.mipmap.icon_connection_normal);
                    break;
                case 9://显示sip连接断开
                    if (isVisible)
                        connetIcon.setBackgroundResource(R.mipmap.icon_connection_disable);
                    break;
                case 23://显示当前的电量
                    int level = msg.arg1;
                    if (isVisible)
                        displayCurrentBattery.setText("" + level);
                    break;
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_handup_icon://挂断
                hangUpPhone();
                break;
            case R.id.btn_mute://静音
                if (!isSilent) {
                    Linphone.toggleMicro(true);
                    btn_mute.setBackgroundResource(R.mipmap.btn_mute_pressed);
                    isSilent = true;
                } else {
                    Linphone.toggleMicro(false);
                    btn_mute.setBackgroundResource(R.mipmap.btn_voicetube_pressed);
                    isSilent = false;
                }
                break;
            case R.id.btn_camera: //前后摄像头的转换
                if (isVideo || isComingVideo) {
                    if (mLiveCameraView != null)
                        mLiveCameraView.swapCamera();
                }
                break;
            case R.id.btn_volumeadd:  //音量加
                mAudioManager.adjustStreamVolume(AudioManager.STREAM_VOICE_CALL, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
                break;
            case R.id.btn_volumelow:  //音量减
                mAudioManager.adjustStreamVolume(AudioManager.STREAM_VOICE_CALL, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLiveCameraView != null)
            mLiveCameraView.destroy();
        //停止刷新时间线程的标识
        threadIsRun = false;
        //移除Handler监听
        if (handler != null)
            handler.removeCallbacksAndMessages(null);
    }
}
