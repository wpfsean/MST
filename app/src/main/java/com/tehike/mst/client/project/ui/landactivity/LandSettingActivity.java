package com.tehike.mst.client.project.ui.landactivity;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.Switch;
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
import com.tehike.mst.client.project.services.BatteryAndWifiService;
import com.tehike.mst.client.project.sysinfo.SysinfoUtils;
import com.tehike.mst.client.project.ui.portactivity.PortMainActivity;
import com.tehike.mst.client.project.ui.widget.CustomDialog;
import com.tehike.mst.client.project.update.UpdateManager;
import com.tehike.mst.client.project.utils.ActivityUtils;
import com.tehike.mst.client.project.utils.BatteryUtils;
import com.tehike.mst.client.project.utils.ContextUtils;
import com.tehike.mst.client.project.utils.HttpUtils;
import com.tehike.mst.client.project.utils.Logutil;
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
 * 描述：横屏设置中心
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2018/11/14 14:54
 */

public class LandSettingActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener {

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
    @BindView(R.id.login_group_land_layout)
    LinearLayout loginParentLayout;

    /**
     * 登录信息的展开布局
     */
    @BindView(R.id.login_child_land_layout)
    LinearLayout loginChildLayout;

    /**
     * 视频设置
     */
    @BindView(R.id.video_group_land_layout)
    LinearLayout videoParentLayout;

    /**
     * 视频设置的子布局
     */
    @BindView(R.id.video_child_land_layout)
    LinearLayout videoChildLayout;

    /**
     * 报警设置
     */
    @BindView(R.id.alarm_group_land_layout)
    LinearLayout alarmParentLayout;

    /**
     * 报警设置的子布局
     */
    @BindView(R.id.alarm_child_land_layout)
    LinearLayout alarmChildLayout;

    /**
     * 弹箱设置
     */
    @BindView(R.id.box_group_land_layout)
    LinearLayout blueBoxParentLayout;

    /**
     * 弹箱设置的子布局
     */
    @BindView(R.id.box_child_land_layout)
    LinearLayout blueBoxChildLayout;

    /**
     * 系统设置
     */
    @BindView(R.id.system_setting_group_land_layout)
    LinearLayout systemParentLayout;

    /**
     * 系统设置的子布局
     */
    @BindView(R.id.system_setting_child_land_layout)
    LinearLayout systemChildLayout;

    /**
     * 常用 设置
     */
    @BindView(R.id.inuse_setting_group_land_layout)
    LinearLayout inuseParentLayout;

    /**
     * 常用 设置的布局
     */
    @BindView(R.id.inuse_setting_child_land_layout)
    LinearLayout inuseChildLayout;

    /**
     * 方向设置
     */
    @BindView(R.id.direction_setting_group_land_layout)
    LinearLayout directionParentLayout;

    /**
     * 方向设置的布局
     */
    @BindView(R.id.direction_setting_child_land_layout)
    LinearLayout directionChildLayout;

    /**
     * 关于我们
     */
    @BindView(R.id.about_group_land_layout)
    LinearLayout aboutParentLayout;

    /**
     * 是否记住密码
     */
    @BindView(R.id.land_switch_remember_pwd_layout)
    Switch switchRememberPwd;

    /**
     * 是否自动登录
     */
    @BindView(R.id.land_switch_autologin_layout)
    Switch switchAutoLogin;

    /**
     * Onvif解析是否是主码流
     */
    @BindView(R.id.land_switch_ismain_stream_layout)
    Switch switchOpenMainStream;

    /**
     * 监控视频是否播放声音
     */
    @BindView(R.id.land_switch_isopen_sound_layout)
    Switch switchOpenVoice;

    /**
     * 显示当前的电量
     */
    @BindView(R.id.prompt_electrity_values_land_layout)
    TextView displayCurrentBattery;

    /**
     * 时间格式
     */
    SimpleDateFormat hoursFormat = null;

    /**
     * 时间线程是否在运行标识
     */
    boolean threadIsRun = true;

