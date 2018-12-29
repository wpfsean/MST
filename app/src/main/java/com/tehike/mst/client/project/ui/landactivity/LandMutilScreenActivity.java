package com.tehike.mst.client.project.ui.landactivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tehike.mst.client.project.R;
import com.tehike.mst.client.project.adapters.VideoResourcesListAda;
import com.tehike.mst.client.project.base.App;
import com.tehike.mst.client.project.base.BaseActivity;
import com.tehike.mst.client.project.cms.SendEmergencyAlarmToServerThrad;
import com.tehike.mst.client.project.entity.VideoBean;
import com.tehike.mst.client.project.global.AppConfig;
import com.tehike.mst.client.project.linphone.MessageCallback;
import com.tehike.mst.client.project.linphone.SipService;
import com.tehike.mst.client.project.onvif.ControlPtzUtils;
import com.tehike.mst.client.project.services.BatteryAndWifiService;
import com.tehike.mst.client.project.ui.widget.OnMultiTouchListener;
import com.tehike.mst.client.project.utils.BatteryUtils;
import com.tehike.mst.client.project.utils.CryptoUtil;
import com.tehike.mst.client.project.utils.FileUtil;
import com.tehike.mst.client.project.utils.GsonUtils;
import com.tehike.mst.client.project.utils.Logutil;
import com.tehike.mst.client.project.utils.PageModel;
import com.tehike.mst.client.project.utils.SharedPreferencesUtils;
import com.tehike.mst.client.project.utils.SnapShotUtils;
import com.tehike.mst.client.project.utils.ToastUtils;

import org.linphone.core.LinphoneChatMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.OnClick;
import cn.nodemedia.NodePlayer;
import cn.nodemedia.NodePlayerDelegate;
import cn.nodemedia.NodePlayerView;

/**
 * 描述：横屏的视频播放界面
 * <p>
 * 思路 ：
 * 取出本地可在的videoResources资源，转成list集合，
 * 上下页翻页是通过截取list实现
 * <p>
 * 使nodemedia播放视频
 * <p>
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2018/11/13 14:06
 */

public class LandMutilScreenActivity extends BaseActivity implements NodePlayerDelegate, View.OnClickListener, View.OnTouchListener {


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
     * 电量图标
     */
    @BindView(R.id.icon_electritity_show)
    ImageView batteryIcon;


    /**
     * 网络图标
     */
    @BindView(R.id.icon_network)
    ImageView networkIcon;

    /**
     * sip状态图标
     */
    @BindView(R.id.icon_connection_show)
    ImageView connetIcon;

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
     * 第一个视频 的Progressbar
     */
    @BindView(R.id.first_pr_layout)
    ProgressBar first_pr_layout;

    /**
     * 第一个视频 的loading
     */
    @BindView(R.id.first_dispaly_loading_layout)
    TextView first_dispaly_loading_layout;

    /**
     * 第一个视频所在的背景
     */
    @BindView(R.id.first_surfaceview_relativelayout)
    public RelativeLayout first_surfaceview_relativelayout;

    /**
     * 第二个视频的view
     */
    @BindView(R.id.second_player_layout)
    NodePlayerView secondPlayerView;

    /**
     * 第二个视频所在的背景
     */
    @BindView(R.id.second_surfaceview_relativelayout)
    public RelativeLayout second_surfaceview_relativelayout;

    /**
     * 第二个视频 的Progressbar
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
     * 第一个视频 的loading
     */
    @BindView(R.id.fourth_player_layout)
    NodePlayerView fourthPlayerView;

    /**
     * 第四个视频所在的背景
     */
    @BindView(R.id.fourth_surfaceview_relativelayout)
    public RelativeLayout fourth_surfaceview_relativelayout;

    /**
     * 每四个progressbar
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

    //上下左右四个键
    @BindView(R.id.ptz_video_up)
    ImageButton video_ptz_up;
    @BindView(R.id.ptz_video_down)
    ImageButton video_ptz_down;
    @BindView(R.id.ptz_video_left)
    ImageButton video_ptz_left;
    @BindView(R.id.ptz_video_right)
    ImageButton video_ptz_right;

    //放大缩小键盘
    @BindView(R.id.video_zoomout_button)
    ImageButton video_zoomout_button;
    @BindView(R.id.video_zoombig_button)
    ImageButton video_zoombig_button;

    /**
     * 加载更多
     */
    @BindView(R.id.loading_more_videosources_layout)
    ImageButton loadMoreData;

    /**
     * LIST所在的父布局
     */
    @BindView(R.id.relativelayout_listview)
    RelativeLayout relativelayout_listview;

    /**
     * 右侧控制区的布局
     */
    @BindView(R.id.show_relativelayout_all_button)
    RelativeLayout show_relativelayout_all_button;

    /**
     * 方向键的布局
     */
    @BindView(R.id.relativelayout_control)
    RelativeLayout relativelayout_control;

    /**
     * 展示视频数据的listview
     */
    @BindView(R.id.show_listresources)
    public ListView show_listresources;

    /**
     * 第一个视频源名称
     */
    @BindView(R.id.first_text)
    TextView first_text;

    /**
     * 第二个视频源名称
     */
    @BindView(R.id.second_text)
    TextView second_text;

