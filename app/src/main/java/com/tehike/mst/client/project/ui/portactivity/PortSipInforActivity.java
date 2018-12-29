package com.tehike.mst.client.project.ui.portactivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.text.TextUtils;
import android.util.Base64;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tehike.mst.client.project.R;
import com.tehike.mst.client.project.base.BaseActivity;
import com.tehike.mst.client.project.entity.SipBean;
import com.tehike.mst.client.project.entity.SipClient;
import com.tehike.mst.client.project.entity.VideoBean;
import com.tehike.mst.client.project.global.AppConfig;
import com.tehike.mst.client.project.linphone.Linphone;
import com.tehike.mst.client.project.linphone.MessageCallback;
import com.tehike.mst.client.project.linphone.PhoneCallback;
import com.tehike.mst.client.project.linphone.RegistrationCallback;
import com.tehike.mst.client.project.linphone.SipManager;
import com.tehike.mst.client.project.linphone.SipService;
import com.tehike.mst.client.project.sysinfo.SysinfoUtils;
import com.tehike.mst.client.project.utils.ActivityUtils;
import com.tehike.mst.client.project.utils.ByteUtils;
import com.tehike.mst.client.project.utils.ContextUtils;
import com.tehike.mst.client.project.utils.CryptoUtil;
import com.tehike.mst.client.project.utils.FileUtil;
import com.tehike.mst.client.project.utils.G711Utils;
import com.tehike.mst.client.project.utils.GsonUtils;
import com.tehike.mst.client.project.utils.HttpBasicRequest;
import com.tehike.mst.client.project.utils.Logutil;
import com.tehike.mst.client.project.utils.NetworkUtils;
import com.tehike.mst.client.project.utils.RemoteVoiceRequestUtils;
import com.tehike.mst.client.project.utils.TimeUtils;
import com.tehike.mst.client.project.utils.ToastUtils;
import com.tehike.mst.client.project.utils.UIUtils;
import com.tehike.mst.client.project.utils.WriteLogToFile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneChatMessage;
import org.linphone.core.LinphoneChatRoom;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 描述：
 * 通过webapi接口获取sip组内数据
 * 再通过定时的线程任务池去刷新Sipstatus
 * <p>
 * <p>
 * <p>
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2018/12/28 15:02
 */