    /**
     * 登录信息的子布局是否可见
     */
    boolean whetherLoginChildVisiable = true;

    /**
     * 视频监控的子布局是否可见
     */
    boolean whetherVideoChildVisiable = false;

    /**
     * 报警信息的子布局是否可见
     */
    boolean whetherAlarmChildVisiable = false;

    /**
     * 弹箱设置的子布局是否可见
     */
    boolean whetherblueBoxChildVisiable = false;

    /**
     * 系统设置的子布局是否可见
     */
    boolean whetherSystemChildVisiable = false;

    /**
     * 常用设置的子布局是否可见
     */
    boolean whetherInuseChildVisiable = false;

    /**
     * 方向设置的子布局是否可见
     */
    boolean whetherdirectionChildVisiable = false;


    @Override
    protected int intiLayout() {
        return R.layout.activity_land_setting_layout;
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {

        //显示当前的时间
        initializeTime();

        //初始化view
        initializeView();

        //初始化数据
        initializeData();
    }

    /**
     * 初始化view
     */
    private void initializeView() {
        //显示登录信息的子布局项
        loginChildLayout.setVisibility(View.VISIBLE);
        //隐藏视频监控的子项
        videoChildLayout.setVisibility(View.GONE);
        //隐藏报警设置的子项
        alarmChildLayout.setVisibility(View.GONE);
        //隐藏弹箱设置的子项
        blueBoxChildLayout.setVisibility(View.GONE);
        //隐藏系统设置的子项
        systemChildLayout.setVisibility(View.GONE);
        //隐藏常用设置的子项
        inuseChildLayout.setVisibility(View.GONE);
        //隐藏方向设置的子项
        directionChildLayout.setVisibility(View.GONE);
        //就否记住密码
        switchRememberPwd.setOnCheckedChangeListener(this);
        //是否自动 登录
        switchAutoLogin.setOnCheckedChangeListener(this);
        //是否打开主码流
        switchOpenMainStream.setOnCheckedChangeListener(this);
        //是否开启声音
        switchOpenVoice.setOnCheckedChangeListener(this);
    }

    /**
     * 初始化数据
     */
    private void initializeData() {
        //是否正在登录
        boolean isAutoLogin = (boolean) SharedPreferencesUtils.getObject(LandSettingActivity.this, "autologin", false);
        if (isAutoLogin) {
            switchAutoLogin.setChecked(true);
        } else {
            switchAutoLogin.setChecked(false);
        }
        //是否记住密码
        boolean isrePwd = (boolean) SharedPreferencesUtils.getObject(LandSettingActivity.this, "isremember", false);
        if (isrePwd) {
            switchRememberPwd.setChecked(true);
        } else {
            switchRememberPwd.setChecked(false);
        }
        //是否设置声音开启
        boolean isOpenSound = AppConfig.ISVIDEOSOUNDS;
        if (isOpenSound) {
            switchOpenVoice.setChecked(true);
        } else {
            switchOpenVoice.setChecked(false);
        }
        //是否设置onvif解析取主码流
        boolean isMainStream = AppConfig.IS_MAIN_STREAM;
        if (isMainStream) {
            switchOpenMainStream.setChecked(true);
        } else {
            switchOpenMainStream.setChecked(false);
        }
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
        View rootview = LayoutInflater.from(LandSettingActivity.this).inflate(R.layout.activity_land_setting_layout, null);
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
        View rootview = LayoutInflater.from(LandSettingActivity.this).inflate(R.layout.activity_land_setting_layout, null);
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
     * 自定义的dialog
     */
    private CustomDialog dialog;

    /**
     * 提交意见 反馈的弹窗
     */
    private void displayCommentDialog() {

        //弹 窗布局
        View view = LayoutInflater.from(LandSettingActivity.this).inflate(R.layout.send_comment_dialog_port, null);
        //输入框
        final EditText editText = ButterKnife.findById(view, R.id.et_content);
        editText.setHint("朋友，你的意见，将会让我们把产品做得更好。");
        //标题
        final TextView title = ButterKnife.findById(view, R.id.idea_title);
        title.setText("意见反馈");
        //字数
        final TextView contentLength = ButterKnife.findById(view, R.id.content_length);
        final Button commitBtn = ButterKnife.findById(view, R.id.comit_button);
        //确认按钮
        commitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String content = editText.getText().toString().trim();
                if (TextUtils.isEmpty(content)) {
                    Toast.makeText(LandSettingActivity.this, "please input your feedback first !", Toast.LENGTH_LONG).show();
                    return;
                }
                new HttpUtils("http://19.0.0.20/RecordTheNumForData/a.php?paramater=" + content + "&nativeIpAddrees=19.0.0.79", new HttpUtils.GetHttpData() {
                    @Override
                    public void httpData(String result) {
                    }
                }).start();

                dialog.dismiss();
            }
        });
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence == null) {
                    contentLength.setText("0/70");
                } else {
                    contentLength.setText(charSequence.toString().length() + "/70");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        dialog = new CustomDialog(this, view, ScreenUtils.getInstance(this).getWidth() - ContextUtils.dip2px(this, 220), ContextUtils.dip2px(this, 320), Gravity.CENTER);
        dialog.show();
    }

    /**
     * 网络状态的变化回调
     *
     * @param state
     * @param name
     */
    @Override
    public void onNetChange(int state, String name) {

    }

    /**
     * 点击事件处理
     *
     * @param view
     */
    @OnClick({R.id.logout_port_layout, R.id.prompt_feedback_land_layout, R.id.update_apk_land_layout, R.id.set_lightness_land_layout, R.id.reset_setting_land_layout, R.id.save_setting_land_layout, R.id.back_setting_land_layout, R.id.refresh_setting_land_layout, R.id.setting_horizontal_direction, R.id.app_clear_cach_land_layout, R.id.setting_alarm_ip_land_layout, R.id.display_account_infor_land_layout, R.id.display_pwd_infor_land_layout, R.id.login_group_land_layout, R.id.video_group_land_layout, R.id.alarm_group_land_layout, R.id.box_group_land_layout, R.id.system_setting_group_land_layout, R.id.inuse_setting_group_land_layout, R.id.direction_setting_group_land_layout})
    public void onclickEvent(View view) {
        switch (view.getId()) {
            case R.id.logout_port_layout://退出登录
                ActivityUtils.removeAllActivity();
                finish();
                if (SipManager.getLc() != null) {
                    SipManager.getLc().clearProxyConfigs();
                }
                App.exit();
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(0);
                break;
            case R.id.prompt_feedback_land_layout://意见反馈
                displayCommentDialog();
                break;
            case R.id.update_apk_land_layout://更新apk
                new UpdateManager(LandSettingActivity.this).checkUpdate();
                break;
            case R.id.set_lightness_land_layout://调节亮度
                displaySetAppLightnessDialog();
                break;
            case R.id.back_setting_land_layout://返回键
                ActivityUtils.removeActivity(this);
                this.finish();
                break;
            case R.id.refresh_setting_land_layout://设置已刷新
                if (isVisible)
                    showProgressSuccess("设置已更新");
                break;
            case R.id.reset_setting_land_layout://恢复设置
                displayReBootSetDialog();

                break;
            case R.id.save_setting_land_layout://保存设置
                ActivityUtils.removeActivity(this);
                this.finish();
                break;
            case R.id.display_account_infor_land_layout://显示当前帐号信息
                displayAccountDialog();
                break;
            case R.id.display_pwd_infor_land_layout://显示密码信息
                displayPwdDialog();
                break;
            case R.id.setting_alarm_ip_land_layout://修改报警信息
                displaySetAlarmDialog();
                break;
            case R.id.app_clear_cach_land_layout://清除缓存
                if (isVisible) {
                    showProgressSuccess("已完成！");
                }
                break;
            case R.id.setting_horizontal_direction://竖屏设置
                ActivityUtils.removeAllActivity();
                this.finish();
                AppConfig.APP_DIRECTION = 1;
                startActivity(new Intent(LandSettingActivity.this, PortMainActivity.class));
                break;

            case R.id.login_group_land_layout://点击登录布局关闭或打开子布局

                Logutil.i("点击了");

                if (whetherLoginChildVisiable == false) {
                    whetherLoginChildVisiable = true;
                    loginChildLayout.setVisibility(View.VISIBLE);
                } else if (whetherLoginChildVisiable == true) {
                    whetherLoginChildVisiable = false;
                    loginChildLayout.setVisibility(View.GONE);
                }
                break;
            case R.id.video_group_land_layout://点击视频监控布局关闭或打开子布局
                if (whetherVideoChildVisiable == false) {
                    whetherVideoChildVisiable = true;
                    videoChildLayout.setVisibility(View.VISIBLE);
                } else if (whetherVideoChildVisiable == true) {
                    whetherVideoChildVisiable = false;
                    videoChildLayout.setVisibility(View.GONE);
                }
                break;
            case R.id.alarm_group_land_layout://点击报警布局关闭或打开子布局
                if (whetherAlarmChildVisiable == false) {
                    whetherAlarmChildVisiable = true;
                    alarmChildLayout.setVisibility(View.VISIBLE);
                } else if (whetherAlarmChildVisiable == true) {
                    whetherAlarmChildVisiable = false;
                    alarmChildLayout.setVisibility(View.GONE);
                }
                break;

            case R.id.box_group_land_layout://点击弹箱设置布局关闭或打开子布局
                if (whetherblueBoxChildVisiable == false) {
                    whetherblueBoxChildVisiable = true;
                    blueBoxChildLayout.setVisibility(View.VISIBLE);
                } else if (whetherblueBoxChildVisiable == true) {
                    whetherblueBoxChildVisiable = false;
                    blueBoxChildLayout.setVisibility(View.GONE);
                }
                break;

            case R.id.system_setting_group_land_layout://点击系统设置布局关闭或打开子布局
                if (whetherSystemChildVisiable == false) {
                    whetherSystemChildVisiable = true;
                    systemChildLayout.setVisibility(View.VISIBLE);
                } else if (whetherSystemChildVisiable == true) {
                    whetherSystemChildVisiable = false;
                    systemChildLayout.setVisibility(View.GONE);
                }
                break;
            case R.id.inuse_setting_group_land_layout://点击常用设置布局关闭或打开子布局
                if (whetherInuseChildVisiable == false) {
                    whetherInuseChildVisiable = true;
                    inuseChildLayout.setVisibility(View.VISIBLE);
                } else if (whetherInuseChildVisiable == true) {
                    whetherInuseChildVisiable = false;
                    inuseChildLayout.setVisibility(View.GONE);
                }
                break;
            case R.id.direction_setting_group_land_layout:
                if (whetherdirectionChildVisiable == false) {
                    whetherdirectionChildVisiable = true;
                    directionChildLayout.setVisibility(View.VISIBLE);
                } else if (whetherdirectionChildVisiable == true) {
                    whetherdirectionChildVisiable = false;
                    directionChildLayout.setVisibility(View.GONE);
                }
                break;
        }
    }

    /**
     * 设置app亮度
     */
    private void displaySetAppLightnessDialog() {

        View view = LayoutInflater.from(this).inflate(R.layout.prompt_set_lightness_layout, null);
        //显示当前亮度值的控件
        final TextView promptLightnessValues = view.findViewById(R.id.prompt_current_lighness_values_layout);

        SeekBar lumninanceBar = view.findViewById(R.id.prompt_current_lighness_seekbar_layout);
        //popuwindow显示
        final PopupWindow popu = new PopupWindow(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        popu.setContentView(view);
        //显示所在的位置
        View rootview = LayoutInflater.from(LandSettingActivity.this).inflate(R.layout.activity_land_setting_layout, null);
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
     * 设置报警ip
     */
    private void displaySetAlarmDialog() {
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
        View rootview = LayoutInflater.from(LandSettingActivity.this).inflate(R.layout.activity_land_setting_layout, null);
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
                if (popu.isShowing()) {
                    popu.dismiss();
                }
            }
        });
    }

    /**
     * 提示恢复设置
     */
    private void displayReBootSetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LandSettingActivity.this);
        builder.setTitle("恢复设置").setMessage("确定要恢复设置？").setNegativeButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //dialog消息
                dialog.dismiss();
                //清除本李缓存的所有的数据
                SharedPreferencesUtils.clear(App.getApplication());
                //退出本应用
                logoutApp();
                Intent intent = new Intent();
                intent.setClass(LandSettingActivity.this, LandLoginActivity.class);
                LandSettingActivity.this.startActivity(intent);

            }
        }).setPositiveButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();
    }

    /**
     * 退出登录
     */
    private void logoutApp() {
        ActivityUtils.removeAllActivity();
        finish();
    }

    /**
     * 显示当前的时间
     */
    private void initializeTime() {
        //启动线程每秒刷新一下
        TimingThread timeThread = new TimingThread();
        new Thread(timeThread).start();
        //显示当前的年月日
        Date date = new Date();
        //时间格式
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy年MM月dd日");
        hoursFormat = new SimpleDateFormat("HH:mm:ss");
        String currntYearDate = yearFormat.format(date);
        //显示当前的年月日
        if (!TextUtils.isEmpty(currntYearDate))
            currentYearLayout.setText(currntYearDate);
        //显示当前的电量
        int electricityValues = BatteryUtils.getSystemBattery(App.getApplication());
        displayCurrentBattery.setText(electricityValues + "");

    }

    /**
     * switch开关事件
     *
     * @param buttonView
     * @param isChecked
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            //关闭或打开记住密码
            case R.id.land_switch_remember_pwd_layout:
                if (isChecked) {
                    SharedPreferencesUtils.putObject(LandSettingActivity.this, "isremember", true);
                } else {
                    SharedPreferencesUtils.putObject(LandSettingActivity.this, "isremember", false);
                }
                break;
            //关闭或打开是否自动 登录
            case R.id.land_switch_autologin_layout:
                if (isChecked) {
                    SharedPreferencesUtils.putObject(LandSettingActivity.this, "autologin", true);
                } else {
                    SharedPreferencesUtils.putObject(LandSettingActivity.this, "autologin", false);
                }
                break;
            //关闭或打开是否解析主码流
            case R.id.land_switch_ismain_stream_layout:
                if (isChecked) {
                    AppConfig.IS_MAIN_STREAM = true;
                } else {
                    AppConfig.IS_MAIN_STREAM = false;
                }
                break;
            //关闭或打开是否播放声音
            case R.id.land_switch_isopen_sound_layout:
                if (isChecked) {
                    AppConfig.ISVIDEOSOUNDS = true;
                } else {
                    AppConfig.ISVIDEOSOUNDS = false;
                }
                break;
        }
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
     * 显示当前的头部状态
     */
    private void displayAppStatus() {

        /**
         * 获取当前的sip状态
         */
        if (AppConfig.SIP_STATUS) {
            connetIcon.setBackgroundResource(R.mipmap.icon_connection_normal);
        } else {
            connetIcon.setBackgroundResource(R.mipmap.icon_connection_disable);
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

    @Override
    protected void onResume() {
        displayAppStatus();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        //停止刷新时间的标识
        threadIsRun = false;
        //handler移除所有的监听
        if (handler != null)
            handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    //显示当前的时间
                    displayCurrentTime();
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
                case 23:
                    //显示当前的电量信息
                    int level = msg.arg1;
                    if (isVisible) {
                        displayCurrentBattery.setText("" + level);
                    }
                    break;
            }
        }
    };
}