    /**
     * 第三个视频源名称
     */
    @BindView(R.id.third_text)
    TextView third_text;

    /**
     * 第四个视频源名称
     */
    @BindView(R.id.fourth_text)
    TextView fourth_text;

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
     * 最顶头布局
     */
    @BindView(R.id.icone_relativtelayout_title)
    RelativeLayout icone_relativtelayout_title;

    /**
     * 左侧父布局
     */
    @BindView(R.id.video_view_parent_relativelayout)
    RelativeLayout video_view_parent_relativelayout;

    /**
     * 截图预览
     */
    @BindView(R.id.land_shot_priview_layout)
    ImageView priviewPic;


    /**
     * 要播放视频的数据集合
     */
    List<VideoBean> devicesList = new ArrayList<>();

    /**
     * 当前四屏的数据集合
     */
    List<VideoBean> currentList = new ArrayList<>();


    //单屏的NodeMediaclient播放器
    NodePlayer singlePlayer;
    NodePlayer firstPalyer, secondPlayer, thirdPlayer, fourthPlayer;

    /**
     * 四分屏分页加载器
     */
    PageModel pm;

    /**
     * 线程是否正在运行
     */
    boolean threadIsRun = true;


    /**
     * 当前页码
     */
    int videoCurrentPage = 1;

    /**
     * 当前状态是四分屏
     */
    boolean isCurrentFourScreen = true;

    /**
     * 当前状态是单屏
     */
    boolean isCurrentSingleScreen = false;

    /**
     * 判断这四个视频 中否被选中
     */
    boolean firstViewSelect = false;
    boolean secondViewSelect = false;
    boolean thirdViewSelect = false;
    boolean fourthViewSelect = false;

    /**
     * 当前电量显示
     */
    @BindView(R.id.prompt_electrity_values_land_layout)
    TextView displayCurrentBattery;

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
    int currentDevicePosition = -1;