public class PortSipInforActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

    /**
     * 显示sipItem的view
     */
    @BindView(R.id.gridview)
    public GridView sipItemView;

    /**
     * 电量信息图标
     */
    @BindView(R.id.icon_electritity_show)
    ImageView batteryIcon;

    /**
     * 信号强度图标
     */
    @BindView(R.id.icon_network)
    ImageView rssiIcon;

    /**
     * 连接状态图标
     */
    @BindView(R.id.icon_connection_show)
    ImageView connetIcon;

    /**
     * 消息图标
     */
    @BindView(R.id.icon_message_show)
    ImageView messIcon;

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
     * 下拉 刷新控件
     */
    @BindView(R.id.sipgrou_intercom_refreshlayout)
    SwipeRefreshLayout mSwipeRefreshLayout;

    /**
     * 底部对讲按键
     */
    @BindView(R.id.port_bottom_intercom_radio_btn)
    RadioButton port_bottom_intercom_radio_btn;

    /**
     * 底部视频监控按键
     */
    @BindView(R.id.port_bottom_video_radio_btn)
    RadioButton port_bottom_video_radio_btn;

    /**
     * 底部聊天按键
     */
    @BindView(R.id.port_bottom_chat_radio_btn)
    RadioButton port_bottom_chat_radio_btn;

    /**
     * 底部报警按键
     */
    @BindView(R.id.port_bottom_alarm_radio_btn)
    RadioButton port_bottom_alarm_radio_btn;

    /**
     * 底部申请换弹按键
     */
    @BindView(R.id.port_bottom_apply_for_play_radio_btn)
    RadioButton port_bottom_apply_for_play_radio_btn;

    /**
     * 底部设置中心按键
     */
    @BindView(R.id.port_bottom_setting_radio_btn)
    RadioButton port_bottom_setting_radio_btn;

    /**
     * 集群对讲
     */
    @BindView(R.id.port_bottom_cluster_intercom_radio_btn)
    RadioButton port_bottom_cluster_intercom_radio_btn;

    /**
     * 会议直播
     */
    @BindView(R.id.port_bottom_live_meeting_radio_btn)
    RadioButton port_bottom_live_meeting_radio_btn;

    /**
     * 显示时间的线程是否正在运行
     */
    boolean threadIsRun = true;

    /**
     * 年月日格式
     */
    SimpleDateFormat yearFormat = null;

    /**
     * 时分秒格式
     */
    SimpleDateFormat hoursFormat = null;

    /**
     * 获取组内所有的sip数据
     */
    List<SipBean> sipGroupItemDataList = new ArrayList<>();

    /**
     * 用于保存sipStatus接口返回的Sip状态
     */
    List<SipClient> sipGroupItemStatusList = new ArrayList<>();

    /**
     * 用于显示远程喊话时间（布局）
     */
    TextView speaking_time = null;

    /**
     * 显示喊话时间的线程
     */
    SpeakingTimeThread thread = null;

    /**
     * 当前的组id
     */
    int groupID = -1;

    /**
     * 上下文
     */
    Context mContext;

    /**
     * 当前的选中项
     */
    int sipGroupItemSelected = -1;

    /**
     * 选中对象的ip
     */
    String sipGroupItemSelectDeviceIp = "";

    /**
     * 远程喊话 的端口
     */
    int sipGroupItemSelectDevicePort = -1;

    /**
     * 选中对象sip号码
     */
    String sipGroupItemSelectDeviceNumber = "";

    /**
     * udp发送声音数据
     */
    DatagramSocket udpSocket = null;

    /**
     * 用于向对方 发起远程喊话指令的socket
     */
    Socket tcpSocket = null;

    /**
     * 记录喊话时间
     */
    int speakingTime = 0;

    /**
     * 声音采样率
     */
    public int frequency = 16000;

    /**
     * 录音时声音缓存大小
     */
    private int rBufferSize;

    /**
     * 录音对象
     */
    private AudioRecord recorder;

    /**
     * 录音线程
     */
    private RecordingVoiceThread mRecordingVoiceThread;

    /**
     * 停止标识
     */
    private boolean stopRecordingFlag = false;

    /**
     * 定时 的线程池任务
     */
    private ScheduledExecutorService mScheduledExecutorService;

    /**
     * 适配器
     */
    SipItemAdapter adapter;


    @Override
    protected int intiLayout() {
        return R.layout.activity_port_sip_infor;
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {
        //上下文
        mContext = this;

        //初始化下拉刷新控件
        initializeRefreshModule();

        //初始化数据
        initializeData();

        //初始化刷新sip状态
        initializeTimRefreshStatus();

        //初始化时间显示
        initializeTime();

        //底部按键的点击事件
        radioBtnChecked();

    }

    /**
     * 初始化本页面数据
     */
    private void initializeData() {
        //获取intent传递的id
        groupID = getIntent().getIntExtra("group_id", -1);
        //判断组id是否是默认值
        if (groupID == -1) {
            handler.sendEmptyMessage(5);
            return;
        }
        //请求sip某个组数据
        requestSipGroupItemData();
    }

    /**
     * 初始化时间显示
     */
    private void initializeTime() {
        //启动线程每秒刷新一下
        TimingThread timeThread = new TimingThread();
        new Thread(timeThread).start();

        //显示当前的年月日
        Date date = new Date();
        //时间格式
        yearFormat = new SimpleDateFormat("yyyy年MM月dd日");
        hoursFormat = new SimpleDateFormat("HH:mm:ss");
        String currntYearDate = yearFormat.format(date);
        //显示当前的年月日
        if (!TextUtils.isEmpty(currntYearDate))
            currentYearLayout.setText(currntYearDate);
    }

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
                    e.printStackTrace();
                }
                handler.sendEmptyMessage(1);
            } while (threadIsRun);
        }
    }

    /**
     * 按键的点击事件
     */
    @OnClick({R.id.port_sipgroup_voice_btn, R.id.port_sipgroup_intercom_btn, R.id.port_sipgroup_remote_waring_btn, R.id.port_sipgroup_video_intercom_btn, R.id.port_sipgroup_remote_shot_btn, R.id.port_sipgroup_remote_speaking_btn})
    public void btnClickEvent(View view) {
        switch (view.getId()) {
            case R.id.port_sipgroup_voice_btn:
                //语音电话
                if (sipGroupItemSelected == -1) {
                    showProgressFail("请选择对象！");
                    return;
                }
                //语音电话
                maekeCall(false);
                break;
            case R.id.port_sipgroup_intercom_btn://聊天
                if (sipGroupItemSelected == -1) {
                    showProgressFail("请选择对象！");
                    return;
                }
                goChatPage();
                break;
            case R.id.port_sipgroup_remote_waring_btn://远程警告
                if (sipGroupItemSelected == -1) {
                    if (isVisible)
                        showProgressFail("请选择对象！");
                    return;
                }
                remoteVoiceMethod(2);
                break;
            case R.id.port_sipgroup_video_intercom_btn://视频电话
                if (sipGroupItemSelected == -1) {
                    if (isVisible)
                        showProgressFail("请选择对象！");
                    return;
                }
                maekeCall(true);
                break;
            case R.id.port_sipgroup_remote_shot_btn://远程鸣枪
                if (sipGroupItemSelected == -1) {
                    if (isVisible)
                        showProgressFail("请选择对象！");
                    return;
                }
                remoteVoiceMethod(3);
                break;
            case R.id.port_sipgroup_remote_speaking_btn://远程喊话
                //判断是否选 中
                if (sipGroupItemSelected == -1) {
                    showProgressFail("请选择对象！");
                    return;
                }
                //判断选中的终端是否手持终端
                if (sipGroupItemDataList != null && sipGroupItemDataList.size() > 0) {
                    //获取设备类型
                    String deviceType = sipGroupItemDataList.get(sipGroupItemSelected).getDeviceType();
                    //判断是移动端就不支持
                    if (!TextUtils.isEmpty(deviceType)) {
                        if (deviceType.equals("TH-C6000")) {
                            handler.sendEmptyMessage(20);
                            return;
                        }

                    }
                    remoteSpeackMethod();
                }
                break;
        }
    }

    /**
     * 实现远程喊话功能
     */
    private void remoteSpeackMethod() {
        //每次操作重新实例化一个udp对象
        if (udpSocket != null)
            try {
                udpSocket = null;
                udpSocket = new DatagramSocket();
            } catch (SocketException e) {
                e.printStackTrace();
            }
        //初始化录音 参数
        initializeRecordParamater();
        //tcp去请求喊话
        requestSpeakingSocket sendSoundData = new requestSpeakingSocket();
        new Thread(sendSoundData).start();
    }

    /**
     * 用于远程喊话请求的子线程
     */
    class requestSpeakingSocket extends Thread {
        @Override
        public void run() {
            try {
                if (tcpSocket == null) {
                    tcpSocket = new Socket(sipGroupItemSelectDeviceIp, AppConfig.REMOTE_PORT);
                    tcpSocket.setSoTimeout(3 * 1000);
                    byte[] requestData = new byte[4 + 4 + 4 + 4];

                    // flag
                    byte[] flag = new byte[4];
                    flag = "RVRD".getBytes();
                    System.arraycopy(flag, 0, requestData, 0, flag.length);

                    // action
                    byte[] action = new byte[4];
                    action[0] = 1;// 0無操作，1遠程喊話，2播放語音警告，3播放鳴槍警告，4遠程監聽，5單向廣播
                    action[1] = 0;
                    action[2] = 0;
                    action[3] = 0;
                    System.arraycopy(action, 0, requestData, 4, action.length);

                    // 接受喊话时=接收语音数据包的 UDP端口(测试)
                    byte[] parameter = new byte[4];
                    System.arraycopy(parameter, 0, requestData, 8, parameter.length);
                    // // 向服务器发消息
                    OutputStream os = tcpSocket.getOutputStream();// 字节输出流
                    os.write(requestData);
                    //   tcpSocket.shutdownOutput();// 关闭输出流
                    // 读取服务器返回的消息
                    InputStream in = tcpSocket.getInputStream();
                    byte[] data = new byte[20];
                    int read = in.read(data);
                    //   System.out.println("返回的數據" + Arrays.toString(data));
                    // 解析数据头
                    byte[] r_flag = new byte[4];
                    for (int i = 0; i < 4; i++) {
                        r_flag[i] = data[i];
                    }
                    String r_DataFlag = new String(r_flag, "gb2312");
                    //     System.out.println("數據頭:" + new String(r_flag, "gb2312"));
                    // 解析返回的請求
                    byte[] r_quest = new byte[4];
                    for (int i = 0; i < 4; i++) {
                        r_quest[i] = data[i + 4];
                    }
                    // 0無操作，1遠程喊話，2播放語音警告，3播放鳴槍警告，4遠程監聽，5單向廣播
                    int r_questCode = r_quest[0];
                    String r_questMess = RemoteVoiceRequestUtils.getMessage(r_questCode);

                    // 返回的状态
                    byte[] r_status = new byte[4];
                    for (int i = 0; i < 4; i++) {
                        r_status[i] = data[i + 8];
                    }
                    int r_statusCode = r_status[0];
                    String r_statusMess = RemoteVoiceRequestUtils.getStatusMessage(r_statusCode);
                    Logutil.i("应答状态:" + r_statusCode + "\t" + r_statusMess);

                    // 返回参数
                    byte[] r_paramater = new byte[4];
                    for (int i = 0; i < 4; i++) {
                        r_paramater[i] = data[i + 12];
                    }
                    Logutil.i(Arrays.toString(r_paramater));
                    sipGroupItemSelectDevicePort = ByteUtils.bytesToInt(r_paramater, 0);

                    if (r_statusMess.equals("Accept"))
                        handler.sendEmptyMessage(12);
                    else {
                        handler.sendEmptyMessage(15);
                        if (tcpSocket != null) {
                            tcpSocket.close();
                            tcpSocket = null;
                        }
                    }
                }
            } catch (Exception e) {
                if (tcpSocket != null) {
                    try {
                        tcpSocket.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    tcpSocket = null;
                }
                handler.sendEmptyMessage(14);
                Logutil.e("error:" + e.getMessage());
            }
        }
    }

    /**
     * 初始化录音 参数
     */
    public void initializeRecordParamater() {
        try {
            //设置录音缓冲区大小
            rBufferSize = AudioRecord.getMinBufferSize(frequency,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);
            //获取录音机对象
            recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    frequency, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, rBufferSize);
        } catch (Exception e) {
            String msg = "ERROR init: " + e.getStackTrace();
            Logutil.e("error:" + msg);
        }
    }

    /**
     * 跳转聊天
     */
    private void goChatPage() {
        if (sipGroupItemSelected != -1) {
            SipBean mSipClient = sipGroupItemDataList.get(sipGroupItemSelected);
            //判断当前选中的对象是否空
            if (mSipClient != null) {
                String currentName = mSipClient.getName();
                //使未读消息状态为为已读
                if (SipService.isReady() || SipManager.isInstanceiated()) {
                    LinphoneChatRoom[] rooms = SipManager.getLc().getChatRooms();
                    if (rooms.length > 0) {
                        for (LinphoneChatRoom room : rooms) {
                            //使未读消息为空已读
                            room.markAsRead();
                        }
                        handler.sendEmptyMessage(16);
                    }
                }
            }
            //跳转页面并传递数据
            if (mSipClient != null) {
                Intent intent = new Intent();
                intent.setClass(PortSipInforActivity.this, PortChatActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("sipclient", mSipClient);
                intent.putExtras(bundle);
                PortSipInforActivity.this.startActivity(intent);
            }
        }
    }

    /**
     * 远程 警告或远程鸣枪
     */
    public void remoteVoiceMethod(int type) {
        if (sipGroupItemDataList != null && sipGroupItemDataList.size() > 0) {
            //获取当前对象的设备类型
            String deviceType = sipGroupItemDataList.get(sipGroupItemSelected).getDeviceType();
            //判断是移动端就不支持
            if (!TextUtils.isEmpty(deviceType)) {
                if (deviceType.equals("TH-C6000")) {
                    handler.sendEmptyMessage(20);
                    return;
                }
            }
        }

        //判断远程操作对象的ip是否为空
        if (TextUtils.isEmpty(sipGroupItemSelectDeviceIp)) {
            handler.sendEmptyMessage(11);
            return;
        }
        //子线程去执行远程操作（鸣枪、警告等）
        RemoteVoiceRequestUtils remoteVoiceRequestUtils = new RemoteVoiceRequestUtils(type, sipGroupItemSelectDeviceIp, new RemoteVoiceRequestUtils.RemoteCallbck() {
            @Override
            public void remoteStatus(String status) {
                if (!TextUtils.isEmpty(status)) {
                    Message message = new Message();
                    message.what = 13;
                    message.obj = status;
                    handler.sendMessage(message);
                }
            }
        });
        //运行子线程
        new Thread(remoteVoiceRequestUtils).start();
    }

    /**
     * 语音通话
     */
    private void maekeCall(boolean isVideoSupport) {
        Intent intent = new Intent();
        //判断自身 的sip状态
//        if (!AppConfig.SIP_STATUS) {
//            handler.sendEmptyMessage(10);
//            return;
//        }
        //判断 要呼叫的号码是否为空
        if (TextUtils.isEmpty(sipGroupItemSelectDeviceNumber)) {
            handler.sendEmptyMessage(10);
            return;
        }
        intent.putExtra("isMakingCall", true);
        if (sipGroupItemSelected != -1) {
            //呼叫
//            Linphone.callTo(sipGroupItemSelectDeviceNumber, true);
//            //开启外放
//            Linphone.toggleSpeaker(true);
            intent.putExtra("isMakingVideoCall", isVideoSupport);
            intent.setClass(PortSipInforActivity.this, PortSingleCallActivity.class);
            intent.putExtra("callerNumber", sipGroupItemSelectDeviceNumber);
            startActivity(intent);
        }
    }

    /**
     * 退出本页面
     */
    @OnClick(R.id.finish_back_layout)
    public void finishPager(View view) {
        exitPager();
    }

    /**
     * 退出本页面时的动作
     */
    private void exitPager() {
        try {
            if (udpSocket != null) {
                udpSocket.close();
            }

            if (tcpSocket != null) {
                tcpSocket.close();
            }

            if (mScheduledExecutorService != null) {
                if (!mScheduledExecutorService.isShutdown()) {
                    mScheduledExecutorService.shutdown();
                    mScheduledExecutorService = null;
                }
            }


        } catch (Exception e) {
        }
        //结束 本页面
        ActivityUtils.removeActivity(this);
        PortSipInforActivity.this.finish();
    }

    /**
     * 刷新本页面
     */
    @OnClick(R.id.loading_more_videosources_layout)
    public void refreshPager(View view) {
        refreshData();
    }

    /**
     * 下拉刷新本页面
     */
    private void refreshData() {
        //显示正在刷新
        mSwipeRefreshLayout.setRefreshing(true);
        //子线程延时两秒拮
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mSwipeRefreshLayout != null)
                    handler.sendEmptyMessage(4);
                mSwipeRefreshLayout.setRefreshing(false);
                if (adapter != null)
                    adapter.notifyDataSetChanged();
            }
        }, 2 * 1000);
    }

    @Override
    public void onRefresh() {
        //显示正在刷新
        mSwipeRefreshLayout.setRefreshing(true);
        //子线程延时两秒拮
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mSwipeRefreshLayout != null)
                    handler.sendEmptyMessage(4);
                mSwipeRefreshLayout.setRefreshing(false);
                if (adapter != null)
                    adapter.notifyDataSetChanged();
            }
        }, 2 * 1000);
        super.onRefresh();
    }

    /**
     * 请求sip某个组内的所有数据
     */
    private void requestSipGroupItemData() {
        //获取某个组内数据
        String sipGroupItemUrl = AppConfig.WEB_HOST + SysinfoUtils.getServerIp() + AppConfig._USIPGROUPS_GROUP;

        //子线程根据组Id请求组数据
        HttpBasicRequest httpThread = new HttpBasicRequest(sipGroupItemUrl + groupID, new HttpBasicRequest.GetHttpData() {
            @Override
            public void httpData(String result) {
                //无数据
                if (TextUtils.isEmpty(result) || result.contains("Execption")) {
                    handler.sendEmptyMessage(1);
                    return;
                }
                //清除数据
                if (sipGroupItemDataList != null && sipGroupItemDataList.size() > 0) {
                    sipGroupItemDataList.clear();
                }
                Logutil.d("组数据" + result);
                //解析sip组内资源数据
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    if (!jsonObject.isNull("errorCode")) {
                        Logutil.w("请求不到数据信息");
                        return;
                    }
                    int count = jsonObject.getInt("count");
                    if (count > 0) {
                        JSONArray jsonArray = jsonObject.getJSONArray("resources");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonItem = jsonArray.getJSONObject(i);
                            //解析
                            SipBean groupItemInfoBean = new SipBean();
                            groupItemInfoBean.setDeviceType(jsonItem.getString("deviceType"));
                            groupItemInfoBean.setId(jsonItem.getString("id"));
                            groupItemInfoBean.setIpAddress(jsonItem.getString("ipAddress"));
                            groupItemInfoBean.setName(jsonItem.getString("name"));
                            groupItemInfoBean.setNumber(jsonItem.getString("number"));
                            groupItemInfoBean.setSentryId(jsonItem.getInt("sentryId")+"");
                            groupItemInfoBean.setState(-1);
                            //判断是否有面部视频
                            if (!jsonItem.isNull("videosource")) {
                                //解析面部视频
                                JSONObject jsonItemVideo = jsonItem.getJSONObject("videosource");
                                if (jsonItemVideo != null) {
                                    //封闭面部视频
                                    VideoBean videoBean = new VideoBean(
                                            jsonItemVideo.getString("channel"),
                                            jsonItemVideo.getString("devicetype"),
                                            jsonItemVideo.getString("id"),
                                            jsonItemVideo.getString("ipaddress"),
                                            jsonItemVideo.getString("name"),
                                            jsonItemVideo.getString("password"),
                                            jsonItemVideo.getInt("port"),
                                            jsonItemVideo.getString("username"), "", "", "", "", "", "");
                                    groupItemInfoBean.setVideoBean(videoBean);
                                }
                            }
                            sipGroupItemDataList.add(groupItemInfoBean);
                        }
                    }
                    handler.sendEmptyMessage(21);
                } catch (JSONException e) {
                    Logutil.e("Sip组内数据解析异常::" + e.getMessage());
                    WriteLogToFile.info("Sip组内数据解析异常::" + e.getMessage());
                }
            }
        });
        new Thread(httpThread).start();
    }

    /**
     * 定时的去刷新状态
     */
    private void initializeTimRefreshStatus() {

        //子线程延时三秒后去刷新状态
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //定时刷新
                timingRefreshSipStatusData();
            }
        }).start();
    }

    /**
     * 定时三秒刷新一下数据（刷新状态）
     */
    private void timingRefreshSipStatusData() {
        //线程任务池
        if (mScheduledExecutorService == null || mScheduledExecutorService.isShutdown())
            mScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        //服务器地址
        String serverIp = SysinfoUtils.getSysinfo().getWebresourceServer();
        if (TextUtils.isEmpty(serverIp))
            serverIp = AppConfig.SERVERIP;
        //用户名
        String userName = SysinfoUtils.getUserName();
        //密码
        String userPwd = SysinfoUtils.getUserPwd();
        if (TextUtils.isEmpty(userName))
            userName = AppConfig.USERNAME;
        if (TextUtils.isEmpty(userPwd))
            userPwd = AppConfig.PWD;
        //线程任务池定时的去刷新sip状态
        if (!mScheduledExecutorService.isShutdown()) {
            mScheduledExecutorService.scheduleWithFixedDelay(new RequestRefreshStatus(userName, userPwd, AppConfig.WEB_HOST + serverIp + AppConfig._SIS_STATUS), 0L, 3000, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 初始化下拉刷新控件
     */
    private void initializeRefreshModule() {
        //设置下拉 颜色
        mSwipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        //设置下拉刷新监听
        mSwipeRefreshLayout.setOnRefreshListener(this);
    }

    /**
     * 获取sip状态的数据
     */
    class RequestRefreshStatus implements Runnable {

        //传入的url
        String url;
        String userName;
        String userPwd;

        /**
         * 构造方法
         */
        public RequestRefreshStatus(String s, String p, String url) {
            this.url = url;
            this.userName = s;
            this.userPwd = p;
        }

        @Override
        public void run() {
            //加同步锁
            synchronized (this) {
                try {
                    //用HttpURLConnection请求
                    HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(3000);
                    String authString = userName + ":" + userPwd;
                    //添加 basic参数
                    con.setRequestProperty("Authorization", "Basic " + new String(Base64.encode(authString.getBytes(), 0)));
                    con.connect();

                    Message message = new Message();
                    message.what = 23;
                    if (con.getResponseCode() == 200) {
                        InputStream in = con.getInputStream();
                        String result = readTxt(in);
                        message.obj = result;
                    } else {
                        message.obj = "";
                    }
                    handler.sendMessage(message);
                    con.disconnect();
                } catch (Exception e) {
                }
            }
        }

        /**
         * 字节 流转可见字符
         *
         * @param in
         * @return
         * @throws IOException
         */
        public String readTxt(InputStream in) throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            return sb.toString();
        }
    }

    /**
     * 展示Sip组数据
     */
    private void disPlaySispGroupItemAdapter() {

        //获取本机的Sip号码
        String currentSipNumber = SysinfoUtils.getSysinfo().getSipUsername();

        //踢除自己
        for (int i = 0; i < sipGroupItemDataList.size(); i++) {
            if (sipGroupItemDataList.get(i).getNumber().equals(currentSipNumber)) {
                continue;
            }
        }
        adapter = new SipItemAdapter(mContext);
        sipItemView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        //item点击事件
        sipItemView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (adapter != null) {
                    if (sipGroupItemDataList != null && sipGroupItemDataList.size() > 0) {
                        //判断选中的是否是在线状态对象
                        if ((sipGroupItemDataList.get(position).getState() + "").equals("1")) {
                            sipGroupItemSelectDeviceIp = sipGroupItemDataList.get(position).getIpAddress();
                            sipGroupItemSelectDeviceNumber = sipGroupItemDataList.get(position).getNumber();
                            if (TextUtils.isEmpty(sipGroupItemSelectDeviceIp) || TextUtils.isEmpty(sipGroupItemSelectDeviceNumber)) {
                                handler.sendEmptyMessage(24);
                            }
                            Logutil.i("sipGroupItemSelectDeviceIp" + sipGroupItemSelectDeviceIp + "\nsipGroupItemSelectDeviceNumber" + sipGroupItemSelectDeviceNumber);
                            sipGroupItemSelected = position;
                        } else {
                            sipGroupItemSelected = -1;
                        }
                        adapter.setSeclection(position);
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    /**
     * 显示正在喊话在dialog
     */
    private void showSpeakingDialog() {
        //加载dialog布局
        View view = View.inflate(PortSipInforActivity.this, R.layout.activity_port_prompt_dialog, null);
        speaking_time = view.findViewById(R.id.speaking_time_layout);
        //正在向谁喊话（布局）
        TextView speaking_name = view.findViewById(R.id.speaking_name_layout);
        //关闭（布局）
        TextView close_dialog = view.findViewById(R.id.seaking_close_dialog_layout);
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(PortSipInforActivity.this);
        builder.setView(view);
        //点击外面使不消失
        builder.setCancelable(false);
        final AlertDialog dialog = builder.create();
        dialog.show();
        //此处设置位置窗体大小
        dialog.getWindow().setLayout(ContextUtils.dip2px(this, 280), ContextUtils.dip2px(this, 280));

        //判断dialog是否正在显示
        if (dialog.isShowing()) {

            //运行喊话计时线程
            if (thread == null)
                thread = new SpeakingTimeThread();
            thread.start();

            String videoSourceStr = FileUtil.readFile(AppConfig.SOURCES_SIP).toString();
            List<SipBean> alterSamples = GsonUtils.GsonToList(CryptoUtil.decodeBASE64(videoSourceStr), SipBean.class);


            //根据sip号码找到相应的设备名称
            if (alterSamples != null && alterSamples.size() > 0) {
                for (SipBean device : alterSamples) {
                    if (device.getNumber().equals(sipGroupItemSelectDeviceNumber)) {
                        String deviceName = device.getName();
                        String str = "正在向\t<<< <b><font color=#ff0000>" + deviceName + "</b><font/> >>>喊话";
                        speaking_name.setText(Html.fromHtml(str));
                        break;
                    }
                }
            }
        }
        //关闭按钮的点击事件
        close_dialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                speakingTime = 0;
                try {
                    stopRecord();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    /**
     * 喊话计时线程
     */
    class SpeakingTimeThread extends Thread {
        @Override
        public void run() {
            super.run();
            do {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Logutil.i("Thread error:" + e.getMessage());
                }
                handler.sendEmptyMessage(14);
            } while (!stopRecordingFlag);
        }
    }

    /**
     * 开始录音
     */
    public void startRecord() {
        stopRecordingFlag = false;
        mRecordingVoiceThread = new RecordingVoiceThread();
        mRecordingVoiceThread.start();
    }

    /**
     * 结束录音
     */
    public void stopRecord() throws IOException {
        if (tcpSocket != null) {
            tcpSocket.close();
            tcpSocket = null;
        }
        if (udpSocket != null) {
            udpSocket.close();
            udpSocket = null;
        }
        stopRecordingFlag = true;
    }

    /**
     * 录音线程
     */
    class RecordingVoiceThread extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                byte[] tempBuffer, readBuffer = new byte[rBufferSize];
                int bufResult = 0;
                recorder.startRecording();
                while (!stopRecordingFlag) {
                    bufResult = recorder.read(readBuffer, 0, rBufferSize);
                    if (bufResult > 0 && bufResult % 2 == 0) {
                        tempBuffer = new byte[bufResult];
                        System.arraycopy(readBuffer, 0, tempBuffer, 0, rBufferSize);
                        G711EncodeVoice(tempBuffer);
                    }
                }
                recorder.stop();
                Looper.prepare();
                Looper.loop();
            } catch (Exception e) {
                String msg = "ERROR AudioRecord: " + e.getMessage();
                Logutil.e(msg);
                Looper.prepare();
                Looper.loop();
            }
        }
    }

    /**
     * G711a声音压缩
     */
    private void G711EncodeVoice(byte[] tempBuffer) {
        DatagramPacket dp = null;
        try {
            dp = new DatagramPacket(G711Utils.encode(tempBuffer), G711Utils.encode(tempBuffer).length, InetAddress.getByName(sipGroupItemSelectDeviceIp), sipGroupItemSelectDevicePort);
            try {
                if (udpSocket == null)
                    udpSocket = new DatagramSocket();
                udpSocket.send(dp);
                Logutil.i("正在发送...." + Arrays.toString(G711Utils.encode(tempBuffer)) + "\n长度" + G711Utils.encode(tempBuffer).length);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理获取到的sip状态数据
     */
    private void handlerSipGroupItemStatus(String result) {
        //先清除前一次请求到的数据
        if (sipGroupItemStatusList != null)
            sipGroupItemStatusList.clear();
        //判断请求数据是否为空
        if (TextUtils.isEmpty(result)) {
            handler.sendEmptyMessage(7);
            return;
        }
        //判断请求数据是否异常
        if (result.contains("Execption") || result.contains("code != 200")) {
            handler.sendEmptyMessage(8);
            return;
        }
        //Gson解析数据
        try {
            JSONArray jsonArray = new JSONArray(result);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String username = jsonObject.getString("usrname");
                String state = jsonObject.getString("state");
                SipClient sipClient = new SipClient(username, "", "", "", state, "", "");
                sipGroupItemStatusList.add(sipClient);
            }

            //组全数据
            if (sipGroupItemStatusList.size() > 0 && sipGroupItemStatusList.size() > 0) {
                for (int i = 0; i < sipGroupItemStatusList.size(); i++) {
                    for (int j = 0; j < sipGroupItemDataList.size(); j++) {
                        if (sipGroupItemStatusList.get(i).getUsrname().equals(sipGroupItemDataList.get(j).getNumber())) {
                            sipGroupItemDataList.get(j).setState(Integer.parseInt(sipGroupItemStatusList.get(i).getState()));
                        }
                    }
                }
            }
            //去刷新适配器
            handler.sendEmptyMessage(22);
        } catch (Exception e) {

        }
    }

    @Override
    protected void onResume() {

        //显示当前的各种状态
        displayAppStatusIcon();

        //sip状态回调
        sipStatusCallback();

        super.onResume();
    }

    /**
     * SIp注册信息回调
     */
    private void sipStatusCallback() {
        if (SipService.isReady() || SipManager.isInstanceiated()) {
            Linphone.addCallback(new RegistrationCallback() {
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
                    super.incomingCall(linphoneCall);
                }

                @Override
                public void outgoingInit() {
                    super.outgoingInit();
                }

                @Override
                public void callConnected() {
                    super.callConnected();
                }

                @Override
                public void callEnd() {
                    super.callEnd();
                }

                @Override
                public void callReleased() {
                    super.callReleased();
                }

                @Override
                public void error() {
                    super.error();
                }
            });


            //添加消息回调并显示消息提醒
            SipService.addMessageCallback(new MessageCallback() {
                @Override
                public void receiverMessage(LinphoneChatMessage linphoneChatMessage) {
                    handler.sendEmptyMessage(16);
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
                            handler.sendEmptyMessage(18);
                        } else {
                            handler.sendEmptyMessage(19);
                        }
                    }
                }
            }

        }
    }

    /**
     * 显示当前的头部状态
     */
    private void displayAppStatusIcon() {

        if (AppConfig.SIP_STATUS) {
            connetIcon.setBackgroundResource(R.mipmap.icon_connection_normal);
        } else {
            connetIcon.setBackgroundResource(R.mipmap.icon_connection_disable);
        }
        //电池电量信息
        int level = AppConfig.DEVICE_BATTERY;
        if (level >= 75 && level <= 100) {
            batteryIcon.setBackgroundResource(R.mipmap.icon_electricity_a);
        }
        if (level >= 50 && level < 75) {
            batteryIcon.setBackgroundResource(R.mipmap.icon_electricity_b);
        }
        if (level >= 25 && level < 50) {
            batteryIcon.setBackgroundResource(R.mipmap.icon_electricity_c);
        }
        if (level >= 0 && level < 25) {
            batteryIcon.setBackgroundResource(R.mipmap.icon_electricity_disable);
        }
        //信号 状态
        int rssi = AppConfig.DEVICE_WIFI;
        if (rssi > -50 && rssi < 0) {
            rssiIcon.setBackgroundResource(R.mipmap.icon_network);
        } else if (rssi > -70 && rssi <= -50) {
            rssiIcon.setBackgroundResource(R.mipmap.icon_network_a);
        } else if (rssi < -70) {
            rssiIcon.setBackgroundResource(R.mipmap.icon_network_b);
        } else if (rssi == -200) {
            rssiIcon.setBackgroundResource(R.mipmap.icon_network_disable);
        }
    }

    /**
     * 底部radioButton的监听事件
     */
    private void radioBtnChecked() {
        //勤务通信
        port_bottom_intercom_radio_btn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    JumpSpecifiPage(0);
                    port_bottom_intercom_radio_btn.setChecked(true);
                    port_bottom_video_radio_btn.setChecked(false);
                    port_bottom_chat_radio_btn.setChecked(false);
                    port_bottom_alarm_radio_btn.setChecked(false);
                    port_bottom_apply_for_play_radio_btn.setChecked(false);
                    port_bottom_setting_radio_btn.setChecked(false);
                    port_bottom_cluster_intercom_radio_btn.setChecked(false);
                    port_bottom_live_meeting_radio_btn.setChecked(false);
                }
            }
        });
        //视频监控
        port_bottom_video_radio_btn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    JumpSpecifiPage(1);
                    port_bottom_intercom_radio_btn.setChecked(false);
                    port_bottom_video_radio_btn.setChecked(true);
                    port_bottom_chat_radio_btn.setChecked(false);
                    port_bottom_alarm_radio_btn.setChecked(false);
                    port_bottom_apply_for_play_radio_btn.setChecked(false);
                    port_bottom_setting_radio_btn.setChecked(false);
                    port_bottom_cluster_intercom_radio_btn.setChecked(false);
                    port_bottom_live_meeting_radio_btn.setChecked(false);
                }
            }
        });
        //即时通信
        port_bottom_chat_radio_btn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    JumpSpecifiPage(2);
                    port_bottom_intercom_radio_btn.setChecked(false);
                    port_bottom_video_radio_btn.setChecked(false);
                    port_bottom_chat_radio_btn.setChecked(true);
                    port_bottom_alarm_radio_btn.setChecked(false);
                    port_bottom_apply_for_play_radio_btn.setChecked(false);
                    port_bottom_setting_radio_btn.setChecked(false);
                    port_bottom_cluster_intercom_radio_btn.setChecked(false);
                    port_bottom_live_meeting_radio_btn.setChecked(false);
                }
            }
        });
        //应急报警
        port_bottom_alarm_radio_btn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    port_bottom_intercom_radio_btn.setChecked(false);
                    port_bottom_video_radio_btn.setChecked(false);
                    port_bottom_chat_radio_btn.setChecked(false);
                    port_bottom_alarm_radio_btn.setChecked(true);
                    port_bottom_apply_for_play_radio_btn.setChecked(false);
                    port_bottom_setting_radio_btn.setChecked(false);
                    port_bottom_cluster_intercom_radio_btn.setChecked(false);
                    port_bottom_live_meeting_radio_btn.setChecked(false);

                    //发送报警到服务器
                    exectAlarmHanderToServer();
                }
            }
        });
        //申请供弹
        port_bottom_apply_for_play_radio_btn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    port_bottom_intercom_radio_btn.setChecked(false);
                    port_bottom_video_radio_btn.setChecked(false);
                    port_bottom_chat_radio_btn.setChecked(false);
                    port_bottom_alarm_radio_btn.setChecked(false);
                    port_bottom_apply_for_play_radio_btn.setChecked(true);
                    port_bottom_setting_radio_btn.setChecked(false);
                    port_bottom_cluster_intercom_radio_btn.setChecked(false);
                    port_bottom_live_meeting_radio_btn.setChecked(false);
                }
            }
        });
        //设置中心
        port_bottom_setting_radio_btn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    port_bottom_intercom_radio_btn.setChecked(false);
                    port_bottom_video_radio_btn.setChecked(false);
                    port_bottom_chat_radio_btn.setChecked(false);
                    port_bottom_alarm_radio_btn.setChecked(false);
                    port_bottom_apply_for_play_radio_btn.setChecked(false);
                    port_bottom_setting_radio_btn.setChecked(true);
                    port_bottom_cluster_intercom_radio_btn.setChecked(false);
                    port_bottom_live_meeting_radio_btn.setChecked(false);

                    VerifyPwdDialog(new Intent());
                }
            }
        });
        //集群对讲
        port_bottom_cluster_intercom_radio_btn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    port_bottom_intercom_radio_btn.setChecked(false);
                    port_bottom_video_radio_btn.setChecked(false);
                    port_bottom_chat_radio_btn.setChecked(false);
                    port_bottom_alarm_radio_btn.setChecked(false);
                    port_bottom_apply_for_play_radio_btn.setChecked(false);
                    port_bottom_setting_radio_btn.setChecked(false);
                    port_bottom_cluster_intercom_radio_btn.setChecked(true);
                    port_bottom_live_meeting_radio_btn.setChecked(false);
                }
            }
        });
        //会议直播
        port_bottom_live_meeting_radio_btn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    port_bottom_intercom_radio_btn.setChecked(false);
                    port_bottom_video_radio_btn.setChecked(false);
                    port_bottom_chat_radio_btn.setChecked(false);
                    port_bottom_alarm_radio_btn.setChecked(false);
                    port_bottom_apply_for_play_radio_btn.setChecked(false);
                    port_bottom_setting_radio_btn.setChecked(false);
                    port_bottom_cluster_intercom_radio_btn.setChecked(false);
                    port_bottom_live_meeting_radio_btn.setChecked(true);
                }
            }
        });
    }

    /**
     * 弹出验证密码框
     */
    private void VerifyPwdDialog(final Intent intent) {

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
        View rootview = LayoutInflater.from(PortSipInforActivity.this).inflate(R.layout.activity_port_sip_infor, null);
        popu.showAtLocation(rootview, Gravity.CENTER, 0, 0);
        popu.setBackgroundDrawable(new BitmapDrawable());
        popu.setFocusable(true);
        popu.setTouchable(true);
        popu.setOutsideTouchable(false);
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
                    handler.sendEmptyMessage(19);
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
//                    intent.setClass(PortSipInforActivity.this, PortSettingActivity.class);
//                    PortSipInforActivity.this.startActivity(intent);
                } else {
                    //不正确就提示
                    handler.sendEmptyMessage(19);
                }
            }
        });

        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    if (popu != null) {
                        popu.dismiss();
                    }
                }
                return false;
            }
        });
    }

    /**
     * 跳转到指定的页面
     */
    private void JumpSpecifiPage(int current) {
        Intent intent = new Intent();
        intent.setClass(PortSipInforActivity.this, PortMainFragmentActivity.class);
        intent.putExtra("current", current);
        PortSipInforActivity.this.startActivity(intent);
        PortSipInforActivity.this.finish();
    }

    /**
     * 发送应急报警
     */
    private void exectAlarmHanderToServer() {

        //判断网络是否通
        if (!NetworkUtils.isConnected()) {
            Logutil.e("网络不通！");
            return;
        }
        //获取本机的Ip地址
        String nativeIP = NetworkUtils.getIPAddress(true);
        if (TextUtils.isEmpty(nativeIP)) {
            Logutil.e("Ip为空！");
            return;
        }
//
//        //子线程去发送应急报警
//        SendEmergencyAlarmToServerThrad sendEmergencyAlarmToServer = new SendEmergencyAlarmToServerThrad(videoBen, "暴狱", new SendEmergencyAlarmToServerThrad.Callback() {
//            @Override
//            public void getCallbackData(String result) {
//                if (TextUtils.isEmpty(result)) {
//                    Logutils.e("应急报警返回信息为Null:");
//                    return;
//                }
//                Message callbackMessage = new Message();
//                callbackMessage.what = 17;
//                callbackMessage.obj = result;
//                handler.sendMessage(callbackMessage);
//            }
//        });
//        //执行子线程
//        new Thread(sendEmergencyAlarmToServer).start();
    }

    /**
     * 适配器
     */
    class SipItemAdapter extends BaseAdapter {
        //选中对象的标识
        private int clickTemp = -1;
        //布局加载器
        private LayoutInflater layoutInflater;

        //构造函数
        public SipItemAdapter(Context context) {
            layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return sipGroupItemDataList.size();
        }

        @Override
        public Object getItem(int position) {
            return sipGroupItemDataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void setSeclection(int position) {
            clickTemp = position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = layoutInflater.inflate(R.layout.activity_port_sipstatus_item, null);
                viewHolder.item_name = (TextView) convertView.findViewById(R.id.item_name);
                viewHolder.mRelativeLayout = (RelativeLayout) convertView.findViewById(R.id.item_layout);
                viewHolder.main_layout = convertView.findViewById(R.id.sipstatus_main_layout);
                viewHolder.deviceType = convertView.findViewById(R.id.device_type_layout);
                viewHolder.unReadMessIcon = convertView.findViewById(R.id.sip_item_newmessage_layout);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            SipBean mSipClient = sipGroupItemDataList.get(position);
            if (mSipClient != null) {
                //显示设备名
                String deviceName = mSipClient.getName();
                if (!TextUtils.isEmpty(deviceName)) {
                    viewHolder.item_name.setText(deviceName);
                } else {
                    viewHolder.item_name.setText("");
                }
                //设备类型
                String deviceType = mSipClient.getDeviceType();
                if (!TextUtils.isEmpty(deviceType)) {
                    if (deviceType.equals("TH-C6000")) {
                        viewHolder.deviceType.setText("移动终端");
                    }
                    if (deviceType.equals("TH-S6100")) {
                        viewHolder.deviceType.setText("哨位终端");
                    }
                    if (deviceType.equals("TH-S6200")) {
                        viewHolder.deviceType.setText("值班终端");
                    }
                }
                //显示状态
                String status = mSipClient.getState() + "";
                switch (status) {
                    case "-1"://未知状态
                        viewHolder.mRelativeLayout.setBackgroundResource(R.mipmap.btn_lixian);
                        break;
                    case "1"://在线
                        viewHolder.mRelativeLayout.setBackgroundResource(R.drawable.sip_call_select_bg);
                        break;
                    case "2"://振铃
                        viewHolder.mRelativeLayout.setBackgroundResource(R.mipmap.btn_zhenling);
                        break;
                    case "3"://通话
                        viewHolder.mRelativeLayout.setBackgroundResource(R.mipmap.btn_tonghua);
                        break;
                }

                //当前的sip号码
                String currentName = mSipClient.getNumber();
                //判断sip服役是否开启
                if (SipManager.isInstanceiated() || SipService.isReady()) {
                    //获取所有的聊天室
                    LinphoneChatRoom[] rooms = SipManager.getLc().getChatRooms();
                    if (rooms.length > 0) {
                        //遍历聊天室
                        for (LinphoneChatRoom room : rooms) {
                            if (room != null) {
                                //获取每个聊天室对号码（对方 ）
                                String fromUserName = room.getPeerAddress().getUserName();
                                //判断当前对象号码和聊天室对方是否是同一个人
                                if (fromUserName.equals(currentName)) {
                                    //判断本机与对方是否存在 未读消息
                                    int unReadCount = room.getUnreadMessagesCount();
                                    //如果有未读消息，更改图标
                                    if (unReadCount > 0) {
                                        viewHolder.unReadMessIcon.setVisibility(View.VISIBLE);
                                        viewHolder.unReadMessIcon.setBackgroundResource(R.mipmap.newmessage3);
                                    } else {
                                        viewHolder.unReadMessIcon.setVisibility(View.GONE);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (clickTemp == position) {
                //默认只有在线状态对能被选中
                if ((sipGroupItemDataList.get(position).getState() + "").equals("1")) {
                    viewHolder.main_layout.setBackgroundResource(R.drawable.sip_selected_bg);
                }
            } else {
                viewHolder.main_layout.setBackgroundColor(Color.TRANSPARENT);
            }

            return convertView;
        }

        /**
         * 内部类
         */
        class ViewHolder {
            //显示设备名
            TextView item_name;
            LinearLayout main_layout;
            RelativeLayout mRelativeLayout;
            //显示设备类型
            TextView deviceType;
            //未读消息图标
            ImageView unReadMessIcon;
        }
    }


    @Override
    public void onNetChange(int state, String name) {

    }


    @Override
    protected void onDestroy() {

        threadIsRun = false;

        stopRecordingFlag = false;

        if (thread != null)
            thread = null;

        if (handler != null)
            handler.removeCallbacksAndMessages(null);

        if (mScheduledExecutorService != null)
            mScheduledExecutorService.shutdown();
        super.onDestroy();
    }


    /**
     * Handler处理发送的子消息
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    //显示时分秒
                    Date date = new Date();
                    String hoursStr = hoursFormat.format(date);
                    if (isVisible) {
                        currentTimeLayout.setText(hoursStr);
                    }
                    break;
                case 2:
                    //显示sip正常连接状态
                    if (isVisible)
                        connetIcon.setBackgroundResource(R.mipmap.icon_connection_normal);
                    break;
                case 3:
                    //sip断开连接状态
                    if (isVisible)
                        connetIcon.setBackgroundResource(R.mipmap.icon_connection_disable);
                    break;
                case 4:
                    //提示刷新完成
                    if (isVisible)
                        showProgressSuccess("刷新完成!");
                    break;
                case 5://提示未获取到组id
                    if (isVisible)
                        showProgressFail("组Id异常!");
                    break;
                case 6://无网时从本地拿数据
//                    String nativeSipData = (String) SharedPreferencesUtils.getObject(PortSipInforActivity.this, "sipData", "");
//                    if (!TextUtils.isEmpty(nativeSipData)) {
//                        List<SipBean> cc = GsonUtils.GsonToList(nativeSipData, SipBean.class);
//                        if (cc != null) {
//                            sipGroupItemDataList = cc;
//                            disPlaySispGroupItemAdapter();
//                        }
//                    }
                    break;
                case 12:
                    //弹出远程喊话
                    if (isVisible) {
                        showSpeakingDialog();
                        startRecord();
                    }
                    break;
                case 13:
                    //提示远程操作结果
                    String result = (String) msg.obj;
                    if (!TextUtils.isEmpty(result)) {
                        if (result.contains("error"))
                            showProgressFail(UIUtils.getString(R.string.unable_connect));
                        else
                            showProgressSuccess(UIUtils.getString(R.string.play_finish));
                    }
                    break;
                case 14:
                    //提示喊话时长
                    speakingTime += 1;
                    String speakTime = TimeUtils.getTime(speakingTime);
                    if (speaking_time != null)
                        if (isVisible)
                            speaking_time.setText(speakTime);
                    break;
                case 15:
                    //提示设备忙
                    if (isVisible)
                        showProgressFail("设备忙！");
                    break;
                case 16:
                    //去除新消息提示
                    if (isVisible)
                        messIcon.setBackgroundResource(R.mipmap.message);
                    break;
                case 17:
                    //应急报警结果回调
                    String returnResult = (String) msg.obj;
                    if (!TextUtils.isEmpty(returnResult)) {
                        if (returnResult.contains("error")) {
                            showProgressFail(returnResult);
                        } else {
                            showProgressSuccess(returnResult);
                        }
                    }
                    break;
                case 19:
                    if (isVisible) {
                        ToastUtils.showLong("信息有错！");
                    }
                    break;
                case 20:
                    if (isVisible) {
                        showProgressFail("终端不支持！");
                    }
                    break;
                case 21:
                    //处理cms数据并适配
                    disPlaySispGroupItemAdapter();
                    break;
                case 22:
                    //刷新适配器
                    if (adapter != null)
                        adapter.notifyDataSetChanged();
                    break;
                case 23:
                    //处理sip状态数据
                    String sisStatusResult = (String) msg.obj;
                    handlerSipGroupItemStatus(sisStatusResult);
                    break;
                case 24:
                    //提示未获取到设备的状态信息
                    if (isVisible)
                        showProgressFail("未获取到设备信息！");
                    break;
            }
        }
    };
}
