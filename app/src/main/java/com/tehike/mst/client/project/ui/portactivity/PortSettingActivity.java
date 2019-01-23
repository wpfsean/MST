package com.tehike.mst.client.project.ui.portactivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tehike.mst.client.project.R;
import com.tehike.mst.client.project.base.App;
import com.tehike.mst.client.project.base.BaseActivity;
import com.tehike.mst.client.project.global.AppConfig;
import com.tehike.mst.client.project.linphone.Linphone;
import com.tehike.mst.client.project.linphone.PhoneCallback;
import com.tehike.mst.client.project.linphone.RegistrationCallback;
import com.tehike.mst.client.project.linphone.SipManager;
import com.tehike.mst.client.project.linphone.SipService;
import com.tehike.mst.client.project.sysinfo.SysinfoUtils;
import com.tehike.mst.client.project.ui.landactivity.LandLoginActivity;
import com.tehike.mst.client.project.ui.widget.CustomDialog;
import com.tehike.mst.client.project.ui.widget.WiperSwitch;
import com.tehike.mst.client.project.update.UpdateManager;
import com.tehike.mst.client.project.utils.ActivityUtils;
import com.tehike.mst.client.project.utils.ContextUtils;
import com.tehike.mst.client.project.utils.NetworkUtils;
import com.tehike.mst.client.project.utils.ScreenUtils;
import com.tehike.mst.client.project.utils.SharedPreferencesUtils;

import org.linphone.core.LinphoneCall;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 描述：竖屏设置中心
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2018/11/14 10:06
 */

public class PortSettingActivity extends BaseActivity {


    /**
     * 自定义的dialog
     */
    private CustomDialog dialog;

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
     * 登录 信息
     */
    @BindView(R.id.login_group_port_layout)
    LinearLayout loginParentLayout;

    /**
     * 登录信息的展开布局
     */
    @BindView(R.id.login_child_port_layout)
    LinearLayout loginChildLayout;

    /**
     * 视频设置
     */
    @BindView(R.id.video_group_port_layout)
    LinearLayout videoParentLayout;

    /**
     * 视频设置的子布局
     */
    @BindView(R.id.video_child_port_layout)
    LinearLayout videoChildLayout;

    /**
     * 报警设置
     */
    @BindView(R.id.alarm_group_port_layout)
    LinearLayout alarmParentLayout;

    /**
     * 报警设置的子布局
     */
    @BindView(R.id.alarm_child_port_layout)
    LinearLayout alarmChildLayout;

    /**
     * 弹箱设置
     */
    @BindView(R.id.box_group_port_layout)
    LinearLayout buleBoxParentLayout;

    /**
     * 弹箱设置的子布局
     */
    @BindView(R.id.box_child_port_layout)
    LinearLayout blueBoxChildLayout;

    /**
     * 系统设置
     */
    @BindView(R.id.system_setting_group_port_layout)
    LinearLayout systemParentLayout;

    /**
     * 系统设置的子布局
     */
    @BindView(R.id.system_setting_child_port_layout)
    LinearLayout systemChildLayout;

    /**
     * 常用 设置
     */
    @BindView(R.id.inuse_setting_group_port_layout)
    LinearLayout inuseParentLayout;

    /**
     * 常用 设置的布局
     */
    @BindView(R.id.inuse_setting_child_port_layout)
    LinearLayout inuseChildLayout;

    /**
     * 方向设置
     */
    @BindView(R.id.direction_setting_group_port_layout)
    LinearLayout directionParentLayout;

    /**
     * 方向设置的布局
     */
    @BindView(R.id.direction_setting_child_port_layout)
    LinearLayout directionChildLayout;

    /**
     * 关于我们
     */
    @BindView(R.id.about_group_port_layout)
    LinearLayout aboutParentLayout;

    /**
     * 方关于我们的布局
     */
    @BindView(R.id.about_child_port_layout)
    LinearLayout aboutChildLayout;

    /**
     * 是否自动登录
     */
    @BindView(R.id.switch_autologin_layout)
    WiperSwitch switchAutoLogin;

    /**
     * 是否记住密码
     */
    @BindView(R.id.switch_remember_pwd_layout)
    WiperSwitch switchRemeberPwd;

