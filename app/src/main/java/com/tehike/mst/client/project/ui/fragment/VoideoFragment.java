package com.tehike.mst.client.project.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tehike.mst.client.project.R;
import com.tehike.mst.client.project.base.App;
import com.tehike.mst.client.project.base.BaseFragment;
import com.tehike.mst.client.project.cms.SendAlarmToServer;
import com.tehike.mst.client.project.entity.SipBean;
import com.tehike.mst.client.project.entity.VideoBean;
import com.tehike.mst.client.project.global.AppConfig;
import com.tehike.mst.client.project.onvif.ControlPtzUtils;
import com.tehike.mst.client.project.ui.portactivity.PortMainActivity;
import com.tehike.mst.client.project.ui.widget.OnMultiTouchListener;
import com.tehike.mst.client.project.utils.CryptoUtil;
import com.tehike.mst.client.project.utils.FileUtil;
import com.tehike.mst.client.project.utils.GsonUtils;
import com.tehike.mst.client.project.utils.Logutil;
import com.tehike.mst.client.project.utils.PageModel;
import com.tehike.mst.client.project.utils.SharedPreferencesUtils;
import com.tehike.mst.client.project.utils.SnapShotUtils;
import com.tehike.mst.client.project.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.OnClick;
import cn.nodemedia.NodePlayer;
import cn.nodemedia.NodePlayerDelegate;
import cn.nodemedia.NodePlayerView;


/**
 * @author Wpf
 * @version v1.0
 * @date：2016-5-4 下午5:14:58
 */
public class VoideoFragment extends BaseFragment implements NodePlayerDelegate, View.OnTouchListener {

    /**
     * 四分屏按钮
     */
    @BindView(R.id.four_screen_button_select)
    ImageButton four_screen_button_select;

    /**
     * 单屏按钮
     */
    @BindView(R.id.single_screen_button_selecte)
    ImageButton single_screen_button_selecte;

    /**
     * 停止或重新播放按钮
     */
    @BindView(R.id.paly_or_stop_button_select)
    ImageButton paly_or_stop_button_select;

    /**
     * 单屏显示的player布局
     */
    @BindView(R.id.single_player_layout)
    NodePlayerView single_player_layout;

    /**
     * 第一个视频的view
     */
    @BindView(R.id.first_player_layout)
    NodePlayerView firstPlayerView;

    /**
     * 第一个加载进度条
     */
    @BindView(R.id.first_pr_layout)
    ProgressBar first_pr_layout;

    /**
     * 第一个视频 的loading
     */
    @BindView(R.id.first_dispaly_loading_layout)
    TextView first_dispaly_loading_layout;

    /**
     * 第一个视频所在的背景而
     */
    @BindView(R.id.first_surfaceview_relativelayout)
    public RelativeLayout first_surfaceview_relativelayout;

    /**
     * 第二个视频的view
     */
    @BindView(R.id.second_player_layout)
    NodePlayerView secondPlayerView;

    /**
     * 第二个视频所在的背景而
     */
    @BindView(R.id.second_surfaceview_relativelayout)
    public RelativeLayout second_surfaceview_relativelayout;

    /**
     * 第二个加载进度条
     */
    @BindView(R.id.second_pr_layout)
    ProgressBar second_pr_layout;

    /**
     * 第一个视频 的loading
     */
    @BindView(R.id.seond_dispaly_loading_layout)
    TextView second_dispaly_loading_layout;

    /**
     * 第三个视频的view
     */
    @BindView(R.id.third_player_layout)
    NodePlayerView thirdPlayerView;

    /**
     * 第三个视频所在的背景而
     */
    @BindView(R.id.third_surfaceview_relativelayout)
    public RelativeLayout third_surfaceview_relativelayout;

    /**
     * 第三个视频 的progressbar
     */
    @BindView(R.id.third_pr_layout)
    ProgressBar third_pr_layout;

    /**
     * 第三个视频 的loading
     */
    @BindView(R.id.third_dispaly_loading_layout)
    TextView third_dispaly_loading_layout;

    /**
     * 第四个视频的view
     */
    @BindView(R.id.fourth_player_layout)
    NodePlayerView fourthPlayerView;

    /**
     * 第四个视频所在的背景而
     */
    @BindView(R.id.fourth_surfaceview_relativelayout)
    public RelativeLayout fourth_surfaceview_relativelayout;

    /**
     * 第四个progressbar
     */
    @BindView(R.id.fourth_pr_layout)
    ProgressBar fourth_pr_layout;

    /**
     * 返回按钮
     */
    @BindView(R.id.fourth_dispaly_loading_layout)
    TextView fourth_dispaly_loading_layout;

    /**
     * 单屏播放时的progressbar
     */
    @BindView(R.id.single_player_progressbar_layout)
    ProgressBar single_player_progressbar_layout;

    /**
     * 单屏时显示 的Loading
     */
    @BindView(R.id.dispaly_video_loading_layout)
    TextView dispaly_video_loading_layout;

    /**
     * 显示视频信息的Textview
     */
    @BindView(R.id.display_video_information_text_layout)
    TextView display_video_information_text_layout;

    @BindView(R.id.ptz_video_top_left_btn)
    ImageButton ptz_video_top_left_btn;


    @BindView(R.id.ptz_video_top_right_btn)
    ImageButton ptz_video_top_right_btn;


    @BindView(R.id.ptz_video_bottom_left_btn)
    ImageButton ptz_video_bottom_left_btn;

    @BindView(R.id.ptz_video_bottom_right_btn)
    ImageButton ptz_video_bottom_right_btn;

    /**
     * 下键
     */
    @BindView(R.id.ptz_video_up)
    ImageButton video_ptz_up;

    /**
     * 上键
     */
    @BindView(R.id.ptz_video_down)
    ImageButton video_ptz_down;

    /**
     * 左键
     */
    @BindView(R.id.ptz_video_left)
    ImageButton video_ptz_left;

    /**
     * 右键
     */
    @BindView(R.id.ptz_video_right)
    ImageButton video_ptz_right;

    /**
     * 放大按钮
     */
    @BindView(R.id.video_zoomout_button)
    ImageButton video_zoomout_button;

    /**
     * 缩小按钮
     */
    @BindView(R.id.video_zoombig_button)
    ImageButton video_zoombig_button;


    /**
     * 四屏所在 的父布局
     */
    @BindView(R.id.four_surfaceview_parent_relativelayout)
    RelativeLayout four_surfaceview_parent_relativelayout;

    /**
     * 单屏所在的父布局
     */
    @BindView(R.id.single_surfaceview_parent_relativelayout)
    RelativeLayout single_surfaceview_parent_relativelayout;

    /**
     * 截图按键
     */
    @BindView(R.id.screenshots_button_select)
    ImageButton screenshots_button_select;

    /**
     * 截图预览
     */
    @BindView(R.id.preview_layout)
    ImageView preview_layout;

    /**
     * 单屏播放的播放器
     */
    NodePlayer singlePlayer;

    /**
     * 四分屏的四个播放器
     */
    NodePlayer firstPalyer, secondPlayer, thirdPlayer, fourthPlayer;

    /**
     * 要播放视频的数据集合
     */
    List<VideoBean> devicesList = new ArrayList<>();

