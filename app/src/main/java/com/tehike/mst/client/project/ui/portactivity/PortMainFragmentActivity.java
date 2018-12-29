package com.tehike.mst.client.project.ui.portactivity;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.tehike.mst.client.project.R;
import com.tehike.mst.client.project.adapters.SlidingPagerAdapter;
import com.tehike.mst.client.project.base.BaseActivity;
import com.tehike.mst.client.project.cms.SendEmergencyAlarmToServerThrad;
import com.tehike.mst.client.project.entity.VideoBean;
import com.tehike.mst.client.project.global.AppConfig;
import com.tehike.mst.client.project.linphone.Linphone;
import com.tehike.mst.client.project.linphone.MessageCallback;
import com.tehike.mst.client.project.linphone.PhoneCallback;
import com.tehike.mst.client.project.linphone.RegistrationCallback;
import com.tehike.mst.client.project.linphone.SipManager;
import com.tehike.mst.client.project.linphone.SipService;
import com.tehike.mst.client.project.sysinfo.SysinfoUtils;
import com.tehike.mst.client.project.ui.fragment.ChatListFragment;
import com.tehike.mst.client.project.ui.fragment.IntercomFragment;
import com.tehike.mst.client.project.ui.fragment.VoideoFragment;
import com.tehike.mst.client.project.utils.GsonUtils;
import com.tehike.mst.client.project.utils.Logutil;
import com.tehike.mst.client.project.utils.NetworkUtils;
import com.tehike.mst.client.project.utils.SharedPreferencesUtils;
import com.tehike.mst.client.project.utils.ToastUtils;

import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneChatMessage;
import org.linphone.core.LinphoneChatRoom;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;

/**
 * 描述：处理滑动页面
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2018/11/19 15:22
 */


public class PortMainFragmentActivity extends BaseActivity {
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
    ImageView connetIConb;

    /**
     * 消息图标
     */
    @BindView(R.id.icon_message_show)
    ImageView messageIcon;

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
     * viewpager左右滑动
     */
    @BindView(R.id.main_view_pager)
    ViewPager mViewPager;

    /**
     * 无网络时显示的而已
     */
    @BindView(R.id.no_network_layout)
    RelativeLayout no_network_layout;

    /**
     * 勤务通信
     */
    @BindView(R.id.port_bottom_intercom_radio_btn)
    RadioButton port_bottom_intercom_radio_btn;

    /**
     * 视频监控
     */
    @BindView(R.id.port_bottom_video_radio_btn)
    RadioButton port_bottom_video_radio_btn;

    /**
     * 即时通信
     */
    @BindView(R.id.port_bottom_chat_radio_btn)
    RadioButton port_bottom_chat_radio_btn;

    /**
     * 应急报警
     */
    @BindView(R.id.port_bottom_alarm_radio_btn)
    RadioButton port_bottom_alarm_radio_btn;

    /**
     * 申请供弹
     */
    @BindView(R.id.port_bottom_apply_for_play_radio_btn)
    RadioButton port_bottom_apply_for_play_radio_btn;

    /**
     * 设置中心
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
     * 页面集合
     */
    private List<Fragment> list = new ArrayList<>();

    /**
     * 当前页面标识
     */
    int currentPager = 0;

    /**
     * 时间显示线程是否在运行标识
     */
    boolean threadIsRun = true;

