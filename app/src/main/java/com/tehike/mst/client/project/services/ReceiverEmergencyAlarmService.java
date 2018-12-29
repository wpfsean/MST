package com.tehike.mst.client.project.services;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.tehike.mst.client.project.R;
import com.tehike.mst.client.project.base.App;
import com.tehike.mst.client.project.entity.AlarmBean;
import com.tehike.mst.client.project.entity.VideoBean;
import com.tehike.mst.client.project.onvif.ResolveVideoSourceRtsp;
import com.tehike.mst.client.project.sysinfo.SysinfoUtils;
import com.tehike.mst.client.project.utils.ByteUtils;
import com.tehike.mst.client.project.utils.Logutil;
import com.tehike.mst.client.project.utils.ScreenUtils;
import com.tehike.mst.client.project.utils.WriteLogToFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import cn.nodemedia.NodePlayer;
import cn.nodemedia.NodePlayerView;

/**
 * 接收友邻哨报警功能服务
 */

public class ReceiverEmergencyAlarmService extends Service {

    /**
     * 接收报警的子线程
     */
    ReceiverEmergencyAlarm mReceiverEmergencyAlarm = null;

    /**
     * Tcp服务接收报警
     */
    ServerSocket serverSocket = null;

    /**
     * 报警来源
     */
    String alarmSourceAddress = "";

    /**
     * 报警点视频名称
     */
    String alarmSourceName = "";

    /**
     * 报警类型
     */
    String alarmSourceType = "";

    /**
     * 播放报警视频的播放器
     */
    NodePlayer nodePlayer = null;

    /**
     * 播放器所有的View
     */
    NodePlayerView mNodePlayerView = null;

    /**
     * 接收报警服务的端口
     */
    int port = -1;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //启动子线程执行socket服务
        if (mReceiverEmergencyAlarm == null)
            mReceiverEmergencyAlarm = new ReceiverEmergencyAlarm();
        new Thread(mReceiverEmergencyAlarm).start();