    /**
     * 当前四屏的数据集合
     */
    List<VideoBean> currentList = new ArrayList<>();

    /**
     * 单屏时要播放的数据
     */
    List<VideoBean> currentSingleList = new ArrayList<>();
    /**
     * ptzURl
     */
    String mPtzUrl = "";

    /**
     * 获取支持ptz的Token
     */
    String mToken = "";

    /**
     * 单屏时画面的对象
     */
    VideoBean currentDevice = null;

    /**
     * 当前对象的位置下标
     */
    int currentDevicePosition = 0;

    /**
     * 当前 是否是四分屏状态
     */
    boolean isCurrentFourScreen = true;

    /**
     * 当前是否是单屏状态
     */
    boolean isCurrentSingleScreen = false;

    /**
     * 单屏分页加载器
     */
    PageModel singlePm;

    /**
     * 四分屏分页加载器
     */
    PageModel pm;

    /**
     * 当前页码
     */
    int videoCurrentPage = 1;

    /**
     * 判断四个视频是否被选中
     */
    boolean firstViewSelect = true;
    boolean secondViewSelect = false;
    boolean thirdViewSelect = false;
    boolean fourthViewSelect = false;

    /**
     * 本页面是否可见
     */
    boolean currentPageIsVisible = false;

    /**
     * 本页面接收预览内容的广播
     */
    VideoDataReceiver mVideoDataReceiver;

    RefreshVideoDataBroadcast broadcast;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_port_fragment_paly_video_layout;
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //初始化view
        initializeView();

        //先判断本地的视频源数据是否存在(报异常)
        try {
            String videoSourceStr = FileUtil.readFile(AppConfig.SOURCES_VIDEO).toString();
            devicesList = GsonUtils.GsonToList(CryptoUtil.decodeBASE64(videoSourceStr), VideoBean.class);
            initializeData();
        } catch (Exception e) {
            //异常后，注册广播监听videoSource数据是否初始化成功
            registerRefreshVideoDataBroadcast();
        }


        //初始化单屏的播放
        currentDevicePosition = 0;
        currentDevice = devicesList.get(currentDevicePosition);
        String playRtspUrl = currentDevice.getRtsp();
        String playVideoName = currentDevice.getName();
        //单屏播放
        if (!TextUtils.isEmpty(playRtspUrl)) {
            initSinglePlayer(playRtspUrl);
        }
        //显示当前播放视频的名称
        if (!TextUtils.isEmpty(playVideoName)) {
            display_video_information_text_layout.setText(playVideoName);
        }

        //播放器单击或双击事件
        videoClickEvent();