    /**
     * 是否播放声音
     */
    @BindView(R.id.switch_isopen_sound_layout)
    WiperSwitch switchOpenSound;

    /**
     * 是否用主码流（true 主码流， false 子码流）
     */
    @BindView(R.id.switch_ismain_stream_layout)
    WiperSwitch switchOpenMainStream;

    /**
     * 显示时间的格式
     */
    SimpleDateFormat hoursFormat = null;

    /**
     * 显示时间的线程是否正在运行标识
     */
    boolean threadIsRun = true;


    //登录信息的子布局是否可见
    boolean whetherLoginChildVisiable = true;
    //视频监听的子布局是否可见
    boolean whetherVideoChildVisiable = false;
    //报警信息的子布局是否可见
    boolean whetherAlarmChildVisiable = false;
    //弹箱连接的子布局是否可见
    boolean whetherBoxChildVisiable = false;
    //系统设置的子布局是否可见
    boolean whetherSystemChildVisiable = false;
    //常用设置的子布局是否可见
    boolean whetherInuseChildVisiable = false;
    //方向设置的子布局是否可见
    boolean whetherDirectionChildVisiable = false;
    //关于我们的子布局是否可见
    boolean whetherAboutChildVisiable = false;


    @Override
    protected int intiLayout() {
        return R.layout.activity_port_setting_layout;
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {


        //验证权限并申请
        checkPermission();

        //初始化view
        initializeView();

        //初始化数据
        initializeData();

        //初始化时间显示
        initializeDate();
    }


    /**
     * 初始化view
     */
    private void initializeView() {
        //显示登录子布局
        loginChildLayout.setVisibility(View.VISIBLE);
        //隐藏视频监控的子布局
        videoChildLayout.setVisibility(View.GONE);
        //隐藏报警设置的子布局
        alarmChildLayout.setVisibility(View.GONE);
        //隐藏弹箱连接的子布局
        blueBoxChildLayout.setVisibility(View.GONE);
        //隐藏系统设置的子布局
        systemChildLayout.setVisibility(View.GONE);
        //隐藏常用设置的子布局
        inuseChildLayout.setVisibility(View.GONE);
        //隐藏方向设置的子布局
        directionChildLayout.setVisibility(View.GONE);
        //隐藏关于我们的子布局
        aboutChildLayout.setVisibility(View.GONE);

    }

    /**
     * 显示当前状态
     */
    private void initializeData() {
        //是否正在登录
        boolean isAutoLogin = (boolean) SharedPreferencesUtils.getObject(PortSettingActivity.this, "autologin", false);
        if (isAutoLogin) {
            switchAutoLogin.setChecked(true);
        } else {
            switchAutoLogin.setChecked(false);
        }

        //是否记住密码
        boolean isrePwd = (boolean) SharedPreferencesUtils.getObject(PortSettingActivity.this, "isremember", false);
        if (isrePwd) {
            switchRemeberPwd.setChecked(true);
        } else {
            switchRemeberPwd.setChecked(false);
        }

        //是否设置声音开启
        boolean isOpenSound = AppConfig.ISVIDEOSOUNDS;
        if (isOpenSound) {
            switchOpenSound.setChecked(true);
        } else {
            switchOpenSound.setChecked(false);
        }

        //是否设置onvif解析取主码流
        boolean isMainStream = AppConfig.IS_MAIN_STREAM;
        if (isMainStream) {
            switchOpenMainStream.setChecked(true);
        } else {
            switchOpenMainStream.setChecked(false);
        }

        switchRemeberPwd.setOnChangedListener(new WiperSwitch.OnChangedListener() {
            @Override
            public void OnChanged(WiperSwitch wiperSwitch, boolean checkState) {

                if (checkState) {
                    SharedPreferencesUtils.putObject(PortSettingActivity.this, "isremember", true);
                } else {
                    SharedPreferencesUtils.putObject(PortSettingActivity.this, "isremember", false);
                }
            }
        });

        switchAutoLogin.setOnChangedListener(new WiperSwitch.OnChangedListener() {
            @Override
            public void OnChanged(WiperSwitch wiperSwitch, boolean checkState) {
                if (checkState) {
                    SharedPreferencesUtils.putObject(PortSettingActivity.this, "autologin", true);
                } else {
                    SharedPreferencesUtils.putObject(PortSettingActivity.this, "autologin", false);
                }
            }
        });

        switchOpenMainStream.setOnChangedListener(new WiperSwitch.OnChangedListener() {
            @Override
            public void OnChanged(WiperSwitch wiperSwitch, boolean checkState) {
                if (checkState) {
                    AppConfig.IS_MAIN_STREAM = true;
                } else {
                    AppConfig.IS_MAIN_STREAM = false;
                }
            }
        });

        switchOpenSound.setOnChangedListener(new WiperSwitch.OnChangedListener() {
            @Override
            public void OnChanged(WiperSwitch wiperSwitch, boolean checkState) {
                if (checkState) {
                    AppConfig.ISVIDEOSOUNDS = true;
                } else {
                    AppConfig.ISVIDEOSOUNDS = false;
                }
            }
        });

    }

    /**
     * 显示当前的时间
     */
    private void initializeDate() {
        //启动线程每秒刷新一下
        TimingThread timeThread = new TimingThread();
        new Thread(timeThread).start();

        //显示当前的年月日
        Date date = new Date();
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy年MM月dd日");
        hoursFormat = new SimpleDateFormat("HH:mm:ss");
        String currntYearDate = yearFormat.format(date);
        //显示当前的年月日
        if (!TextUtils.isEmpty(currntYearDate))
            currentYearLayout.setText(currntYearDate);
    }

    /**
     * 每隔1秒刷新一下时间的线程
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
     * finish本页面
     *
     * @param view
     */
    @OnClick(R.id.finish_back_layout)
    public void finishPage(View view) {
        ActivityUtils.removeActivity(this);
        this.finish();
    }

    /**
     * 点击事件
     *
     * @param view
     */
    @OnClick({R.id.setting_alarm_ip_layout, R.id.app_clear_cach_layout, R.id.update_apk_port_layout, R.id.logout_port_layout, R.id.box_child_port_layout, R.id.direction_setting_child_port_layout, R.id.private_network1_setting_port_layout, R.id.display_account_infor_port_layout, R.id.display_pwd_infor_port_layout, R.id.direction_setting_group_port_layout, R.id.feedback_child_port_layout, R.id.login_group_port_layout, R.id.video_group_port_layout, R.id.alarm_group_port_layout, R.id.box_group_port_layout, R.id.system_setting_group_port_layout, R.id.inuse_setting_group_port_layout, R.id.about_group_port_layout, R.id.port_set_lumninance_layout})
    public void onclickEvent(View view) {
        switch (view.getId()) {
            case R.id.setting_alarm_ip_layout:
                //设置报警ip
                displaySettingAlarmDialog();
                break;
            case R.id.app_clear_cach_layout:
                //清除数据库的缓存
                clearCach();
                break;
            case R.id.update_apk_port_layout:
                //更新apk
                new UpdateManager(this).checkUpdate();
                break;
            case R.id.logout_port_layout:
                //退出登录
                logoutApp();
                break;
            case R.id.private_network1_setting_port_layout:
                //专网1设置
                openActivity(PortWifiActivity.class);
                break;
            case R.id.display_account_infor_port_layout:
                //显示帐号信息
                displayAccountDialog();
                break;
            case R.id.display_pwd_infor_port_layout:
                //显示密码
                displayPwdDialog();
                break;
            case R.id.direction_setting_child_port_layout:
                //方向设置
                setDirection();
                break;
            case R.id.feedback_child_port_layout:
                //意见反馈
                displayFeedbackDialog();
                break;
            case R.id.port_set_lumninance_layout:
                displaySetbrignessDialog();
                break;
            case R.id.login_group_port_layout:
                //登录信息点击 隐藏或显示
                if (whetherLoginChildVisiable == false) {
                    whetherLoginChildVisiable = true;
                    loginChildLayout.setVisibility(View.VISIBLE);
                } else if (whetherLoginChildVisiable == true) {
                    whetherLoginChildVisiable = false;
                    loginChildLayout.setVisibility(View.GONE);
                }
                break;
            case R.id.video_group_port_layout:
                //视频 设置的item 隐藏或显示
                if (whetherVideoChildVisiable == false) {
                    whetherVideoChildVisiable = true;
                    videoChildLayout.setVisibility(View.VISIBLE);
                } else if (whetherVideoChildVisiable == true) {
                    whetherVideoChildVisiable = false;
                    videoChildLayout.setVisibility(View.GONE);
                }
                break;
            case R.id.alarm_group_port_layout:
                //报警设置 设置的item 隐藏或显示
                if (whetherAlarmChildVisiable == false) {
                    whetherAlarmChildVisiable = true;
                    alarmChildLayout.setVisibility(View.VISIBLE);
                } else if (whetherAlarmChildVisiable == true) {
                    whetherAlarmChildVisiable = false;
                    alarmChildLayout.setVisibility(View.GONE);
                }
                break;
            case R.id.box_group_port_layout:
                //弹箱设置 设置的item 隐藏或显示
                if (whetherBoxChildVisiable == false) {
                    whetherBoxChildVisiable = true;
                    blueBoxChildLayout.setVisibility(View.VISIBLE);
                } else if (whetherBoxChildVisiable == true) {
                    whetherBoxChildVisiable = false;
                    blueBoxChildLayout.setVisibility(View.GONE);
                }
                break;
            case R.id.system_setting_group_port_layout:
                //系统设置 设置的item 隐藏或显示
                if (whetherSystemChildVisiable == false) {
                    whetherSystemChildVisiable = true;
                    systemChildLayout.setVisibility(View.VISIBLE);
                } else if (whetherSystemChildVisiable == true) {
                    whetherSystemChildVisiable = false;
                    systemChildLayout.setVisibility(View.GONE);
                }
                break;
            case R.id.inuse_setting_group_port_layout:
                //系统设置 设置的item 隐藏或显示
                if (whetherInuseChildVisiable == false) {
                    whetherInuseChildVisiable = true;
                    inuseChildLayout.setVisibility(View.VISIBLE);
                } else if (whetherInuseChildVisiable == true) {
                    whetherInuseChildVisiable = false;
                    inuseChildLayout.setVisibility(View.GONE);
                }
                break;
            case R.id.direction_setting_group_port_layout:
                //系统设置 设置的item 隐藏或显示
                if (whetherDirectionChildVisiable == false) {
                    whetherDirectionChildVisiable = true;
                    directionChildLayout.setVisibility(View.VISIBLE);
                } else if (whetherDirectionChildVisiable == true) {
                    whetherDirectionChildVisiable = false;
                    directionChildLayout.setVisibility(View.GONE);
                }
                break;
            case R.id.about_group_port_layout:
                //关于我们 设置的item 隐藏或显示
                if (whetherAboutChildVisiable == false) {
                    whetherAboutChildVisiable = true;
                    aboutChildLayout.setVisibility(View.VISIBLE);
                } else if (whetherDirectionChildVisiable == true) {
                    whetherAboutChildVisiable = false;
                    aboutChildLayout.setVisibility(View.GONE);
                }
                break;
            case R.id.box_child_port_layout:
                //蓝牙弹箱连接设置
                openActivity(PortBlueToothActivity.class);
                break;
        }
    }

    /**
     * 显示调试亮度的提示框
     */
    private void displaySetbrignessDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.prompt_set_lightness_layout, null);
        //显示当前亮度值的控件
        final TextView promptLightnessValues = view.findViewById(R.id.prompt_current_lighness_values_layout);