        Logutil.d("启动接收友邻哨报警服务");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //停止tcp服务
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            serverSocket = null;
        }

        //释放播放器
        if (nodePlayer != null){
            if (nodePlayer.isPlaying()){
                nodePlayer.stop();
            }
            nodePlayer.release();
        }

        //移除Handler监听
        if (handler != null)
            handler.removeCallbacksAndMessages(null);
    }


    /**
     * 接收友邻哨报警
     */
    class ReceiverEmergencyAlarm extends Thread {
        @Override
        public void run() {
            //从数据库取出报警接收的端口
            port = SysinfoUtils.getSysinfo().getNeighborWatchPort();
            try {
                //Tcpserver接收报警报文
                if (serverSocket == null)
                    serverSocket = new ServerSocket(port, 3);
                InputStream inputstream = null;
                while (true) {
                    Socket socket = null;
                    try {
                        socket = serverSocket.accept();
                        inputstream = socket.getInputStream();
                        //falge 4
                        byte[] header = new byte[524];
                        int read = inputstream.read(header);

                        byte[] flagByte = new byte[4];
                        for (int i = 0; i < 4; i++) {
                            flagByte[i] = header[i];
                        }
                        //报警报文的头
                        String flage = new String(flagByte, "gb2312");

                        //senderIpByte 32
                        byte[] senderIpByte = new byte[32];
                        for (int i = 0; i < 32; i++) {
                            senderIpByte[i] = header[i + 4];
                        }
                        int senderP = ByteUtils.getPosiotion(senderIpByte);
                        //获取报警报文的senderip
                        String senderIpAddress = new String(senderIpByte, 0, senderP, "gb2312");
                        //视频数据头
                        byte[] videoFlageByte = new byte[4];
                        for (int i = 0; i < 4; i++) {
                            videoFlageByte[i] = header[i + 36];
                        }
                        String videoFlage = new String(videoFlageByte, "gb2312");
                        //唯一识别编号
                        byte[] videoIdByte = new byte[48];
                        for (int i = 0; i < 48; i++) {
                            videoIdByte[i] = header[i + 40];
                        }
                        int videoIdP = ByteUtils.getPosiotion(videoIdByte);
                        String videoId = new String(videoIdByte, 0, videoIdP, "gb2312");

                        //视频源名称
                        byte[] videoNameByte = new byte[128];
                        for (int i = 0; i < 128; i++) {
                            videoNameByte[i] = header[i + 88];
                        }
                        int videoNameP = ByteUtils.getPosiotion(videoNameByte);
                        String videoName = new String(videoNameByte, 0, videoNameP, "gb2312");

                        //设备类型，默认为 ONVIF，终端视频源为RTSP
                        byte[] videoDeviceTypeByte = new byte[16];
                        for (int i = 0; i < 16; i++) {
                            videoDeviceTypeByte[i] = header[i + 216];
                        }
                        int videoDeviceTypeP = ByteUtils.getPosiotion(videoDeviceTypeByte);
                        String deviceType = new String(videoDeviceTypeByte, 0, videoDeviceTypeP, "gb2312");

                        //视频源设备IP地址
                        byte[] videoIPAddressByte = new byte[32];
                        for (int i = 0; i < 16; i++) {
                            videoIPAddressByte[i] = header[i + 232];
                        }
                        int videoIPAddressP = ByteUtils.getPosiotion(videoIPAddressByte);
                        String videoIpAddress = new String(videoIPAddressByte, 0, videoIPAddressP, "gb2312");

                        //视频源设备端口
                        int sentryId = ByteUtils.bytesToInt(header, 264);
                        System.out.println(sentryId);

                        //视频源通道编号，默认为 1	设备类型=RTSP时，Channel保存rtsp uri 的PathAndQuery 部分

                        byte[] videoChannelByte = new byte[128];
                        for (int i = 0; i < 128; i++) {
                            videoChannelByte[i] = header[i + 268];
                        }
                        int videoChannelP = ByteUtils.getPosiotion(videoChannelByte);
                        String videoChannel = new String(videoChannelByte, 0, videoChannelP, "gb2312");

                        //用户名
                        byte[] userByte = new byte[32];
                        for (int i = 0; i < 32; i++) {
                            userByte[i] = header[i + 396];
                        }
                        int userP = ByteUtils.getPosiotion(userByte);
                        String userName = new String(userByte, 0, userP, "gb2312");

                        //口令
                        byte[] pwdByte = new byte[32];
                        for (int i = 0; i < 32; i++) {
                            pwdByte[i] = header[i + 428];
                        }
                        int pwdP = ByteUtils.getPosiotion(pwdByte);
                        String pwd = new String(pwdByte, 0, pwdP, "gb2312");

                        //报警类型
                        byte[] videoAlarmTypeByte = new byte[32];
                        for (int i = 0; i < 32; i++) {
                            videoAlarmTypeByte[i] = header[i + 460];
                        }
                        int videoAlarmTypeP = ByteUtils.getPosiotion(videoAlarmTypeByte);
                        String alarmType = new String(videoAlarmTypeByte, 0, videoAlarmTypeP, "gb2312");

                        VideoBean videoBen = new VideoBean(videoChannel, deviceType, videoId, videoIpAddress, videoName, pwd, -1, userName, "", "", "", "", "", "");
                        AlarmBean alarmBen = new AlarmBean(senderIpAddress, videoBen, alarmType, "");

                        if (alarmBen != null) {
                            Message message = new Message();
                            message.obj = alarmBen;
                            message.what = 1;
                            handler.sendMessage(message);
                        } else {
                            Logutil.e("接收到的报警信息为空");
                        }

                    } catch (IOException e) {
                    } finally {
                        if (socket != null) {
                            try {
                                socket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Logutil.e("接收友邻哨报警socket异常:" + e.getMessage());
            }
        }
    }

    /**
     * 处理报警信息
     */
    private void receiveAlarmInfo(AlarmBean alarmBen) {

        //判断报警源信息是否为空
        if (alarmBen == null) {
            Logutil.e("接收报警的对象为空！");
            WriteLogToFile.info("接收到的报警信息为空！");
            return;
        }

        alarmSourceAddress = alarmBen.getSender();
        alarmSourceName = alarmBen.getVideoBen().getName();
        alarmSourceType = alarmBen.getAlertType();


        VideoBean videoBean = alarmBen.getVideoBen();
        String deviceType = videoBean.getDevicetype();
        String ip = videoBean.getIpaddress();
        //先判断设备类型和ip是否为空
        if (!TextUtils.isEmpty(deviceType) && !TextUtils.isEmpty(ip)) {
            if (deviceType.toUpperCase().equals("ONVIF")) {
                videoBean.setServiceUrl("http://" + videoBean.getIpaddress() + "/onvif/device_service");
                ResolveVideoSourceRtsp onvif = new ResolveVideoSourceRtsp(videoBean, new ResolveVideoSourceRtsp.GetRtspCallback() {
                    @Override
                    public void getDeviceInfoResult(String rtsp, boolean isSuccess, VideoBean mVideoBean) {
                        //handler处理解析返回的设备对象
                        Message message = new Message();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("device", mVideoBean);
                        message.setData(bundle);
                        message.what = 2;
                        handler.sendMessage(message);
                    }
                });
                //执行线程
                App.getExecutorService().execute(onvif);
            } else if (deviceType.toUpperCase().equals("RTSP")) {
                //若设备类型是RTSP类型，拼加成rtsp
                String mRtsp = "rtsp://" + videoBean.getUsername() + ":" + videoBean.getPassword() + "@" + videoBean.getIpaddress() + "/" + videoBean.getChannel();
                //同样用handler处理这个设备对象
                Message message = new Message();
                Bundle bundle = new Bundle();
                videoBean.setRtsp(mRtsp);
                bundle.putSerializable("device", videoBean);
                message.setData(bundle);
                message.what = 2;
                handler.sendMessage(message);
            } else if (deviceType.toUpperCase().equals("RTMP")) {
                //若设备类型是RTSP类型，拼加成rtsp
                String mRtsp = videoBean.getChannel();
                //同样用handler处理这个设备对象
                Message message = new Message();
                Bundle bundle = new Bundle();
                videoBean.setRtsp(mRtsp);
                bundle.putSerializable("device", videoBean);
                message.setData(bundle);
                message.what = 2;
                handler.sendMessage(message);
            }
        } else {
            //如果为空说明没面部视频
            Message message = new Message();
            Bundle bundle = new Bundle();
            bundle.putSerializable("device", videoBean);
            message.setData(bundle);
            message.what = 2;
            handler.sendMessage(message);
        }
    }

    /**
     * 显示报警信息
     */
    private void promptAlarmInfo(VideoBean device) {

        //获取最终要播放的视频地址
        String videoUrl = "";
        if (device != null) {
            if (!TextUtils.isEmpty(device.getRtsp())) {
                videoUrl = device.getRtsp();
            }
        }
        //用dialog显示
        final AlertDialog.Builder builder = new AlertDialog.Builder(App.getApplication());
        builder.setCancelable(false);

        final View view = View.inflate(App.getApplication(), R.layout.prompt_receive_alarm_info_dialog_layout, null);
        builder.setView(view);
        //关闭按键
        ImageButton closeBtn = view.findViewById(R.id.prompt_alarm_close_btn_layout);
        //视频加载提示
        final TextView loadingLayout = view.findViewById(R.id.prompt_alarm_loading_layout);
        //播放器视图
        mNodePlayerView = view.findViewById(R.id.prompt_alarm_video_view_layout);
        //显示滚文字
        TextView alarmLayout = view.findViewById(R.id.auto_scroll_text_layout);
        //显示报警信息
        String alarmResult = alarmSourceName + "发生" + alarmSourceType + "报警,发生地点" + alarmSourceAddress;
        alarmLayout.setText(alarmResult);

        //如果未获取 到报警源的播放地址
        if (TextUtils.isEmpty(videoUrl)) {
            mNodePlayerView.setVisibility(View.GONE);
            loadingLayout.setVisibility(View.VISIBLE);
        }
        Logutil.i("播放地址:" + videoUrl);

        nodePlayer = new NodePlayer(App.getApplication());
        nodePlayer.setPlayerView(mNodePlayerView);
        nodePlayer.setAudioEnable(false);
        nodePlayer.setVideoEnable(true);
        nodePlayer.setInputUrl(videoUrl);
        nodePlayer.start();

        //显示dialog
        final Dialog dialog = builder.create();
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.show();
        //通过当前的dialog获取window对象
        Window window = dialog.getWindow();
        //设置背景，防止变形
        window.setBackgroundDrawableResource(android.R.color.transparent);

        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = ScreenUtils.getInstance(App.getApplication()).getWidth() - 44;//两边设置的间隙相当于margin
        lp.alpha = 0.9f;
        window.setDimAmount(0.5f);//使用时设置窗口后面的暗淡量
        window.setAttributes(lp);


        //底部关闭按键
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (nodePlayer != null) {
                    nodePlayer.stop();
                }
            }
        });
    }


    /**
     * Handler处理子线程发送过来 的数据
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    //获取报警源信息
                    AlarmBean alarmBen = (AlarmBean) msg.obj;
                    receiveAlarmInfo(alarmBen);
                    break;
                case 2:
                    Bundle dbundle = msg.getData();
                    VideoBean device = (VideoBean) dbundle.getSerializable("device");
                    promptAlarmInfo(device);
                    break;
            }
        }
    };
}