    @Override
    protected int intiLayout() {
        return R.layout.activity_land_mutil_screen;
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {

        initializeTime();

        initializeViewAndData();

        initializePlayer();

        videoClickEvent();

    }

    /**
     * 显示当前的时间
     */
    private void initializeTime() {
        //时间线程
        TimingThread timeThread = new TimingThread();
        new Thread(timeThread).start();
        //当前的年月日
        SimpleDateFormat dateD = new SimpleDateFormat("yyyy年MM月dd日");
        long time = System.currentTimeMillis();
        Date date = new Date(time);
        currentYearLayout.setText(dateD.format(date).toString());
        //显示当前的电量
        int electricityValues = BatteryUtils.getSystemBattery(App.getApplication());
        displayCurrentBattery.setText(electricityValues + "");
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
                    Logutil.i("Thread error:" + e.getMessage());
                }
                handler.sendEmptyMessage(23);
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

    /**
     * 播放器单击或双击事件
     */
    private void videoClickEvent() {

        //第一个播放器单击或双击事件
        firstPlayerView.setOnTouchListener(new OnMultiTouchListener(new OnMultiTouchListener.MultiClickCallback() {
            @Override
            public void onDoubleClick() {
                //判断盛放视频数据的集合长度
                if (devicesList != null && devicesList.size() > 0) {
                    currentDevicePosition = (videoCurrentPage - 1) * 4 + 0;
                    if (currentDevicePosition == -1) {
                        currentDevice = devicesList.get(0);
                    } else {
                        currentDevice = devicesList.get(currentDevicePosition);
                    }
                    if (!TextUtils.isEmpty(currentDevice.getRtsp()))
                        initSinglePlayer(currentDevice.getRtsp());
                }
            }
        }, new OnMultiTouchListener.ClickCallback() {
            @Override
            public void onClick() {
                //第一个视频的点击事件
                handler.sendEmptyMessage(4);
            }
        }));

        secondPlayerView.setOnTouchListener(new OnMultiTouchListener(new OnMultiTouchListener.MultiClickCallback() {
            @Override
            public void onDoubleClick() {

                //判断盛放视频数据的集合长度
                if (devicesList != null && devicesList.size() > 0) {
                    currentDevicePosition = (videoCurrentPage - 1) * 4 + 1;
                    if (currentDevicePosition == -1) {
                        currentDevice = devicesList.get(0);
                    } else {
                        currentDevice = devicesList.get(currentDevicePosition);
                    }
                    if (!TextUtils.isEmpty(currentDevice.getRtsp()))
                        initSinglePlayer(currentDevice.getRtsp());
                }

            }
        }, new OnMultiTouchListener.ClickCallback() {
            @Override
            public void onClick() {

                handler.sendEmptyMessage(5);
            }
        }));


        thirdPlayerView.setOnTouchListener(new OnMultiTouchListener(new OnMultiTouchListener.MultiClickCallback() {
            @Override
            public void onDoubleClick() {
                //判断盛放视频数据的集合长度
                if (devicesList != null && devicesList.size() > 0) {

                    currentDevicePosition = (videoCurrentPage - 1) * 4 + 2;
                    if (currentDevicePosition == -1) {
                        currentDevice = devicesList.get(0);
                    } else {
                        currentDevice = devicesList.get(currentDevicePosition);
                    }
                    if (!TextUtils.isEmpty(currentDevice.getRtsp()))
                        initSinglePlayer(currentDevice.getRtsp());
                }

            }
        }, new OnMultiTouchListener.ClickCallback() {
            @Override
            public void onClick() {
                handler.sendEmptyMessage(6);

            }
        }));


        fourthPlayerView.setOnTouchListener(new OnMultiTouchListener(new OnMultiTouchListener.MultiClickCallback() {
            @Override
            public void onDoubleClick() {
                //判断盛放视频数据的集合长度
                if (devicesList != null && devicesList.size() > 0) {
                    currentDevicePosition = (videoCurrentPage - 1) * 4 + 3;
                    if (currentDevicePosition == -1) {
                        currentDevice = devicesList.get(0);
                    } else {
                        currentDevice = devicesList.get(currentDevicePosition);
                    }
                    if (!TextUtils.isEmpty(currentDevice.getRtsp()))
                        initSinglePlayer(currentDevice.getRtsp());
                }

            }
        }, new OnMultiTouchListener.ClickCallback() {
            @Override
            public void onClick() {
                handler.sendEmptyMessage(7);
            }
        }));


        single_player_layout.setOnTouchListener(new OnMultiTouchListener(new OnMultiTouchListener.MultiClickCallback() {
            @Override
            public void onDoubleClick() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        handler.sendEmptyMessage(8);
                    }
                });
            }
        }, null));
    }

    /**
     * 截图功能 （截图当时onvif图标）
     */
    @OnClick(R.id.land_shot_priview_btn_layout)
    public void shotPic(View view) {


        if (devicesList == null || devicesList.size() == 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showProgressFail("无视频源数据!");
                }
            });
            return;
        }

        VideoBean device = devicesList.get(currentDevicePosition);
        //判断当前的播放对象是否这空
        if (device != null) {
            String shotPicUrl = device.getShotPicUrl();
            if (TextUtils.isEmpty(shotPicUrl)) {
                handler.sendEmptyMessage(19);
                Logutil.e("未获取到截图url");
                return;
            }
            //子线程去截图
            SnapShotUtils screenShot = new SnapShotUtils(shotPicUrl, new SnapShotUtils.CallbackBitmap() {
                @Override
                public void getBitMap(final Bitmap bitmap) {
                    Message message = new Message();
                    message.obj = bitmap;
                    message.what = 20;
                    handler.sendMessage(message);
                }

                @Override
                public void fail(String fail) {
                    handler.sendEmptyMessage(19);
                    Logutil.e("截图失败");
                }
            });
            screenShot.start();
        }

    }

    @OnClick(R.id.send_alarmtoServer_button)
    public void sendAlarmToServer(View view) {

        VideoBean mDevice = null;
        if (devicesList == null || devicesList.size() == 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showProgressFail("无视频源数据!");
                }
            });
            return;
        }

        if (isCurrentFourScreen) {
            if (firstViewSelect) {
                mDevice = currentList.get(0);
            } else if (secondViewSelect) {
                mDevice = currentList.get(1);
            } else if (thirdViewSelect) {
                mDevice = currentList.get(2);
            } else if (fourthViewSelect) {
                mDevice = currentList.get(3);
            }
        } else {
            if (currentDevicePosition != -1)
                mDevice = currentDevice;
        }
        if (mDevice != null) {
            if (mDevice != null) {

                SendEmergencyAlarmToServerThrad sendEmergencyAlarmToServer = new SendEmergencyAlarmToServerThrad(mDevice, "视频源报警", new SendEmergencyAlarmToServerThrad.Callback() {
                    @Override
                    public void getCallbackData(String result) {
                        if (TextUtils.isEmpty(result)) {
                            Logutil.e("应急报警返回信息为Null:");
                            return;
                        }
                        Message callbackMessage = new Message();
                        callbackMessage.what = 26;
                        callbackMessage.obj = result;
                        handler.sendMessage(callbackMessage);
                    }
                });
                new Thread(sendEmergencyAlarmToServer).start();
            }

        }
    }

    /**
     * 显示截图图片
     */
    private void displayShotPic(Message msg) {
        Bitmap bitmap = (Bitmap) msg.obj;
        if (isVisible) {
            priviewPic.setVisibility(View.VISIBLE);
            priviewPic.setImageBitmap(bitmap);
        }
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isVisible)
                            priviewPic.setVisibility(View.GONE);
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

        Logutil.i(isCurrentFourScreen + "\n" + isCurrentSingleScreen);

        //四分屏时
        if (isCurrentFourScreen) {
            if (pm != null && pm.isHasNextPage()) {
                videoCurrentPage++;
                Logutil.i("当前页面:" + videoCurrentPage);
                currentList = pm.getObjects(videoCurrentPage);
                initializePlayer();
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
                initializePlayer();
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
     * 单屏播放rtsp
     */
    private void initSinglePlayer(String rtsp) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                isCurrentFourScreen = false;
                isCurrentSingleScreen = true;
                dispaly_video_loading_layout.setVisibility(View.VISIBLE);
                single_player_progressbar_layout.setVisibility(View.VISIBLE);
                dispaly_video_loading_layout.setText("正在加载");

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
        singlePlayer = new NodePlayer(this);
        singlePlayer.setPlayerView(single_player_layout);
        if (!TextUtils.isEmpty(rtsp))
            singlePlayer.setInputUrl(rtsp);
        else
            singlePlayer.setInputUrl("sss");
        singlePlayer.setNodePlayerDelegate(this);
        singlePlayer.setAudioEnable(AppConfig.ISVIDEOSOUNDS);
        singlePlayer.setVideoEnable(true);
        singlePlayer.start();
        single_player_layout.setOnClickListener(this);

    }


    /**
     * 单屏播放rtsp
     */
    @OnClick(R.id.single_screen_button_selecte)
    public void singleScreenVideo(View view) {

        isCurrentFourScreen = false;
        isCurrentSingleScreen = true;

        if (firstViewSelect) {
            String rtsp = "";
            if (!TextUtils.isEmpty(currentList.get(0).getRtsp())) {
                rtsp = currentList.get(0).getRtsp();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        display_video_information_text_layout.setText(currentList.get(0).getName());
                    }
                });
            }
            initSinglePlayer(rtsp);
        }
        if (secondViewSelect) {
            String rtsp = "";
            if (!TextUtils.isEmpty(currentList.get(1).getRtsp())) {
                rtsp = currentList.get(1).getRtsp();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        display_video_information_text_layout.setText(currentList.get(1).getName());
                    }
                });
            }
            initSinglePlayer(rtsp);
        }

        if (thirdViewSelect) {
            String rtsp = "";
            if (!TextUtils.isEmpty(currentList.get(2).getRtsp())) {
                rtsp = currentList.get(2).getRtsp();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        display_video_information_text_layout.setText(currentList.get(2).getName());
                    }
                });
            }
            initSinglePlayer(rtsp);
        }

        if (fourthViewSelect) {
            String rtsp = "";
            if (!TextUtils.isEmpty(currentList.get(3).getRtsp())) {
                rtsp = currentList.get(3).getRtsp();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        display_video_information_text_layout.setText(currentList.get(3).getName());
                    }
                });
            }
            initSinglePlayer(rtsp);
        }
    }


    /**
     * 四屏播放rtsp
     */
    @OnClick(R.id.four_screen_button_select)
    public void fourScreenVideo(View view) {

        isCurrentSingleScreen = false;
        isCurrentFourScreen = true;

        if (singlePlayer != null && singlePlayer.isPlaying()) {
            singlePlayer.pause();
            singlePlayer.stop();
        }
        four_surfaceview_parent_relativelayout.setVisibility(View.VISIBLE);
        single_surfaceview_parent_relativelayout.setVisibility(View.GONE);
        single_player_layout.setVisibility(View.GONE);

        firstPlayerView.setVisibility(View.VISIBLE);
        secondPlayerView.setVisibility(View.VISIBLE);
        thirdPlayerView.setVisibility(View.VISIBLE);
        fourthPlayerView.setVisibility(View.VISIBLE);

        firstPalyer.start();
        secondPlayer.start();
        thirdPlayer.start();
        fourthPlayer.start();
    }


    /**
     * 播放视频数据
     */
    private void initializePlayer() {
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
            fourthPlayer.setInputUrl(rtsp4);
            fourthPlayer.setNodePlayerDelegate(this);
            fourthPlayer.setAudioEnable(AppConfig.ISVIDEOSOUNDS);
            fourthPlayer.setVideoEnable(true);
            fourthPlayer.start();
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
     * 修改视频源的名称
     */
    private void updateVideoName() {

        if (isVisible) {
            if (currentList.size() == 1) {
                String firstVideoName = currentList.get(0).getName();
                if (!TextUtils.isEmpty(firstVideoName)) {
                    first_text.setText(firstVideoName);
                }

                second_text.setText("");
                third_text.setText("");
                fourth_text.setText("");
                second_dispaly_loading_layout.setVisibility(View.VISIBLE);
                second_dispaly_loading_layout.setText("无视频数据");
                second_pr_layout.setVisibility(View.GONE);
                third_dispaly_loading_layout.setVisibility(View.VISIBLE);
                third_dispaly_loading_layout.setText("无视频数据");
                third_pr_layout.setVisibility(View.GONE);
                fourth_dispaly_loading_layout.setVisibility(View.VISIBLE);
                fourth_dispaly_loading_layout.setText("无视频数据");
                fourth_pr_layout.setVisibility(View.GONE);

            }
            if (currentList.size() == 2) {
                String firstVideoName = currentList.get(0).getName();
                if (!TextUtils.isEmpty(firstVideoName)) {
                    first_text.setText(firstVideoName);
                }
                String secondVideoName = currentList.get(1).getName();
                if (!TextUtils.isEmpty(secondVideoName)) {
                    second_text.setText(secondVideoName);
                }
                third_text.setText("");
                fourth_text.setText("");
                third_dispaly_loading_layout.setVisibility(View.VISIBLE);
                third_dispaly_loading_layout.setText("无视频数据");
                third_pr_layout.setVisibility(View.GONE);
                fourth_dispaly_loading_layout.setVisibility(View.VISIBLE);
                fourth_dispaly_loading_layout.setText("无视频数据");
                fourth_pr_layout.setVisibility(View.GONE);
            }
            if (currentList.size() == 3) {
                String firstVideoName = currentList.get(0).getName();
                String secondVideoName = currentList.get(1).getName();
                String thirdVideoName = currentList.get(2).getName();
                if (!TextUtils.isEmpty(firstVideoName)) {
                    first_text.setText(firstVideoName);
                }
                if (!TextUtils.isEmpty(secondVideoName)) {
                    second_text.setText(secondVideoName);
                }
                if (!TextUtils.isEmpty(thirdVideoName)) {
                    third_text.setText(thirdVideoName);
                }
                fourth_text.setText("");
                fourth_dispaly_loading_layout.setVisibility(View.VISIBLE);
                fourth_dispaly_loading_layout.setText("无视频数据");
                fourth_pr_layout.setVisibility(View.GONE);
            }
            if (currentList.size() == 4) {
                String firstVideoName = currentList.get(0).getName();
                String secondVideoName = currentList.get(1).getName();
                String thirdVideoName = currentList.get(2).getName();
                String fourthVideoName = currentList.get(3).getName();
                if (!TextUtils.isEmpty(firstVideoName)) {
                    first_text.setText(firstVideoName);
                }
                if (!TextUtils.isEmpty(secondVideoName)) {
                    second_text.setText(secondVideoName);
                }
                if (!TextUtils.isEmpty(thirdVideoName)) {
                    third_text.setText(thirdVideoName);
                }
                if (!TextUtils.isEmpty(fourthVideoName)) {
                    fourth_text.setText(fourthVideoName);
                }
            }
        }

    }

    /**
     * 初始化view和数据
     */
    private void initializeViewAndData() {
        //头部和视频父布局监听
        icone_relativtelayout_title.setOnClickListener(this);
        video_view_parent_relativelayout.setOnClickListener(this);

        //云台按钮监听
        video_ptz_up.setOnTouchListener(this);
        video_ptz_down.setOnTouchListener(this);
        video_ptz_left.setOnTouchListener(this);
        video_ptz_right.setOnTouchListener(this);
        video_zoomout_button.setOnTouchListener(this);
        video_zoombig_button.setOnTouchListener(this);

        //加载更多监听
        loadMoreData.setOnClickListener(this);

        //实例四个播放器
        firstPalyer = new NodePlayer(this);
        firstPalyer.setPlayerView(firstPlayerView);
        firstPlayerView.setOnClickListener(this);
        secondPlayer = new NodePlayer(this);
        secondPlayer.setPlayerView(secondPlayerView);
        secondPlayerView.setOnClickListener(this);
        thirdPlayer = new NodePlayer(this);
        thirdPlayer.setPlayerView(thirdPlayerView);
        thirdPlayerView.setOnClickListener(this);
        fourthPlayer = new NodePlayer(this);
        fourthPlayer.setPlayerView(fourthPlayerView);
        fourthPlayerView.setOnClickListener(this);

        //默认第一个窗口是选中状态
        first_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_select_shape);
        second_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_shape);
        third_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_shape);
        fourth_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_shape);
        firstViewSelect = true;

        try {
            devicesList = GsonUtils.GsonToList(CryptoUtil.decodeBASE64(FileUtil.readFile(AppConfig.SOURCES_VIDEO).toString()), VideoBean.class);
        } catch (Exception e) {
        }
        if (devicesList == null || devicesList.size() == 0) {
            showProgressFail("无视频源数据");
            return;
        }
        //初始页面显示的四屏数据
        pm = new PageModel(devicesList, 4);
        currentList = pm.getObjects(videoCurrentPage);
    }


    /**
     * 播放器停止
     */
    @OnClick(R.id.icon_video_stop)
    public void stopVideo(View view) {
        if (isCurrentFourScreen) {
            if (firstViewSelect) {
                if (firstPalyer != null) {
                    firstPalyer.stop();
                }
            } else if (secondViewSelect) {
                if (secondPlayer != null) {
                    secondPlayer.stop();
                }
            } else if (thirdViewSelect) {
                if (thirdPlayer != null) {
                    thirdPlayer.stop();
                }
            } else if (fourthViewSelect) {
                if (fourthPlayer != null) {
                    fourthPlayer.stop();
                }
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtils.showShort("未选中窗口！！！");
                    }
                });
            }
        } else if (isCurrentSingleScreen) {
            if (singlePlayer != null) {
                singlePlayer.stop();
            }
        }
    }

    /**
     * 播放器重新播放
     */
    @OnClick(R.id.icon_video_restart)
    public void restartVideo(View view) {
        if (isCurrentFourScreen) {
            if (firstViewSelect) {
                if (firstPalyer != null) {
                    firstPalyer.start();
                }
            } else if (secondViewSelect) {
                if (secondPlayer != null) {
                    secondPlayer.start();
                }
            } else if (thirdViewSelect) {
                if (thirdPlayer != null) {
                    thirdPlayer.start();
                }
            } else if (fourthViewSelect) {
                if (fourthPlayer != null) {
                    fourthPlayer.start();
                }
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtils.showShort("未选中窗口！！！");
                    }
                });
            }
        } else if (isCurrentSingleScreen) {
            if (singlePlayer != null) {
                singlePlayer.start();
            }
        }
    }


    /**
     * finish本页面
     */
    @OnClick(R.id.finish_back_layout)
    public void finishThisActivity(View view) {
        if (firstPalyer != null) {
            if (firstPalyer.isPlaying())
                firstPalyer.stop();
            firstPalyer.release();
            firstPalyer = null;
        }
        if (secondPlayer != null) {
            if (secondPlayer.isPlaying())
                secondPlayer.stop();
            secondPlayer.release();
            secondPlayer = null;
        }
        if (thirdPlayer != null) {
            if (thirdPlayer.isPlaying())
                thirdPlayer.stop();
            thirdPlayer.release();
            thirdPlayer = null;
        }
        if (fourthPlayer != null) {
            if (fourthPlayer.isPlaying())
                fourthPlayer.stop();
            fourthPlayer.release();
            fourthPlayer = null;
        }
        if (singlePlayer != null) {
            if (singlePlayer.isPlaying())
                singlePlayer.stop();
            singlePlayer.release();
            singlePlayer = null;
        }
        handler.removeCallbacksAndMessages(null);
        threadIsRun = false;
        LandMutilScreenActivity.this.finish();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.loading_more_videosources_layout://加载更多数据
                initListData();
                break;
            case R.id.icone_relativtelayout_title:
                relativelayout_listview.setVisibility(View.GONE);
                show_relativelayout_all_button.setVisibility(View.VISIBLE);
                relativelayout_control.setVisibility(View.VISIBLE);
                break;
            case R.id.video_view_parent_relativelayout:
                relativelayout_listview.setVisibility(View.GONE);
                show_relativelayout_all_button.setVisibility(View.VISIBLE);
                relativelayout_control.setVisibility(View.VISIBLE);
                break;
        }
    }

    /**
     * 初始化列表数据
     */
    private void initListData() {
        if (devicesList != null && devicesList.size() > 0) {
            relativelayout_listview.setVisibility(View.VISIBLE);
            show_relativelayout_all_button.setVisibility(View.GONE);
            relativelayout_control.setVisibility(View.GONE);

            final VideoResourcesListAda ada = new VideoResourcesListAda(devicesList, LandMutilScreenActivity.this);
            View footView = (View) LayoutInflater.from(this).inflate(R.layout.footview, null);
            RelativeLayout refresh_relativelayout_data = (RelativeLayout) footView.findViewById(R.id.refresh_relativelayout_data);
            if (show_listresources.getFooterViewsCount() == 0) {
                show_listresources.addFooterView(footView);
            }
            show_listresources.setAdapter(ada);
            ada.notifyDataSetChanged();
            //刷新数据
            refresh_relativelayout_data.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ada.notifyDataSetChanged();
                    initListData();
                    handler.sendEmptyMessage(21);
                }
            });

            show_listresources.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    playListData(position);
                }
            });
        }
    }

    /**
     * 点击list列表item，并播放
     *
     * @param position
     */
    private void playListData(int position) {
        //点击后隐藏
        handler.sendEmptyMessage(22);

        currentDevicePosition = position;
        currentDevice = devicesList.get(currentDevicePosition);
        Logutil.i("点击列表:" + currentDevice.toString() + "'''" + isCurrentSingleScreen + "\n" + isCurrentFourScreen);
        if (isCurrentSingleScreen) {
            initSinglePlayer(currentDevice.getRtsp());
        }
        if (isCurrentFourScreen) {
            if (firstViewSelect) {
                if (firstPalyer != null && firstPalyer.isPlaying()) {
                    firstPalyer.stop();
                }
                if (!TextUtils.isEmpty(currentDevice.getRtsp()))
                    firstPalyer.setInputUrl(currentDevice.getRtsp());
                firstPalyer.setNodePlayerDelegate(LandMutilScreenActivity.this);
                firstPalyer.setAudioEnable(AppConfig.ISVIDEOSOUNDS);
                firstPalyer.setVideoEnable(true);
                firstPalyer.start();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        first_text.setText(currentDevice.getName());
                    }
                });

            }

            if (secondViewSelect) {
                if (secondPlayer != null && secondPlayer.isPlaying()) {
                    secondPlayer.stop();
                }
                secondPlayer.setInputUrl(currentDevice.getRtsp());
                secondPlayer.setNodePlayerDelegate(LandMutilScreenActivity.this);
                secondPlayer.setAudioEnable(AppConfig.ISVIDEOSOUNDS);
                secondPlayer.setVideoEnable(true);
                secondPlayer.start();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        second_text.setText(currentDevice.getName());
                    }
                });
            }

            if (thirdViewSelect) {
                if (thirdPlayer != null && thirdPlayer.isPlaying()) {
                    thirdPlayer.stop();
                }
                thirdPlayer.setInputUrl(currentDevice.getRtsp());
                thirdPlayer.setNodePlayerDelegate(LandMutilScreenActivity.this);
                thirdPlayer.setAudioEnable(AppConfig.ISVIDEOSOUNDS);
                thirdPlayer.setVideoEnable(true);
                thirdPlayer.start();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        third_text.setText(currentDevice.getName());
                    }
                });
            }

            if (fourthViewSelect) {
                if (fourthPlayer != null && fourthPlayer.isPlaying()) {
                    fourthPlayer.stop();
                }
                fourthPlayer.setInputUrl(currentDevice.getRtsp());
                fourthPlayer.setNodePlayerDelegate(LandMutilScreenActivity.this);
                fourthPlayer.setAudioEnable(AppConfig.ISVIDEOSOUNDS);
                fourthPlayer.setVideoEnable(true);
                fourthPlayer.start();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fourth_text.setText(currentDevice.getName());
                    }
                });
            }
        }
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
            case R.id.ptz_video_up:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    getPtzParamaters();
                    if (!TextUtils.isEmpty(mPtzUrl) && !TextUtils.isEmpty(mToken)) {
                        ControlPtzUtils controlPtz = new ControlPtzUtils(mPtzUrl, mToken, "top", 0.00, 0.03);
                        controlPtz.start();
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
                        ControlPtzUtils controlPtz = new ControlPtzUtils(mPtzUrl, mToken, "below", 0.00, -0.03);
                        controlPtz.start();
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
    public void onEventCallback(NodePlayer player, int event, String msg) {
        if (firstPalyer == player) {
            if (event == 1001) {
                handler.sendEmptyMessage(9);
            } else if (event == 1003) {
                handler.sendEmptyMessage(10);
            }
        }

        if (secondPlayer == player) {
            if (event == 1001) {
                handler.sendEmptyMessage(11);
            } else if (event == 1003) {
                handler.sendEmptyMessage(12);
            }
        }

        if (thirdPlayer == player) {
            if (event == 1001) {
                handler.sendEmptyMessage(13);
            } else if (event == 1003) {
                handler.sendEmptyMessage(14);
            }
        }

        if (fourthPlayer == player) {
            if (event == 1001) {
                handler.sendEmptyMessage(15);
            } else if (event == 1003) {
                handler.sendEmptyMessage(16);
            }
        }

        if (singlePlayer == player) {
            if (event == 1001) {
                handler.sendEmptyMessage(17);
            } else if (event == 1003) {
                handler.sendEmptyMessage(18);
            }
        }


    }

    @Override
    public void onNetChange(int state, String name) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }

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
    }

    /**
     * 状态栏图标
     */
    private void upDateStatusIcon() {

        //Sip状态
        if (AppConfig.SIP_STATUS) {
            connetIcon.setBackgroundResource(R.mipmap.icon_connection_normal);
        } else {
            connetIcon.setBackgroundResource(R.mipmap.icon_connection_disable);
        }

        //信号 状态
        int rssi = AppConfig.DEVICE_WIFI;
        if (rssi > -50 && rssi < 0) {
            networkIcon.setBackgroundResource(R.mipmap.icon_network);
        } else if (rssi > -70 && rssi <= -50) {
            networkIcon.setBackgroundResource(R.mipmap.icon_network_a);
        } else if (rssi < -70) {
            networkIcon.setBackgroundResource(R.mipmap.icon_network_b);
        } else if (rssi == -200) {
            networkIcon.setBackgroundResource(R.mipmap.icon_network_disable);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        upDateStatusIcon();

        linphoneSatusCallback();
    }

    private void linphoneSatusCallback() {

        if (SipService.isReady()) {
            SipService.addMessageCallback(new MessageCallback() {
                @Override
                public void receiverMessage(LinphoneChatMessage linphoneChatMessage) {

                    if (!linphoneChatMessage.isRead()) {
                        handler.sendEmptyMessage(24);
                    } else {
                        handler.sendEmptyMessage(25);
                    }
                }
            });
        }
    }


    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (isVisible)
                        showProgressFail("未获取到视频数据");
                    break;
                case 2:
                    first_pr_layout.setVisibility(View.VISIBLE);
                    first_dispaly_loading_layout.setVisibility(View.VISIBLE);
                    first_dispaly_loading_layout.setText("正在加载...");

                    second_pr_layout.setVisibility(View.VISIBLE);
                    second_dispaly_loading_layout.setVisibility(View.VISIBLE);
                    second_dispaly_loading_layout.setText("正在加载...");

                    third_pr_layout.setVisibility(View.VISIBLE);
                    third_dispaly_loading_layout.setVisibility(View.VISIBLE);
                    third_dispaly_loading_layout.setText("正在加载...");

                    fourth_pr_layout.setVisibility(View.VISIBLE);
                    fourth_dispaly_loading_layout.setVisibility(View.VISIBLE);
                    fourth_dispaly_loading_layout.setText("正在加载...");

                    updateVideoName();
                    break;

                case 3:
                    String videoName = (String) msg.obj;
                    if (!TextUtils.isEmpty(videoName)) {
                        display_video_information_text_layout.setText(videoName);
                    }

                    break;

                case 4://第一个播放器选中状态
                    first_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_select_shape);
                    second_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_shape);
                    third_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_shape);
                    fourth_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_shape);
                    firstViewSelect = true;
                    secondViewSelect = false;
                    thirdViewSelect = false;
                    fourthViewSelect = false;
                    break;
                case 5://第二个播放器选中状态
                    first_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_shape);
                    second_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_select_shape);
                    third_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_shape);
                    fourth_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_shape);
                    firstViewSelect = false;
                    secondViewSelect = true;
                    thirdViewSelect = false;
                    fourthViewSelect = false;
                    break;
                case 6://第三个播放器选中状态
                    first_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_shape);
                    second_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_shape);
                    third_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_select_shape);
                    fourth_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_shape);
                    firstViewSelect = false;
                    secondViewSelect = false;
                    thirdViewSelect = true;
                    fourthViewSelect = false;

                    break;
                case 7://第四个播放器选中状态
                    first_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_shape);
                    second_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_shape);
                    third_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_shape);
                    fourth_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_select_shape);
                    firstViewSelect = false;
                    secondViewSelect = false;
                    thirdViewSelect = false;
                    fourthViewSelect = true;
                    break;

                case 8://单屏双击事件
                    isCurrentSingleScreen = false;
                    isCurrentFourScreen = true;
                    if (singlePlayer != null && singlePlayer.isPlaying()) {
                        singlePlayer.pause();
                        singlePlayer.stop();
                    }
                    four_surfaceview_parent_relativelayout.setVisibility(View.VISIBLE);
                    single_surfaceview_parent_relativelayout.setVisibility(View.GONE);
                    single_player_layout.setVisibility(View.GONE);

                    firstPlayerView.setVisibility(View.VISIBLE);
                    secondPlayerView.setVisibility(View.VISIBLE);
                    thirdPlayerView.setVisibility(View.VISIBLE);
                    fourthPlayerView.setVisibility(View.VISIBLE);

                    firstPalyer.start();
                    secondPlayer.start();
                    thirdPlayer.start();
                    fourthPlayer.start();

                    break;
                case 9:
                    if (isVisible) {
                        first_pr_layout.setVisibility(View.GONE);
                        first_dispaly_loading_layout.setVisibility(View.GONE);
                    }
                    break;
                case 10:
                    if (isVisible) {
                        first_dispaly_loading_layout.setVisibility(View.VISIBLE);
                        first_pr_layout.setVisibility(View.GONE);
                        first_dispaly_loading_layout.setText("重新连接...");
                    }
                    break;
                case 11:
                    if (isVisible) {
                        second_pr_layout.setVisibility(View.GONE);
                        second_dispaly_loading_layout.setVisibility(View.GONE);
                    }
                    break;
                case 12:
                    if (isVisible) {
                        second_dispaly_loading_layout.setVisibility(View.VISIBLE);
                        second_pr_layout.setVisibility(View.GONE);
                        second_dispaly_loading_layout.setText("重新连接...");
                    }
                    break;

                case 13:
                    if (isVisible) {
                        third_pr_layout.setVisibility(View.GONE);
                        third_dispaly_loading_layout.setVisibility(View.GONE);
                    }
                    break;
                case 14:
                    if (isVisible) {
                        third_dispaly_loading_layout.setVisibility(View.VISIBLE);
                        third_pr_layout.setVisibility(View.GONE);
                        third_dispaly_loading_layout.setText("重新连接...");
                    }
                    break;

                case 15:
                    if (isVisible) {
                        fourth_pr_layout.setVisibility(View.GONE);
                        fourth_dispaly_loading_layout.setVisibility(View.GONE);
                    }
                    break;
                case 16:
                    if (isVisible) {
                        fourth_dispaly_loading_layout.setVisibility(View.VISIBLE);
                        fourth_pr_layout.setVisibility(View.GONE);
                        fourth_dispaly_loading_layout.setText("重新连接...");
                    }
                    break;

                case 17:
                    if (isVisible) {
                        single_player_progressbar_layout.setVisibility(View.GONE);
                        dispaly_video_loading_layout.setVisibility(View.GONE);
                    }
                    break;
                case 18:
                    if (isVisible) {
                        dispaly_video_loading_layout.setVisibility(View.VISIBLE);
                        single_player_progressbar_layout.setVisibility(View.GONE);
                        dispaly_video_loading_layout.setText("重新连接...");
                    }
                    break;
                case 19:
                    if (isVisible)
                        showProgressFail("截图失败");
                    break;
                case 20:
                    if (isVisible)
                        displayShotPic(msg);
                    break;
                case 21:
                    if (isVisible)
                        showProgressSuccess("数据已更新");
                    break;
                case 22:
                    if (isVisible) {
                        relativelayout_listview.setVisibility(View.GONE);
                        show_relativelayout_all_button.setVisibility(View.VISIBLE);
                        relativelayout_control.setVisibility(View.VISIBLE);
                    }
                    break;
                case 23:
                    if (isVisible)
                        displayCurrentTime();
                    break;

                case 24:
                    messageIcon.setBackgroundResource(R.mipmap.message);
                    break;
                case 25:
                    messageIcon.setBackgroundResource(R.mipmap.newmessage);
                    break;
                case 26:
                    if (isVisible) {
                        String result = (String) msg.obj;
                        if (!TextUtils.isEmpty(result)) {
                            if (result.contains("error")) {
                                showProgressFail("报警失败！");
                            } else {
                                showProgressSuccess("报警成功！");
                            }
                        }
                    }
                    break;
                case 27:
                    int level = msg.arg1;

                    if (isVisible) {
                        displayCurrentBattery.setText("" + level);
                    }
                    break;
            }
        }
    };
}