        SeekBar lumninanceBar = view.findViewById(R.id.prompt_current_lighness_seekbar_layout);
        //popuwindow显示
        final PopupWindow popu = new PopupWindow(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        popu.setContentView(view);
        //显示所在的位置
        View rootview = LayoutInflater.from(PortSettingActivity.this).inflate(R.layout.activity_port_setting_layout, null);
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

        //先获取系统的亮度
        int systemBrightness = 0;
        try {
            systemBrightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        //显示当前 的亮度
        promptLightnessValues.setText("当前亮度:" + systemBrightness);
        lumninanceBar.setProgress(systemBrightness);
        lumninanceBar.setMax(255);

        //拖动条的拖动事件
        lumninanceBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                final int process = seekBar.getProgress();
                WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
                final float f = process / 255.0F;
                localLayoutParams.screenBrightness = f;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        promptLightnessValues.setText("当前亮度:" + process);
                    }
                });
                getWindow().setAttributes(localLayoutParams);
                //修改系统的亮度值,以至于退出应用程序亮度保持
                //改变系统的亮度值(申请权限失败)
                //这里需要权限android.permission.WRITE_SETTINGS
                //设置为手动调节模式
                Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                //保存到系统中
                Uri uri = Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS);
                Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, process);
                getContentResolver().notifyChange(uri, null);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

    }

    /**
     * 设置方向
     * finish本页面
     */
    private void setDirection() {
        ActivityUtils.removeAllActivity();
        this.finish();
        AppConfig.APP_DIRECTION = 1;
        startActivity(new Intent(PortSettingActivity.this, LandLoginActivity.class));
    }

    /**
     * 清除缓存信息
     */
    private void clearCach() {

    }

    /**
     * 退出登录
     */
    private void logoutApp() {
        ActivityUtils.removeAllActivity();
        App.exit();
        finish();
        if (SipManager.getLc() != null) {
            SipManager.getLc().clearProxyConfigs();
        }
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    /**
     * 显示当前的帐号信息
     */
    private void displayAccountDialog() {
        //获取deviceName
        String deviceName = SysinfoUtils.getSysinfo().getDeviceName();
        //获取Guid
        String guid = SysinfoUtils.getSysinfo().getDeviceGuid();
        //显示的view
        View view = LayoutInflater.from(this).inflate(R.layout.prompt_box_layout, null);
        //控件显示内容
        TextView title = view.findViewById(R.id.propmt_title_laout);
        TextView child1 = view.findViewById(R.id.propmt_child1_laout);
        TextView child2 = view.findViewById(R.id.propmt_child2_laout);
        title.setText("帐号显示");
        child1.setText("当前用户:" + deviceName);
        child2.setText("当前Guid:" + guid);
        //popuwindow显示
        final PopupWindow popu = new PopupWindow(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        popu.setContentView(view);
        popu.setBackgroundDrawable(new BitmapDrawable());
        popu.setFocusable(true);
        popu.setTouchable(true);
        popu.setOutsideTouchable(true);
        popu.update();
        //显示所在的位置
        View rootview = LayoutInflater.from(PortSettingActivity.this).inflate(R.layout.activity_land_setting_layout, null);
        popu.showAtLocation(rootview, Gravity.CENTER, 0, 0);
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
        //点击外面消失
        popu.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (popu.isShowing()) {
                    popu.dismiss();
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * 显示当前的帐号密码
     */
    private void displayPwdDialog() {
        String pwd = SysinfoUtils.getUserPwd();
        if (TextUtils.isEmpty(pwd))
            pwd = AppConfig.PWD;

        View view = LayoutInflater.from(this).inflate(R.layout.prompt_box_layout, null);

        //控件显示内容
        TextView title = view.findViewById(R.id.propmt_title_laout);
        TextView child1 = view.findViewById(R.id.propmt_child1_laout);
        TextView child2 = view.findViewById(R.id.propmt_child2_laout);
        title.setText("密码显示");
        child1.setText("当前用户密码:" + pwd);
        //popuwindow显示
        final PopupWindow popu = new PopupWindow(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        popu.setContentView(view);
        popu.setBackgroundDrawable(new BitmapDrawable());
        popu.setFocusable(true);
        popu.setTouchable(true);
        popu.setOutsideTouchable(true);
        popu.update();
        //显示所在的位置
        View rootview = LayoutInflater.from(PortSettingActivity.this).inflate(R.layout.activity_land_setting_layout, null);
        popu.showAtLocation(rootview, Gravity.CENTER, 0, 0);
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
        //点击外面消失
        popu.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (popu.isShowing()) {
                    popu.dismiss();
                    return true;
                }
                return false;
            }
        });

    }

    /**
     * 设置报警ip
     */
    private void displaySettingAlarmDialog() {
        //显示的view
        View view = LayoutInflater.from(this).inflate(R.layout.prompt_update_alarm_layout, null);

        //ip输入控件
        final EditText alarmIp = view.findViewById(R.id.update_alarm_ip_layout);
        //端口输入控件
        final EditText alarmPort = view.findViewById(R.id.update_alarm_port_layout);
        //确认信息
        TextView sureTv = view.findViewById(R.id.update_sure_layout);

        //popuwindow显示
        final PopupWindow popu = new PopupWindow(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        popu.setContentView(view);
        //显示所在的位置
        View rootview = LayoutInflater.from(PortSettingActivity.this).inflate(R.layout.activity_land_setting_layout, null);
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

        //确认点击事件
        sureTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取Ip输入框内容
                final String edAlarmIp = alarmIp.getText().toString().trim();
                //获取port输入框内容
                final String edAlarmPort = alarmPort.getText().toString().trim();
                //判断ip是否为空
                if (TextUtils.isEmpty(edAlarmIp)) {
                    return;
                }
                //判断ip是否合法
                if (!NetworkUtils.isboolIp(edAlarmIp)) {
                    return;
                }
                //判断端口是否为空
                if (TextUtils.isEmpty(edAlarmPort)) {
                    return;
                }
                //开启子线程去更改数据库信息


                if (popu.isShowing()) {
                    popu.dismiss();
                }
                if (isVisible)
                    showProgressSuccess("更改成功！");
            }
        });
    }

    /**
     * 显示意见反馈框
     */
    private void displayFeedbackDialog() {
        View view = LayoutInflater.from(PortSettingActivity.this).inflate(R.layout.send_comment_dialog_port, null);
        final EditText et_content = ButterKnife.findById(view, R.id.et_content);
        et_content.setHint("朋友，你的意见，将会让我们把产品做得更好。");
        final TextView tv_title = ButterKnife.findById(view, R.id.idea_title);
        tv_title.setText("意见反馈");
        final TextView tv_length = ButterKnife.findById(view, R.id.content_length);
        final Button comment_button = ButterKnife.findById(view, R.id.comit_button);
        comment_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String content = et_content.getText().toString().trim();
                if (TextUtils.isEmpty(content)) {
                    Toast.makeText(PortSettingActivity.this, "please input your feedback first !", Toast.LENGTH_LONG).show();
                    return;
                }
                //todu
            }
        });
        et_content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence == null) {
                    tv_length.setText("0/70");
                } else {
                    tv_length.setText(charSequence.toString().length() + "/70");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        dialog = new CustomDialog(this, view, ScreenUtils.getInstance(this).getWidth() - ContextUtils.dip2px(this, 20), ContextUtils.dip2px(this, 280), Gravity.CENTER);
        dialog.show();
    }

    /**
     * 显示状态图标
     */
    private void displayAppStatusIcon() {

        /**
         * 获取当前的sip状态
         */
        if (AppConfig.SIP_STATUS) {
            connetIcon.setBackgroundResource(R.mipmap.icon_connection_normal);
        } else {
            connetIcon.setBackgroundResource(R.mipmap.icon_connection_disable);
        }
        /**
         * 电池电量信息
         */
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
        /**
         * 信号 状态
         */
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
        }

    }

    /**
     * 显示当前的时间
     */
    private void displayCurrentTime() {
        Date date = new Date();
        String hoursStr = hoursFormat.format(date);
        if (isVisible) {
            currentTimeLayout.setText(hoursStr);
        }
    }

    /**
     * 申请修改系统信息的权限
     */
    private void checkPermission() {
        //6.0申请
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //判断是否已申请
            if (!Settings.System.canWrite(this)) {
                //如果没有修改系统的权限这请求修改系统的权限
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(intent, 0);
            }
        }
    }

    /**
     * 网络状态变化的监听
     *
     * @param state
     * @param name
     */
    @Override
    public void onNetChange(int state, String name) {

    }

    @Override
    protected void onResume() {
        displayAppStatusIcon();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        //停止刷新时间的标识
        threadIsRun = false;
        //移除handler监听
        if (handler != null)
            handler.removeCallbacksAndMessages(null);

        super.onDestroy();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    displayCurrentTime();
                    break;
                case 2: //显示sip正常连接状态
                    if (isVisible)
                        connetIcon.setBackgroundResource(R.mipmap.icon_connection_normal);
                    break;
                case 3:  //sip断开连接状态
                    if (isVisible)
                        connetIcon.setBackgroundResource(R.mipmap.icon_connection_disable);
                    break;
            }
        }
    };
}