    @Override
    protected int intiLayout() {
        return R.layout.activity_port_main_fragment;
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {

        //初始化时间显示
        initializeTime();

        //初始化viewpager显示
        initializeFragments();

        //viewpager滑动监听
        initializeViewPagerListern();

        //底部radioButton点击事件
        initializeBottomBtnClick();

    }

    /**
     * 底部的radioButton点击事件事件
     */
    private void initializeBottomBtnClick() {
        //勤务通信
        port_bottom_intercom_radio_btn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mViewPager.setCurrentItem(0);
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
                    mViewPager.setCurrentItem(1);
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
                    mViewPager.setCurrentItem(2);
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
     * 验证密码
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
        final PopupWindow popu = new PopupWindow(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        popu.setContentView(view);
        //显示所在的位置
        View rootview = LayoutInflater.from(PortMainFragmentActivity.this).inflate(R.layout.activity_port_main_fragment, null);
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
                   // intent.setClass(PortMainFragmentActivity.this, PortSettingActivity.class);
                    PortMainFragmentActivity.this.startActivity(intent);
                } else {
                    //不正确就提示
                    handler.sendEmptyMessage(22);
                }
            }
        });
        //点击返回键,popuwindow消失
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



        //子线程去发送应急报警
        SendEmergencyAlarmToServerThrad sendEmergencyAlarmToServer = new SendEmergencyAlarmToServerThrad(null, "暴狱", new SendEmergencyAlarmToServerThrad.Callback() {
            @Override
            public void getCallbackData(String result) {
                if (TextUtils.isEmpty(result)) {
                    Logutil.e("应急报警返回信息为Null:");
                    return;
                }
                Message callbackMessage = new Message();
                callbackMessage.what = 12;
                callbackMessage.obj = result;
                handler.sendMessage(callbackMessage);
            }
        });
        //执行子线程
        new Thread(sendEmergencyAlarmToServer).start();
    }

    /**
     * ViewPager滑动监听
     */
    private void initializeViewPagerListern() {
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(final int position) {

                Logutil.i("position:" + position);
                if (position == 0) {
                    port_bottom_intercom_radio_btn.setChecked(true);
                    port_bottom_video_radio_btn.setChecked(false);
                    port_bottom_chat_radio_btn.setChecked(false);
                    port_bottom_alarm_radio_btn.setChecked(false);
                    port_bottom_apply_for_play_radio_btn.setChecked(false);
                    port_bottom_setting_radio_btn.setChecked(false);
                    port_bottom_cluster_intercom_radio_btn.setChecked(false);
                    port_bottom_live_meeting_radio_btn.setChecked(false);
                } else if (position == 1) {
                    port_bottom_intercom_radio_btn.setChecked(false);
                    port_bottom_video_radio_btn.setChecked(true);
                    port_bottom_chat_radio_btn.setChecked(false);
                    port_bottom_alarm_radio_btn.setChecked(false);
                    port_bottom_apply_for_play_radio_btn.setChecked(false);
                    port_bottom_setting_radio_btn.setChecked(false);
                    port_bottom_cluster_intercom_radio_btn.setChecked(false);
                    port_bottom_live_meeting_radio_btn.setChecked(false);
                } else if (position == 2) {
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

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }

    /**
     * 初始化要显示的Fragment
     */
    private void initializeFragments() {
        list.add(new IntercomFragment());
        list.add(new VoideoFragment());
        list.add(new ChatListFragment());
        final SlidingPagerAdapter adapter = new SlidingPagerAdapter(getSupportFragmentManager(), list);
        mViewPager.setAdapter(adapter);

        currentPager = getIntent().getIntExtra("current", 0);
        mViewPager.setCurrentItem(currentPager);
        Logutil.i("current:" + currentPager);
        if (currentPager == 0) {
            port_bottom_intercom_radio_btn.setChecked(true);
            port_bottom_video_radio_btn.setChecked(false);
            port_bottom_chat_radio_btn.setChecked(false);
            port_bottom_alarm_radio_btn.setChecked(false);
            port_bottom_apply_for_play_radio_btn.setChecked(false);
            port_bottom_setting_radio_btn.setChecked(false);
            port_bottom_cluster_intercom_radio_btn.setChecked(false);
            port_bottom_live_meeting_radio_btn.setChecked(false);
        } else if (currentPager == 1) {
            port_bottom_intercom_radio_btn.setChecked(false);
            port_bottom_video_radio_btn.setChecked(true);
            port_bottom_chat_radio_btn.setChecked(false);
            port_bottom_alarm_radio_btn.setChecked(false);
            port_bottom_apply_for_play_radio_btn.setChecked(false);
            port_bottom_setting_radio_btn.setChecked(false);
            port_bottom_cluster_intercom_radio_btn.setChecked(false);
            port_bottom_live_meeting_radio_btn.setChecked(false);
        } else if (currentPager == 2) {
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

    /**
     * 时间显示处理
     */
    private void initializeTime() {

        //启动线程每秒刷新一下
        TimingThread timeThread = new TimingThread();
        new Thread(timeThread).start();

        //显示当前的年月日
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日");
        String currntYearDate = simpleDateFormat.format(date);
        //显示
        if (!TextUtils.isEmpty(currntYearDate))
            currentYearLayout.setText(currntYearDate);
    }

    /**
     * wifi变化时的状回调
     *
     * @param state
     * @param name
     */
    @Override
    public void onNetChange(int state, String name) {
    }

    //显示时间的线程
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
                handler.sendEmptyMessage(1001);
            } while (threadIsRun);
        }
    }

    /**
     * 显示当前的时分秒
     */
    private void disPlayTime() {
        Date date = new Date();
        SimpleDateFormat timeD = new SimpleDateFormat("HH:mm:ss");
        String currentTime = timeD.format(date).toString();
        if (!TextUtils.isEmpty(currentTime)) {
            if (isVisible)
                currentTimeLayout.setText(currentTime);
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

    @Override
    protected void onResume() {
        super.onResume();

        //状态监听
        if (SipService.isReady()) {
            Linphone.addCallback(new RegistrationCallback() {
                @Override
                public void registrationOk() {
                    updateUi(connetIConb, R.mipmap.icon_connection_normal);
                }

                @Override
                public void registrationFailed() {
                    super.registrationFailed();
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
        }

        //sip显示状态
        if (AppConfig.SIP_STATUS) {
            updateUi(connetIConb, R.mipmap.icon_connection_normal);
        } else {
            updateUi(connetIConb, R.mipmap.icon_connection_disable);
        }
        //电量显示状态
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
        //信号状态显示
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
        super.onDestroy();
        //停止时间线程循环
        threadIsRun = false;
        //移除handler监听
        if (handler != null)
            handler.removeCallbacksAndMessages(null);
    }



    /**
     * Handler处理消息
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1001:
                    //显示时分秒
                    disPlayTime();
                    break;
                case 10:
                    //新消息提示
                    if (isVisible)
                        messageIcon.setBackgroundResource(R.mipmap.newmessage);
                    break;
                case 11:
                    //去除新消息提示
                    if (isVisible)
                        messageIcon.setBackgroundResource(R.mipmap.message);
                    break;
                case 12:
                    //报警结果
                    String returnResult = (String) msg.obj;
                    if (!TextUtils.isEmpty(returnResult)) {
                        if (returnResult.contains("error")) {
                            showProgressFail("报警异常!");
                        } else {
                            showProgressSuccess("报警成功!");
                        }
                    }
                    break;
                case 21:
                    //提示密码不能为空
                    ToastUtils.showShort("不能为空!");
                    break;
                case 22:
                    //提示密码不正确
                    ToastUtils.showShort("密码不正确!");
            }
        }
    };
}