        //注册广播接收预览内容
        registerBroadcast();
    }

    /**
     * 广播（用于接收视频字典缓存成功后，适配本页面数据）
     */
    private void registerRefreshVideoDataBroadcast() {
        broadcast = new RefreshVideoDataBroadcast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AppConfig.RESOLVE_VIDEO_DONE_ACTION);
        getActivity().registerReceiver(broadcast, intentFilter);
    }

    class RefreshVideoDataBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                //取出本地缓存的所有的Video数据
                devicesList = GsonUtils.GsonToList(CryptoUtil.decodeBASE64(FileUtil.readFile(AppConfig.SOURCES_VIDEO).toString()), VideoBean.class);
                initializeData();
            } catch (Exception e) {
                Logutil.e("取video字典广播异常---->>>" + e.getMessage());
            }
        }
    }

    /**
     * 注册广播接收要预览的内容
     */
    private void registerBroadcast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("aa");
        mVideoDataReceiver = new VideoDataReceiver();
        getActivity().registerReceiver(mVideoDataReceiver, intentFilter);
    }

    /**
     * 广播接收器
     */
    class VideoDataReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            //用来盛放预览数据的集合
            final List<VideoBean> broadCastDataList = new ArrayList<>();
            //接收到的数据
            List<VideoBean> mlist = (List<VideoBean>) intent.getSerializableExtra("a");
            //生成想要的数据
            if (mlist != null && mlist.size() > 0) {
                if (devicesList != null && devicesList.size() > 0) {
                    for (int i = 0; i < devicesList.size(); i++) {
                        for (int j = 0; j < mlist.size(); j++) {
                            if (devicesList.get(i).getName().equals(mlist.get(j).getName())) {
                                broadCastDataList.add(devicesList.get(i));
                            }
                        }
                    }
                }
                //如果是四分屏
                if (isCurrentFourScreen) {
                    currentList = broadCastDataList;
                    initPlayer();
                }
                //如果单屏就预览第一个视频
                if (isCurrentSingleScreen) {
                    initSinglePlayer(broadCastDataList.get(0).getRtsp());
                    //显示顶部的标题
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (currentPageIsVisible)
                                display_video_information_text_layout.setText(broadCastDataList.get(0).getName());
                        }
                    });
                }
            }
        }
    }

    /**
     * 播放器单击或双击事件
     */
    private void videoClickEvent() {

        firstPlayerView.setOnTouchListener(new OnMultiTouchListener(new OnMultiTouchListener.MultiClickCallback() {
            @Override
            public void onDoubleClick() {
                if (currentList != null) {
                    currentDevice = currentList.get(0);
                    currentDevicePosition = (videoCurrentPage - 1) * 4 + 0;
                    if (!TextUtils.isEmpty(currentDevice.getRtsp()))
                        initSinglePlayer(currentDevice.getRtsp());
                    handler.sendEmptyMessage(23);
                }
            }
        }, new OnMultiTouchListener.ClickCallback() {
            @Override
            public void onClick() {
                //第一个视频的点击事件
                handler.sendEmptyMessage(4);
                currentDevicePosition = (videoCurrentPage - 1) * 4 + 0;
            }
        }));

        secondPlayerView.setOnTouchListener(new OnMultiTouchListener(new OnMultiTouchListener.MultiClickCallback() {
            @Override
            public void onDoubleClick() {
                if (currentList != null) {
                    currentDevice = currentList.get(1);
                    currentDevicePosition = (videoCurrentPage - 1) * 4 + 1;
                    if (!TextUtils.isEmpty(currentDevice.getRtsp()))
                        initSinglePlayer(currentDevice.getRtsp());
                    handler.sendEmptyMessage(23);
                }
            }
        }, new OnMultiTouchListener.ClickCallback() {
            @Override
            public void onClick() {
                handler.sendEmptyMessage(5);
                currentDevicePosition = (videoCurrentPage - 1) * 4 + 1;
            }
        }));


        thirdPlayerView.setOnTouchListener(new OnMultiTouchListener(new OnMultiTouchListener.MultiClickCallback() {
            @Override
            public void onDoubleClick() {
                if (currentList != null) {
                    currentDevice = currentList.get(2);
                    currentDevicePosition = (videoCurrentPage - 1) * 4 + 2;
                    if (!TextUtils.isEmpty(currentDevice.getRtsp()))
                        initSinglePlayer(currentDevice.getRtsp());
                    handler.sendEmptyMessage(23);
                }

            }
        }, new OnMultiTouchListener.ClickCallback() {
            @Override
            public void onClick() {
                handler.sendEmptyMessage(6);
                currentDevicePosition = (videoCurrentPage - 1) * 4 + 2;
            }
        }));


        fourthPlayerView.setOnTouchListener(new OnMultiTouchListener(new OnMultiTouchListener.MultiClickCallback() {
            @Override
            public void onDoubleClick() {
                if (currentList != null) {
                    currentDevice = currentList.get(3);
                    currentDevicePosition = (videoCurrentPage - 1) * 4 + 3;
                    if (!TextUtils.isEmpty(currentDevice.getRtsp()))
                        initSinglePlayer(currentDevice.getRtsp());
                    handler.sendEmptyMessage(23);
                }

            }
        }, new OnMultiTouchListener.ClickCallback() {
            @Override
            public void onClick() {
                handler.sendEmptyMessage(7);
                currentDevicePosition = (videoCurrentPage - 1) * 4 + 3;
            }
        }));


        single_player_layout.setOnTouchListener(new OnMultiTouchListener(new OnMultiTouchListener.MultiClickCallback() {
            @Override
            public void onDoubleClick() {
                handler.sendEmptyMessage(8);
                initPlayer();
            }
        }, null));
    }

    /**
     * 单屏播放rtsp
     */
    private void initSinglePlayer(String rtsp) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                isCurrentFourScreen = false;
                isCurrentSingleScreen = true;
                if (currentPageIsVisible) {
                    dispaly_video_loading_layout.setVisibility(View.VISIBLE);
                    single_player_progressbar_layout.setVisibility(View.VISIBLE);
                    dispaly_video_loading_layout.setText("加载中");
                }

                if (currentDevice != null) {
                    String singleVideoName = currentDevice.getName();
                    if (!TextUtils.isEmpty(singleVideoName))
                        display_video_information_text_layout.setText(singleVideoName);
                }
            }
        });

        if (singlePlayer != null && singlePlayer.isPlaying()) {
            singlePlayer.pause();
            singlePlayer.stop();
        }

        if (firstPalyer != null && firstPalyer.isPlaying()) {
            firstPalyer.pause();
            firstPalyer.stop();
        }
        if (secondPlayer != null && secondPlayer.isPlaying()) {
            secondPlayer.pause();
            secondPlayer.stop();
        }

        if (thirdPlayer != null && thirdPlayer.isPlaying()) {
            thirdPlayer.pause();
            thirdPlayer.stop();
        }

        if (fourthPlayer != null && fourthPlayer.isPlaying()) {
            fourthPlayer.pause();
            fourthPlayer.stop();
        }

        firstPlayerView.setVisibility(View.GONE);
        secondPlayerView.setVisibility(View.GONE);
        thirdPlayerView.setVisibility(View.GONE);
        fourthPlayerView.setVisibility(View.GONE);
        four_surfaceview_parent_relativelayout.setVisibility(View.GONE);
        single_surfaceview_parent_relativelayout.setVisibility(View.VISIBLE);

        single_player_layout.setVisibility(View.VISIBLE);
        singlePlayer = new NodePlayer(getActivity());
        singlePlayer.setPlayerView(single_player_layout);
        if (!TextUtils.isEmpty(rtsp))
            singlePlayer.setInputUrl(rtsp);
        else
            singlePlayer.setInputUrl("sss");
        singlePlayer.setNodePlayerDelegate(this);
        singlePlayer.setAudioEnable(AppConfig.ISVIDEOSOUNDS);
        singlePlayer.setVideoEnable(true);
        singlePlayer.start();
    }

    /**
     * 播放视频数据
     */
    private void initPlayer() {
        if (currentList.size() == 4) {
            handler.sendEmptyMessage(2);
            String rtsp1 = "";
            if (!TextUtils.isEmpty(currentList.get(0).getRtsp())) {
                rtsp1 = currentList.get(0).getRtsp();
            } else {
                rtsp1 = "";
            }
            String rtsp2 = "";
            if (!TextUtils.isEmpty(currentList.get(1).getRtsp())) {
                rtsp2 = currentList.get(1).getRtsp();
            } else {
                rtsp2 = "";
            }
            String rtsp3 = "";
            if (!TextUtils.isEmpty(currentList.get(2).getRtsp())) {
                rtsp3 = currentList.get(2).getRtsp();
            } else {
                rtsp3 = "";
            }
            String rtsp4 = "";
            if (!TextUtils.isEmpty(currentList.get(3).getRtsp())) {
                rtsp4 = currentList.get(3).getRtsp();
            } else {
                rtsp4 = "";
            }


            if (firstPalyer != null && firstPalyer.isPlaying()) {
                firstPalyer.stop();
            }
            if (secondPlayer != null && secondPlayer.isPlaying()) {
                secondPlayer.stop();
            }
            if (thirdPlayer != null && thirdPlayer.isPlaying()) {
                thirdPlayer.stop();
            }
            if (fourthPlayer != null && fourthPlayer.isPlaying()) {
                fourthPlayer.stop();
            }
            firstPalyer.setInputUrl(rtsp1);
            firstPalyer.setNodePlayerDelegate(this);
            firstPalyer.setAudioEnable(AppConfig.ISVIDEOSOUNDS);
            firstPalyer.setVideoEnable(true);
            firstPalyer.setAutoReconnectWaitTimeout(0);
            firstPalyer.setConnectWaitTimeout(0);
            firstPalyer.start();
            Logutil.i("rtsp1" + rtsp1);
            secondPlayer.setInputUrl(rtsp2);
            secondPlayer.setNodePlayerDelegate(this);
            secondPlayer.setAudioEnable(AppConfig.ISVIDEOSOUNDS);
            secondPlayer.setVideoEnable(true);
            secondPlayer.setAutoReconnectWaitTimeout(0);
            secondPlayer.setConnectWaitTimeout(0);
            secondPlayer.start();
            Logutil.i("rtsp2" + rtsp2);
            thirdPlayer.setInputUrl(rtsp3);
            thirdPlayer.setNodePlayerDelegate(this);
            thirdPlayer.setAudioEnable(AppConfig.ISVIDEOSOUNDS);
            thirdPlayer.setVideoEnable(true);
            thirdPlayer.setAutoReconnectWaitTimeout(0);
            thirdPlayer.setConnectWaitTimeout(0);
            thirdPlayer.start();
            Logutil.i("rtsp3" + rtsp3);
            fourthPlayer.setInputUrl(rtsp4);
            fourthPlayer.setNodePlayerDelegate(this);
            fourthPlayer.setAudioEnable(AppConfig.ISVIDEOSOUNDS);
            fourthPlayer.setVideoEnable(true);
            fourthPlayer.setAutoReconnectWaitTimeout(0);
            fourthPlayer.setConnectWaitTimeout(0);
            fourthPlayer.start();
            Logutil.i("rtsp4" + rtsp4);
        }
        if (currentList.size() == 3) {
            handler.sendEmptyMessage(2);

            String rtsp1 = "";
            if (!TextUtils.isEmpty(currentList.get(0).getRtsp())) {
                rtsp1 = currentList.get(0).getRtsp();
            } else {
                rtsp1 = "";
            }
            String rtsp2 = "";
            if (!TextUtils.isEmpty(currentList.get(1).getRtsp())) {
                rtsp2 = currentList.get(1).getRtsp();
            } else {
                rtsp2 = "";
            }
            String rtsp3 = "";
            if (!TextUtils.isEmpty(currentList.get(2).getRtsp())) {
                rtsp3 = currentList.get(2).getRtsp();
            } else {
                rtsp3 = "";
            }

            if (firstPalyer != null && firstPalyer.isPlaying()) {
                firstPalyer.stop();
            }
            if (secondPlayer != null && secondPlayer.isPlaying()) {
                secondPlayer.stop();
            }
            if (thirdPlayer != null && thirdPlayer.isPlaying()) {
                thirdPlayer.stop();
            }
            if (fourthPlayer != null && fourthPlayer.isPlaying()) {
                fourthPlayer.stop();
            }
            firstPalyer.setInputUrl(rtsp1);
            firstPalyer.setNodePlayerDelegate(this);
            firstPalyer.setAudioEnable(AppConfig.ISVIDEOSOUNDS);
            firstPalyer.setVideoEnable(true);
            firstPalyer.start();
            secondPlayer.setInputUrl(rtsp2);
            secondPlayer.setNodePlayerDelegate(this);
            secondPlayer.setAudioEnable(AppConfig.ISVIDEOSOUNDS);
            secondPlayer.setVideoEnable(true);
            secondPlayer.start();
            thirdPlayer.setInputUrl(rtsp3);
            thirdPlayer.setNodePlayerDelegate(this);
            thirdPlayer.setAudioEnable(AppConfig.ISVIDEOSOUNDS);
            thirdPlayer.setVideoEnable(true);
            thirdPlayer.start();
        }

        if (currentList.size() == 2) {
            handler.sendEmptyMessage(2);

            String rtsp1 = "";
            if (!TextUtils.isEmpty(currentList.get(0).getRtsp())) {
                rtsp1 = currentList.get(0).getRtsp();
            } else {
                rtsp1 = "";
            }
            String rtsp2 = "";
            if (!TextUtils.isEmpty(currentList.get(1).getRtsp())) {
                rtsp2 = currentList.get(1).getRtsp();
            } else {
                rtsp2 = "";
            }

            if (firstPalyer != null && firstPalyer.isPlaying()) {
                firstPalyer.stop();
            }
            if (secondPlayer != null && secondPlayer.isPlaying()) {
                secondPlayer.stop();
            }
            if (thirdPlayer != null && thirdPlayer.isPlaying()) {
                thirdPlayer.stop();
            }
            if (fourthPlayer != null && fourthPlayer.isPlaying()) {
                fourthPlayer.stop();
            }
            firstPalyer.setInputUrl(rtsp1);
            firstPalyer.setNodePlayerDelegate(this);
            firstPalyer.setAudioEnable(AppConfig.ISVIDEOSOUNDS);
            firstPalyer.setVideoEnable(true);
            firstPalyer.start();
            secondPlayer.setInputUrl(rtsp2);
            secondPlayer.setNodePlayerDelegate(this);
            secondPlayer.setAudioEnable(AppConfig.ISVIDEOSOUNDS);
            secondPlayer.setVideoEnable(true);
            secondPlayer.start();
        }

        if (currentList.size() == 1) {
            handler.sendEmptyMessage(2);

            String rtsp1 = "";
            if (!TextUtils.isEmpty(currentList.get(0).getRtsp())) {
                rtsp1 = currentList.get(0).getRtsp();
            } else {
                rtsp1 = "";
            }

            if (firstPalyer != null && firstPalyer.isPlaying()) {
                firstPalyer.stop();
            }
            if (secondPlayer != null && secondPlayer.isPlaying()) {
                secondPlayer.stop();
            }
            if (thirdPlayer != null && thirdPlayer.isPlaying()) {
                thirdPlayer.stop();
            }
            if (fourthPlayer != null && fourthPlayer.isPlaying()) {
                fourthPlayer.stop();
            }
            firstPalyer.setInputUrl(rtsp1);
            firstPalyer.setNodePlayerDelegate(this);
            firstPalyer.setAudioEnable(AppConfig.ISVIDEOSOUNDS);
            firstPalyer.setVideoEnable(true);
            firstPalyer.start();
        }
    }

    /**
     * 初始化本页面的数据
     */
    private void initializeData() {


        if (devicesList == null || devicesList.size() == 0) {
            return;
        }

        //初始化四屏播放数据
        pm = new PageModel(devicesList, 4);
        currentList = pm.getObjects(videoCurrentPage);

        //初始页面单屏的数据
        singlePm = new PageModel(devicesList, 1);
        currentSingleList = singlePm.getObjects(videoCurrentPage);

    }

    /**
     * 初始化本页面的View
     */
    private void initializeView() {
        //方向键和放大缩小键的Touch事件
        video_ptz_up.setOnTouchListener(this);
        video_ptz_down.setOnTouchListener(this);
        video_ptz_left.setOnTouchListener(this);
        video_ptz_right.setOnTouchListener(this);
        video_zoomout_button.setOnTouchListener(this);
        video_zoombig_button.setOnTouchListener(this);

        //左上
        ptz_video_top_left_btn.setOnTouchListener(this);
        //右上
        ptz_video_top_right_btn.setOnTouchListener(this);
        //左下
        ptz_video_bottom_left_btn.setOnTouchListener(this);
        //右下
        ptz_video_bottom_right_btn.setOnTouchListener(this);


        //实现四个播放器
        firstPalyer = new NodePlayer(getActivity());

        //设置要播放的view
        firstPalyer.setPlayerView(firstPlayerView);

        //设置连接等待超时时长
        firstPalyer.setConnectWaitTimeout(3 * 1000);

        //设置不自动 重连
        firstPalyer.setAutoReconnectWaitTimeout(0);

        secondPlayer = new NodePlayer(getActivity());
        secondPlayer.setPlayerView(secondPlayerView);
        secondPlayer.setConnectWaitTimeout(3 * 1000);
        secondPlayer.setAutoReconnectWaitTimeout(0);

        thirdPlayer = new NodePlayer(getActivity());
        thirdPlayer.setPlayerView(thirdPlayerView);
        thirdPlayer.setConnectWaitTimeout(3 * 1000);
        thirdPlayer.setAutoReconnectWaitTimeout(0);

        fourthPlayer = new NodePlayer(getActivity());
        fourthPlayer.setPlayerView(fourthPlayerView);
        fourthPlayer.setConnectWaitTimeout(3 * 1000);
        fourthPlayer.setAutoReconnectWaitTimeout(0);

        //第一个播放器默认是选中状态

        single_screen_button_selecte.setBackgroundResource(R.mipmap.port_btn_single_selected);
    }


    /**
     * 显示加载进度条和文字
     */
    private void displayLoadingAndPr() {
        if (currentPageIsVisible) {
            first_pr_layout.setVisibility(View.VISIBLE);
            first_dispaly_loading_layout.setVisibility(View.VISIBLE);
            first_dispaly_loading_layout.setText("加载中...");

            second_pr_layout.setVisibility(View.VISIBLE);
            second_dispaly_loading_layout.setVisibility(View.VISIBLE);
            second_dispaly_loading_layout.setText("加载中...");

            third_pr_layout.setVisibility(View.VISIBLE);
            third_dispaly_loading_layout.setVisibility(View.VISIBLE);
            third_dispaly_loading_layout.setText("加载中...");

            fourth_pr_layout.setVisibility(View.VISIBLE);
            fourth_dispaly_loading_layout.setVisibility(View.VISIBLE);
            fourth_dispaly_loading_layout.setText("加载中...");
        }

    }

    /**
     * 截图
     */
    @OnClick(R.id.screenshots_button_select)
    public void screenShotPic(View view) {
        Logutil.i(currentDevicePosition + "当前下标");

        //当前播放器播放的对象
        VideoBean mDevice = devicesList.get(currentDevicePosition);

        //判断当前选中的对象
        if (mDevice != null) {
            //判断对象中是有截图的url
            String shotPicUrl = mDevice.getShotPicUrl();
            if (TextUtils.isEmpty(shotPicUrl)) {
                handler.sendEmptyMessage(9);
                Logutil.e("未获取到截图URl");
                return;
            }
            //启动线程去截图并保存到本顾
            SnapShotUtils screenShot = new SnapShotUtils(shotPicUrl, new SnapShotUtils.CallbackBitmap() {
                @Override
                public void getBitMap(final Bitmap bitmap) {
                    Message message = new Message();
                    message.obj = bitmap;
                    message.what = 10;
                    handler.sendMessage(message);
                }

                @Override
                public void fail(String fail) {
                    handler.sendEmptyMessage(9);
                }
            });
            screenShot.start();
        }
    }

    /**
     * 显示截图图片
     */
    private void displayShotPic(Message msg) {
        Bitmap bitmap = (Bitmap) msg.obj;
        preview_layout.setVisibility(View.VISIBLE);
        preview_layout.setImageBitmap(bitmap);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (currentPageIsVisible)
                            preview_layout.setVisibility(View.GONE);
                    }
                });
            }
        };
        Timer timer = new Timer();
        timer.schedule(timerTask, 2000);
    }

    /**
     * 下一页
     */
    @OnClick(R.id.video_nextpage_button)
    public void videoNextPage(View view) {
        //四分屏时
        if (isCurrentFourScreen) {
            if (pm != null && pm.isHasNextPage()) {
                videoCurrentPage++;
                Logutil.i("当前页面:" + videoCurrentPage);
                currentList = pm.getObjects(videoCurrentPage);
                Logutil.i(currentList.size() + "");
                for (VideoBean device : currentList) {
                    Logutil.i(device.toString() + "");
                }
                initPlayer();
                mPtzUrl = "";
                mToken = "";
            } else {
                Logutil.i("没有下一页了");
            }
        }
        //单屏时
        if (isCurrentSingleScreen) {

            if (currentDevicePosition > devicesList.size()) {
                return;
            }

            if (currentDevicePosition == devicesList.size() - 1) {
                currentDevicePosition = devicesList.size() - 1;
                return;
            }
            Logutil.i("单屏下一页" + currentDevicePosition);
            currentDevicePosition += 1;
            Logutil.i(currentDevicePosition + "....");
            currentDevice = devicesList.get(currentDevicePosition);
            initSinglePlayer(currentDevice.getRtsp());
            mPtzUrl = "";
            mToken = "";
        }
    }

    /**
     * 上一页
     */
    @OnClick(R.id.video_previous_button)
    public void videoPreviousPage(View view) {

        if (isCurrentFourScreen) {
            if (pm != null && pm.isHasPreviousPage()) {
                videoCurrentPage--;
                Logutil.i("当前页面:" + videoCurrentPage);
                currentList = pm.getObjects(videoCurrentPage);
                Logutil.i(currentList.size() + "");
                for (VideoBean device : currentList) {
                    Logutil.i(device.toString() + "");
                }
                initPlayer();
                mPtzUrl = "";
                mToken = "";
            } else {
                videoCurrentPage = 0;
            }
        }

        //单屏时
        if (isCurrentSingleScreen) {
            if (currentDevicePosition == 0) {
                currentDevicePosition = 0;
                return;
            }
            currentDevicePosition -= 1;
            currentDevice = devicesList.get(currentDevicePosition);
            initSinglePlayer(currentDevice.getRtsp());
            mPtzUrl = "";
            mToken = "";
        }
    }

    /**
     * 显示预览数据
     *
     * @param view
     */
    @OnClick(R.id.loading_more_videosources_layout)
    public void loadMoreVideoResources(View view) {

        showLoadMoreVideoDialog();
    }

    /**
     * 跳转页面预览
     */
    private void showLoadMoreVideoDialog() {

//        Intent intent = new Intent();
//        intent.setClass(getActivity(), PortVideoResourcesActivity.class);
//        getActivity().startActivity(intent);
//
    }

    /**
     * 单屏播放rtsp
     */
    @OnClick(R.id.single_screen_button_selecte)
    public void singleScreenVideo(View view) {
        //单屏按钮选中
        single_screen_button_selecte.setBackgroundResource(R.mipmap.port_btn_single_selected);
        //四屏按钮未选中
        four_screen_button_select.setBackgroundResource(R.mipmap.port_monitoring_btn_4splitscreen_normal);
        //四屏状态的标识
        isCurrentFourScreen = false;
        //单屏状态的标识
        isCurrentSingleScreen = true;
        //第一个窗口选中
        if (firstViewSelect) {
            String rtsp = "";
            if (!TextUtils.isEmpty(currentList.get(0).getRtsp())) {
                rtsp = currentList.get(0).getRtsp();
            }
            initSinglePlayer(rtsp);
        }

        //第二个窗口选中
        if (secondViewSelect) {
            String rtsp = "";
            if (!TextUtils.isEmpty(currentList.get(1).getRtsp())) {
                rtsp = currentList.get(1).getRtsp();
            }
            initSinglePlayer(rtsp);
        }

        //第三个窗口选中
        if (thirdViewSelect) {
            String rtsp = "";
            if (!TextUtils.isEmpty(currentList.get(2).getRtsp())) {
                rtsp = currentList.get(2).getRtsp();
            }
            initSinglePlayer(rtsp);
        }


        if (fourthViewSelect) {
            String rtsp = "";
            if (!TextUtils.isEmpty(currentList.get(3).getRtsp())) {
                rtsp = currentList.get(3).getRtsp();
            }
            initSinglePlayer(rtsp);
        }
    }

    /**
     * 四屏播放rtsp
     */
    @OnClick(R.id.four_screen_button_select)
    public void fourScreenVideo(View view) {
        //单屏不可见的标识
        isCurrentSingleScreen = false;
        //四屏可见标识
        isCurrentFourScreen = true;
        //停止单屏播放
        if (singlePlayer != null && singlePlayer.isPlaying()) {
            singlePlayer.pause();
            singlePlayer.stop();
        }
        //当前页面是否可见
        if (currentPageIsVisible) {
            //单屏按键变为normal
            single_screen_button_selecte.setBackgroundResource(R.mipmap.port_btn_single_normal);
            //四屏选中
            four_screen_button_select.setBackgroundResource(R.mipmap.port_monitoring_btn_4splitscreen_selected);
            //四屏布局可见
            four_surfaceview_parent_relativelayout.setVisibility(View.VISIBLE);
            //单屏而已不可见
            single_surfaceview_parent_relativelayout.setVisibility(View.GONE);
            single_player_layout.setVisibility(View.GONE);
            //四个播放器的view可见
            firstPlayerView.setVisibility(View.VISIBLE);
            secondPlayerView.setVisibility(View.VISIBLE);
            thirdPlayerView.setVisibility(View.VISIBLE);
            fourthPlayerView.setVisibility(View.VISIBLE);
            //第一个播放器默认是选中状态
            first_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_select_shape);
            second_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_shape);
            third_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_shape);
            fourth_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_shape);
        }
        //四个播放器开始播放
        initPlayer();

        //显示顶部的标题
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (currentPageIsVisible)
                    display_video_information_text_layout.setText("四分屏监控画面");
            }
        });
    }

    /**
     * 点击返回按键
     */
    @OnClick(R.id.finish_video_fragment_layout)
    public void finishPage(View view) {
        getActivity().finish();
    }

    boolean firstPlayerIsStop = false;

    /**
     * 播放器停止
     */
    @OnClick(R.id.paly_or_stop_button_select)
    public void stopOrRestartVideo(View view) {
        if (isCurrentFourScreen) {
            if (firstViewSelect) {
                if (firstPalyer != null) {
                    if (!firstPlayerIsStop) {
                        firstPalyer.stop();
                        firstPlayerIsStop = true;
                        if (getActivity() != null)
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    paly_or_stop_button_select.setBackgroundResource(R.mipmap.port_monitoring_icon_player_selected);
                                }
                            });
                    } else {
                        firstPlayerIsStop = false;
                        firstPalyer.start();
                        if (getActivity() != null)
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    paly_or_stop_button_select.setBackgroundResource(R.mipmap.port_monitoring_icon_stopplay_normal);
                                }
                            });
                    }

                }
            } else if (secondViewSelect) {
                if (secondPlayer != null) {
                    if (!firstPlayerIsStop) {
                        secondPlayer.stop();
                        firstPlayerIsStop = true;
                        if (getActivity() != null)
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    paly_or_stop_button_select.setBackgroundResource(R.mipmap.port_monitoring_icon_player_selected);
                                }
                            });
                    } else {
                        firstPlayerIsStop = false;
                        secondPlayer.start();
                        if (getActivity() != null)
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    paly_or_stop_button_select.setBackgroundResource(R.mipmap.port_monitoring_icon_stopplay_normal);
                                }
                            });
                    }

                }
            } else if (thirdViewSelect) {
                if (thirdPlayer != null) {
                    if (!firstPlayerIsStop) {
                        thirdPlayer.stop();
                        firstPlayerIsStop = true;
                        if (getActivity() != null)
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    paly_or_stop_button_select.setBackgroundResource(R.mipmap.port_monitoring_icon_player_selected);
                                }
                            });
                    } else {
                        firstPlayerIsStop = false;
                        thirdPlayer.start();
                        if (getActivity() != null)
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    paly_or_stop_button_select.setBackgroundResource(R.mipmap.port_monitoring_icon_stopplay_normal);
                                }
                            });
                    }

                }
            } else if (fourthViewSelect) {
                if (fourthPlayer != null) {
                    if (!firstPlayerIsStop) {
                        fourthPlayer.stop();
                        firstPlayerIsStop = true;
                        if (getActivity() != null)
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    paly_or_stop_button_select.setBackgroundResource(R.mipmap.port_monitoring_icon_player_selected);
                                }
                            });
                    } else {
                        firstPlayerIsStop = false;
                        fourthPlayer.start();
                        if (getActivity() != null)
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    paly_or_stop_button_select.setBackgroundResource(R.mipmap.port_monitoring_icon_stopplay_normal);
                                }
                            });
                    }

                }
            } else {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtils.showShort("未选中窗口！！！");
                    }
                });
            }
        } else if (isCurrentSingleScreen) {
            if (singlePlayer != null) {
                if (!firstPlayerIsStop) {
                    singlePlayer.stop();
                    firstPlayerIsStop = true;
                    if (getActivity() != null)
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                paly_or_stop_button_select.setBackgroundResource(R.mipmap.port_monitoring_icon_player_selected);
                            }
                        });
                } else {
                    firstPlayerIsStop = false;
                    singlePlayer.start();
                    if (getActivity() != null)
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                paly_or_stop_button_select.setBackgroundResource(R.mipmap.port_monitoring_icon_stopplay_normal);
                            }
                        });
                }
            }
        }
    }


    /**
     * 向服务器发送报警
     */
    @OnClick(R.id.send_alarmtoServer_button)
    public void sendAlarmToServer(View view) {
        //获取选中的视频源对象
        VideoBean mVideoBean = devicesList.get(currentDevicePosition);
        //判断是否为空
        if (mVideoBean == null) {
            return;
        }
        //子线程发送报警信息到服务器
        SendAlarmToServer sendAlarmToServer = new SendAlarmToServer(mVideoBean, new SendAlarmToServer.Callback() {
            @Override
            public void getCallbackData(final String result) {
                Message message = new Message();
                message.what = 21;
                message.obj = result;
                handler.sendMessage(message);
            }
        });
        sendAlarmToServer.start();
    }

    /**
     * 获取设备是否支持云台控制
     */
    public void getPtzParamaters() {
        if (currentList != null && currentList.size() > 0) {
            for (VideoBean d : currentList) {
                String rtsp = d.getPtzUrl();
                if (!TextUtils.isEmpty(rtsp)) {
                    mPtzUrl = rtsp;
                    String token = d.getProfiles().get(0).getToken();
                    if (!TextUtils.isEmpty(token)) {
                        mToken = token;
                    }
                }
            }
        }
        if (currentDevice != null) {
            String rtsp = currentDevice.getPtzUrl();
            if (!TextUtils.isEmpty(rtsp)) {
                mPtzUrl = rtsp;
                String token = currentDevice.getProfiles().get(0).getToken();
                if (!TextUtils.isEmpty(token)) {
                    mToken = token;
                }
            }
        }
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.ptz_video_top_left_btn:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    getPtzParamaters();
                    if (!TextUtils.isEmpty(mPtzUrl) && !TextUtils.isEmpty(mToken)) {
                        ControlPtzUtils controlPtz = new ControlPtzUtils(mPtzUrl, mToken, "top", -0.1, 0.05);
                        controlPtz.start();
                    } else {
                        handler.sendEmptyMessage(22);
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    ControlPtzUtils controlPtz = new ControlPtzUtils(mPtzUrl, mToken, "stop", 0.00, 0.00);
                    controlPtz.start();
                }
                break;
            case R.id.ptz_video_top_right_btn:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    getPtzParamaters();
                    if (!TextUtils.isEmpty(mPtzUrl) && !TextUtils.isEmpty(mToken)) {
                        ControlPtzUtils controlPtz = new ControlPtzUtils(mPtzUrl, mToken, "top", 0.1, 0.05);
                        controlPtz.start();

                    } else {
                        handler.sendEmptyMessage(22);
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    ControlPtzUtils controlPtz = new ControlPtzUtils(mPtzUrl, mToken, "stop", 0.00, 0.00);
                    controlPtz.start();
                }
                break;

            case R.id.ptz_video_bottom_left_btn:

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    getPtzParamaters();
                    if (!TextUtils.isEmpty(mPtzUrl) && !TextUtils.isEmpty(mToken)) {
                        ControlPtzUtils controlPtz = new ControlPtzUtils(mPtzUrl, mToken, "top", -0.1, -0.05);
                        controlPtz.start();
                    } else {
                        handler.sendEmptyMessage(22);
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    ControlPtzUtils controlPtz = new ControlPtzUtils(mPtzUrl, mToken, "stop", 0.00, 0.00);
                    controlPtz.start();
                }
                break;
            case R.id.ptz_video_bottom_right_btn:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    getPtzParamaters();
                    if (!TextUtils.isEmpty(mPtzUrl) && !TextUtils.isEmpty(mToken)) {
                        ControlPtzUtils controlPtz = new ControlPtzUtils(mPtzUrl, mToken, "top", -0.1, -0.05);
                        controlPtz.start();
                    } else {
                        handler.sendEmptyMessage(22);
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    ControlPtzUtils controlPtz = new ControlPtzUtils(mPtzUrl, mToken, "stop", 0.00, 0.00);
                    controlPtz.start();
                }
                break;
            case R.id.ptz_video_down:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    getPtzParamaters();
                    if (!TextUtils.isEmpty(mPtzUrl) && !TextUtils.isEmpty(mToken)) {
                        ControlPtzUtils controlPtz = new ControlPtzUtils(mPtzUrl, mToken, "top", 0.00, 0.03);
                        controlPtz.start();
                    } else {
                        handler.sendEmptyMessage(22);
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    ControlPtzUtils controlPtz = new ControlPtzUtils(mPtzUrl, mToken, "stop", 0.00, 0.00);
                    controlPtz.start();
                }

                break;

            case R.id.ptz_video_up:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    getPtzParamaters();
                    if (!TextUtils.isEmpty(mPtzUrl) && !TextUtils.isEmpty(mToken)) {
                        ControlPtzUtils controlPtz = new ControlPtzUtils(mPtzUrl, mToken, "below", 0.00, -0.03);
                        controlPtz.start();
                    } else {
                        handler.sendEmptyMessage(22);
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    ControlPtzUtils controlPtz = new ControlPtzUtils(mPtzUrl, mToken, "stop", 0.00, 0.00);
                    controlPtz.start();
                }
                break;

            case R.id.ptz_video_left:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    getPtzParamaters();
                    if (!TextUtils.isEmpty(mPtzUrl) && !TextUtils.isEmpty(mToken)) {
                        ControlPtzUtils controlPtz = new ControlPtzUtils(mPtzUrl, mToken, "right", -0.03, 0.00);
                        controlPtz.start();
                    } else {
                        handler.sendEmptyMessage(22);
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    ControlPtzUtils controlPtz = new ControlPtzUtils(mPtzUrl, mToken, "stop", 0.00, 0.00);
                    controlPtz.start();
                }
                break;

            case R.id.ptz_video_right:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    getPtzParamaters();
                    if (!TextUtils.isEmpty(mPtzUrl) && !TextUtils.isEmpty(mToken)) {
                        ControlPtzUtils controlPtz = new ControlPtzUtils(mPtzUrl, mToken, "left", 0.03, 0.00);
                        controlPtz.start();
                    } else {
                        handler.sendEmptyMessage(22);
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    ControlPtzUtils controlPtz = new ControlPtzUtils(mPtzUrl, mToken, "stop", 0.00, 0.00);
                    controlPtz.start();
                }
                break;

            case R.id.video_zoomout_button:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    getPtzParamaters();
                    if (!TextUtils.isEmpty(mPtzUrl) && !TextUtils.isEmpty(mToken)) {
                        ControlPtzUtils controlPtz = new ControlPtzUtils(mPtzUrl, mToken, "zoom_b", -0.3, -0.03);
                        controlPtz.start();
                    } else {
                        handler.sendEmptyMessage(22);
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    ControlPtzUtils controlPtz = new ControlPtzUtils(mPtzUrl, mToken, "stop", 0.00, 0.00);
                    controlPtz.start();
                }
                break;

            case R.id.video_zoombig_button:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    getPtzParamaters();
                    if (!TextUtils.isEmpty(mPtzUrl) && !TextUtils.isEmpty(mToken)) {
                        ControlPtzUtils controlPtz = new ControlPtzUtils(mPtzUrl, mToken, "zoom_s", 0.3, 0.03);
                        controlPtz.start();
                    } else {
                        handler.sendEmptyMessage(22);
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    ControlPtzUtils controlPtz = new ControlPtzUtils(mPtzUrl, mToken, "stop", 0.00, 0.00);
                    controlPtz.start();
                }
                break;
        }
        return false;
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        currentPageIsVisible = isVisibleToUser;
        Logutil.i("Video页面是否可见:" + isVisibleToUser);
        super.setUserVisibleHint(isVisibleToUser);
    }


    @Override
    public void onEventCallback(NodePlayer player, int event, String msg) {

        if (getActivity() != null) {
            if (firstPalyer == player) {
                if (event == 1001) {
                    handler.sendEmptyMessage(11);
                } else if (event == 1003) {
                    handler.sendEmptyMessage(12);
                }
            }
            if (secondPlayer == player) {
                if (event == 1001) {
                    handler.sendEmptyMessage(13);
                } else if (event == 1003) {
                    handler.sendEmptyMessage(14);
                }
            }
            if (thirdPlayer == player) {
                if (event == 1001) {
                    handler.sendEmptyMessage(15);
                } else if (event == 1003) {
                    handler.sendEmptyMessage(16);
                }
            }
            if (fourthPlayer == player) {
                if (event == 1001) {
                    handler.sendEmptyMessage(17);
                } else if (event == 1003) {
                    handler.sendEmptyMessage(18);
                }
            }
            if (singlePlayer == player) {
                if (event == 1001) {
                    handler.sendEmptyMessage(19);
                } else if (event == 1003) {
                    handler.sendEmptyMessage(20);
                }
            }
        }
    }


    /**
     * 结束 本页面
     */
    private void finishPage() {
        if (firstPalyer != null) {
            firstPalyer.release();
            firstPalyer = null;
        }
        if (secondPlayer != null) {
            secondPlayer.release();
            secondPlayer = null;
        }

        if (thirdPlayer != null) {
            thirdPlayer.release();
            thirdPlayer = null;
        }

        if (fourthPlayer != null) {
            fourthPlayer.release();
            fourthPlayer = null;
        }

        if (singlePlayer != null) {
            singlePlayer.release();
            singlePlayer = null;
        }


        if (first_pr_layout != null) {
            first_pr_layout.setVisibility(View.GONE);
            first_pr_layout = null;
        }
        if (second_pr_layout != null) {
            second_pr_layout.setVisibility(View.GONE);
            second_pr_layout = null;
        }
        if (third_pr_layout != null) {
            third_pr_layout.setVisibility(View.GONE);
            third_pr_layout = null;
        }
        if (fourth_pr_layout != null) {
            fourth_pr_layout.setVisibility(View.GONE);
            fourth_pr_layout = null;
        }
        if (getActivity() != null)
            if (single_player_progressbar_layout != null) {
                single_player_progressbar_layout.setVisibility(View.GONE);
                single_player_progressbar_layout = null;
            }

        if (getActivity() != null)
            if (currentPageIsVisible) {
                if (firstPalyer != null) {
                    first_pr_layout.setVisibility(View.GONE);
                    first_dispaly_loading_layout.setText(View.GONE);
                    second_pr_layout.setVisibility(View.GONE);
                    second_dispaly_loading_layout.setText(View.GONE);
                    third_pr_layout.setVisibility(View.GONE);
                    third_dispaly_loading_layout.setText(View.GONE);
                    fourth_pr_layout.setVisibility(View.GONE);
                    fourth_dispaly_loading_layout.setText(View.GONE);
                    single_player_progressbar_layout.setVisibility(View.GONE);
                }
            }
        if (handler != null)
            handler.removeCallbacksAndMessages(null);
    }


    @Override
    public void onDestroyView() {
        finishPage();
        if (mVideoDataReceiver != null)
            getActivity().unregisterReceiver(mVideoDataReceiver);
        super.onDestroyView();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case 1://提示未加载到数据
                    if (currentPageIsVisible)
                        showProgressFail("未加载到数据！");
                    break;
                case 2://提示加载进度条和progressbar
                    displayLoadingAndPr();
                    break;
                case 4://第一个播放器选中状态
                    if (currentPageIsVisible) {
                        first_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_select_shape);
                        second_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_shape);
                        third_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_shape);
                        fourth_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_shape);
                    }
                    firstViewSelect = true;
                    secondViewSelect = false;
                    thirdViewSelect = false;
                    fourthViewSelect = false;
                    break;
                case 5://第二个播放器选中状态
                    if (currentPageIsVisible) {
                        first_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_shape);
                        second_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_select_shape);
                        third_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_shape);
                        fourth_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_shape);
                    }
                    firstViewSelect = false;
                    secondViewSelect = true;
                    thirdViewSelect = false;
                    fourthViewSelect = false;
                    break;
                case 6://第三个播放器选中状态
                    if (currentPageIsVisible) {
                        first_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_shape);
                        second_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_shape);
                        third_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_select_shape);
                        fourth_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_shape);
                    }
                    firstViewSelect = false;
                    secondViewSelect = false;
                    thirdViewSelect = true;
                    fourthViewSelect = false;

                    break;
                case 7://第四个播放器选中状态
                    if (currentPageIsVisible) {
                        first_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_shape);
                        second_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_shape);
                        third_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_shape);
                        fourth_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_select_shape);
                    }
                    firstViewSelect = false;
                    secondViewSelect = false;
                    thirdViewSelect = false;
                    fourthViewSelect = true;
                    break;

                case 8://单屏双击事件
                    //单屏不可见的标识
                    isCurrentSingleScreen = false;
                    //四屏可见标识
                    isCurrentFourScreen = true;
                    //停止单屏播放
                    if (singlePlayer != null && singlePlayer.isPlaying()) {
                        singlePlayer.pause();
                        singlePlayer.stop();
                    }
                    //当前页面是否可见
                    if (currentPageIsVisible) {
                        //单屏按键变为normal
                        single_screen_button_selecte.setBackgroundResource(R.mipmap.port_btn_single_normal);
                        //四屏选中
                        four_screen_button_select.setBackgroundResource(R.mipmap.port_monitoring_btn_4splitscreen_selected);
                        //四屏布局可见
                        four_surfaceview_parent_relativelayout.setVisibility(View.VISIBLE);
                        //单屏而已不可见
                        single_surfaceview_parent_relativelayout.setVisibility(View.GONE);
                        single_player_layout.setVisibility(View.GONE);
                        //四个播放器的view可见
                        firstPlayerView.setVisibility(View.VISIBLE);
                        secondPlayerView.setVisibility(View.VISIBLE);
                        thirdPlayerView.setVisibility(View.VISIBLE);
                        fourthPlayerView.setVisibility(View.VISIBLE);
                        //第一个播放器默认是选中状态
                        first_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_select_shape);
                        second_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_shape);
                        third_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_shape);
                        fourth_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_shape);
                    }
                    //四个播放器开始播放
                    initPlayer();

                    //显示顶部的标题
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (currentPageIsVisible)
                                display_video_information_text_layout.setText("四分屏监控画面");
                        }
                    });
                    break;
                case 9://提示截图失败
                    if (currentPageIsVisible)
                        showProgressFail("截图失败");
                    break;
                case 10://显示截图聊天图片
                    if (currentPageIsVisible)
                        displayShotPic(msg);

                    break;
                case 11:
                    if (currentPageIsVisible) {
                        first_pr_layout.setVisibility(View.GONE);
                        first_dispaly_loading_layout.setVisibility(View.GONE);
                    }
                    break;

                case 12:
                    if (currentPageIsVisible) {
                        first_dispaly_loading_layout.setVisibility(View.VISIBLE);
                        first_pr_layout.setVisibility(View.GONE);
                        first_dispaly_loading_layout.setText("重新连接...");
                    }
                    break;

                case 13:
                    if (currentPageIsVisible) {
                        second_pr_layout.setVisibility(View.GONE);
                        second_dispaly_loading_layout.setVisibility(View.GONE);
                    }
                    break;
                case 14:
                    if (currentPageIsVisible) {
                        second_dispaly_loading_layout.setVisibility(View.VISIBLE);
                        second_pr_layout.setVisibility(View.GONE);
                        second_dispaly_loading_layout.setText("重新连接...");
                    }
                    break;

                case 15:
                    if (currentPageIsVisible) {
                        third_pr_layout.setVisibility(View.GONE);
                        third_dispaly_loading_layout.setVisibility(View.GONE);
                    }
                    break;
                case 16:
                    if (currentPageIsVisible) {
                        third_dispaly_loading_layout.setVisibility(View.VISIBLE);
                        third_pr_layout.setVisibility(View.GONE);
                        third_dispaly_loading_layout.setText("重新连接...");
                    }
                    break;

                case 17:
                    if (currentPageIsVisible) {
                        fourth_pr_layout.setVisibility(View.GONE);
                        fourth_dispaly_loading_layout.setVisibility(View.GONE);
                    }
                    break;
                case 18:
                    if (currentPageIsVisible) {
                        fourth_dispaly_loading_layout.setVisibility(View.VISIBLE);
                        fourth_pr_layout.setVisibility(View.GONE);
                        fourth_dispaly_loading_layout.setText("重新连接...");
                    }
                    break;

                case 19:
                    if (currentPageIsVisible) {
                        single_player_progressbar_layout.setVisibility(View.GONE);
                        dispaly_video_loading_layout.setVisibility(View.GONE);
                    }
                    break;
                case 20:
                    if (currentPageIsVisible) {
                        dispaly_video_loading_layout.setVisibility(View.VISIBLE);
                        single_player_progressbar_layout.setVisibility(View.GONE);
                        dispaly_video_loading_layout.setText("重新连接...");
                    }
                    break;
                case 21://提示报警信息
                    String result = (String) msg.obj;
                    if (!TextUtils.isEmpty(result) && currentPageIsVisible)
                        showProgressSuccess(result);
                    break;

                case 22://不支持ptz功能
                    if (currentPageIsVisible)
                        ToastUtils.showShort("不支持云台功能!");
                    break;
                case 23:
                    single_screen_button_selecte.setBackgroundResource(R.mipmap.port_btn_single_selected);
                    //四屏选中
                    four_screen_button_select.setBackgroundResource(R.mipmap.port_monitoring_btn_4splitscreen_normal);
                    break;
            }
        }
    };
}
