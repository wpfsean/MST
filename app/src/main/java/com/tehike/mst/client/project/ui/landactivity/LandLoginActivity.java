package com.tehike.mst.client.project.ui.landactivity;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tehike.mst.client.project.R;
import com.tehike.mst.client.project.base.App;
import com.tehike.mst.client.project.base.BaseActivity;
import com.tehike.mst.client.project.global.AppConfig;
import com.tehike.mst.client.project.services.LocationService;
import com.tehike.mst.client.project.services.ServiceUtils;
import com.tehike.mst.client.project.sysinfo.SysInfoBean;
import com.tehike.mst.client.project.sysinfo.SysinfoUtils;
import com.tehike.mst.client.project.ui.portactivity.PortLoginActivity;
import com.tehike.mst.client.project.ui.portactivity.PortMainActivity;
import com.tehike.mst.client.project.utils.ActivityUtils;
import com.tehike.mst.client.project.utils.BatteryUtils;
import com.tehike.mst.client.project.utils.CryptoUtil;
import com.tehike.mst.client.project.utils.FileUtil;
import com.tehike.mst.client.project.utils.GsonUtils;
import com.tehike.mst.client.project.utils.Logutil;
import com.tehike.mst.client.project.utils.NetworkUtils;
import com.tehike.mst.client.project.utils.SharedPreferencesUtils;
import com.tehike.mst.client.project.utils.StringUtils;
import com.tehike.mst.client.project.utils.WriteLogToFile;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 描述：横屏登录页面（通过获取VideoResources协议的长度判断是否登录成功）
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2018/10/18 10:38
 */

public class LandLoginActivity extends BaseActivity {

    /**
     * 无网络时提示
     */
    @BindView(R.id.no_network_layout)
    RelativeLayout noNetWorkShow;

    //进度动画
    @BindView(R.id.image_loading)
    ImageView loadingImageView;

    //登录错误信息提示
    @BindView(R.id.loin_error_infor_layout)
    TextView promptErrorInforTextView;

    //用户名
    @BindView(R.id.edit_username_layout)
    EditText editUserName;

    //密码
    @BindView(R.id.edit_userpass_layout)
    EditText editUserPwd;

    //记住密码Checkbox
    @BindView(R.id.remember_pass_layout)
    Checkable checkRememberPwd;

    //自动登录CheckBox
    @BindView(R.id.auto_login_layout)
    Checkable checkAutoLogin;

    //服务器
    @BindView(R.id.edit_serviceip_layout)
    EditText editServerIp;

    //修改服务器的checkbox
    @BindView(R.id.remembe_serverip_layout)
    CheckBox checkUpdateServerIp;

    /**
     * 加载时的动画
     */
    Animation mLoadingAnim;

    /**
     * 用户是否禁止权限
     */
    boolean mShowRequestPermission = true;

    /**
     * 需要申请的权限
     */
    String[] allPermissionList = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    /**
     * 存放未同同意的权限
     */
    List<String> noAgreePermissions = new ArrayList<>();


    @Override
    protected int intiLayout() {
        return R.layout.activity_land_login;
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {


        //申请所需要的权限
        checkAllPermissions();
    }

    /**
     * 申请权限
     */
    private void checkAllPermissions() {
        //判断是否需要申请权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            noAgreePermissions.clear();

            //遍历申请
            for (String permission : allPermissionList) {
                if (ContextCompat.checkSelfPermission(LandLoginActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
                    noAgreePermissions.add(permission);
                }
            }
            //如果存在继续申请
            if (!noAgreePermissions.isEmpty()) {
                //将List转为数组
                String[] permissions = noAgreePermissions.toArray(new String[noAgreePermissions.size()]);
                ActivityCompat.requestPermissions(LandLoginActivity.this, permissions, 1);
            } else {
                //初始化参数
                initParamaters();
            }
        } else {
            //初始化参数
            initParamaters();
        }
    }

    /**
     * 初始化数据
     */
    private void initParamaters() {

        //动画
        mLoadingAnim = AnimationUtils.loadAnimation(this, R.anim.loading);

        //申请系统权限
        checkSystemPermissions();

        //启动定位服务（获取经）
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) && (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            if (!ServiceUtils.isServiceRunning(LocationService.class))
                ServiceUtils.startService(LocationService.class);
        }

        //判断是否自动登录
        boolean isAutoLogin = (boolean) SharedPreferencesUtils.getObject(LandLoginActivity.this, "autologin", false);
        if (isAutoLogin) {
            loginSuccessToCms();
        }

        boolean isrePwd = (boolean) SharedPreferencesUtils.getObject(LandLoginActivity.this, "isremember", false);
        if (isrePwd) {
            checkRememberPwd.setChecked(true);
            //填充用户名框
            String db_name = SysinfoUtils.getUserName();
            if (!TextUtils.isEmpty(db_name)) {
                editUserName.setText(db_name);
            }
            //填充密码框
            String db_pwd = SysinfoUtils.getUserPwd();
            if (!TextUtils.isEmpty(db_pwd)) {
                editUserPwd.setText(db_pwd);
            }
            //填充服务器地址框
            String db_server = SysinfoUtils.getServerIp();
            if (!TextUtils.isEmpty(db_server)) {
                editServerIp.setText(db_server);
                editServerIp.setEnabled(false);
            }
            //修改服务器地址
            checkUpdateServerIp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        editServerIp.setEnabled(true);
                    } else {
                        editServerIp.setEnabled(false);
                    }
                }
            });
        }
    }

    /**
     * 申请系统权限（允许弹窗，修改系统亮度）
     */
    private void checkSystemPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(LandLoginActivity.this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(intent, 2);
            } else {
                AppConfig.ARGEE_OVERLAY_PERMISSION = true;
            }
        } else {
            AppConfig.ARGEE_OVERLAY_PERMISSION = false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(this)) {
                //如果没有修改系统的权限这请求修改系统的权限
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(intent, 3);
            } else {
                AppConfig.ARGEE_WRITE_SETTINGS = true;
            }
        } else {
            AppConfig.ARGEE_WRITE_SETTINGS = false;
        }
    }


    //是否点击过了登录按键
    boolean isLoginingFlag = false;
    //获取输入框内的用户名
    String name = "";
    //获取输入框内的密码
    String pass = "";
    //获取输入框内的服务器地址
    String server_IP = "";
    //是否记住密码
    boolean isRememberPwd;
    //是否自动 登录
    boolean isAutoLogin;
    //tcp请求cms数据
    Socket loginCmsTcp = null;

    /**
     * 登录
     */
    @OnClick(R.id.userlogin_button_layout)
    public void loginCMS(View view) {
        promptErrorInforTextView.setText("");
        //防止点击过快
        //获取当前输入框内的内容
        name = editUserName.getText().toString().trim();
        pass = editUserPwd.getText().toString().trim();
        server_IP = editServerIp.getText().toString().trim();
        //判断信息是否齐全
        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(pass) && !TextUtils.isEmpty(server_IP)) {
            //判断是否有网
            if (NetworkUtils.isConnected()) {
                //加载动画显示
                loadingImageView.setVisibility(View.VISIBLE);
                loadingImageView.startAnimation(mLoadingAnim);
                //正则表达
                if (!NetworkUtils.isboolIp(server_IP)) {
                    handler.sendEmptyMessage(6);
                    return;
                }
                //判断是否正在登录
                if (!isLoginingFlag) {
                    TcpLoginCmsThread thread = new TcpLoginCmsThread(name, pass, server_IP);
                    new Thread(thread).start();
                    isLoginingFlag = true;
                } else {
                    //取消登录，并关闭socket
                    isLoginingFlag = false;
                    if (loginCmsTcp != null) {
                        try {
                            loginCmsTcp.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        loginCmsTcp = null;
                    }
                    //提示取消登录
                    handler.sendEmptyMessage(9);
                }
            } else {
                //提示无网络
                handler.sendEmptyMessage(3);
            }
        } else {
            //提示信息缺失
            handler.sendEmptyMessage(5);
        }
    }

    /**
     * 子线程验证webapi的Sysinfo接口
     */
    class TcpLoginCmsThread extends Thread {

        String name;
        String pwd;
        String serverIp;

        //构造函数
        public TcpLoginCmsThread(String name, String pwd, String serverIp) {
            this.name = name;
            this.pwd = pwd;
            this.serverIp = serverIp;
        }

        @Override
        public void run() {
            synchronized (this) {
                try {
                    String requestLoginUrl = AppConfig.WEB_HOST + serverIp + AppConfig._SYSINFO;
                    HttpURLConnection con = (HttpURLConnection) new URL(requestLoginUrl).openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(3000);
                    String authString = name + ":" + pwd;
                    con.setRequestProperty("Authorization", "Basic " + new String(Base64.encode(authString.getBytes(), 0)));
                    con.connect();
                    if (con.getResponseCode() == 200) {
                        InputStream in = con.getInputStream();
                        String result = StringUtils.readTxt(in);
                        if (TextUtils.isEmpty(result)) {
                            handler.sendEmptyMessage(8);
                            Logutil.e("Sysinfo接口返回数据为空--->>" + result);
                            return;
                        }
                        Message returnLoginMess = new Message();
                        returnLoginMess.obj = result;
                        returnLoginMess.what = 11;
                        handler.sendMessage(returnLoginMess);
                    } else {
                        Logutil.e("Sysinfo接口返回非200" + con.getResponseCode());
                        handler.sendEmptyMessage(8);
                    }
                    con.disconnect();
                } catch (Exception e) {

                }
            }
        }
    }

    /**
     * 按home键时保存当前的输入状态
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("name", editUserName.getText().toString().trim());
        outState.putString("pass", editUserPwd.getText().toString().trim());
        outState.putString("serverip", editServerIp.getText().toString().trim());
    }

    /**
     * 恢复刚才的输入状态
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        editUserName.setText(savedInstanceState.getString("name"));
        editUserPwd.setText(savedInstanceState.getString("pass"));
        editServerIp.setText(savedInstanceState.getString("serverip"));
    }

    @Override
    public void onNetChange(int state, String name) {

    }

    /**
     * 处理登录 接口的sysinfo数据
     *
     */
    private void handlerSysinfoData(String result) {
        try {
            JSONObject jsonObject = new JSONObject(result);
            if (jsonObject != null) {
                SysInfoBean sysInfoBean = new SysInfoBean(jsonObject.getInt("alertPort"),
                        jsonObject.getString("alertServer"), jsonObject.getString("deviceGuid"),
                        jsonObject.getString("deviceName"), jsonObject.getInt("fingerprintPort"),
                        jsonObject.getString("fingerprintServer"), jsonObject.getInt("heartbeatPort"),
                        jsonObject.getString("heartbeatServer"), jsonObject.getString("sipPassword"),
                        jsonObject.getString("sipServer"), jsonObject.getString("sipUsername"),
                        jsonObject.getInt("webresourcePort"), jsonObject.getString("webresourceServer"), jsonObject.getInt("neighborWatchPort"));

                //把Sysinfo数据写入文件

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                    FileUtil.writeFile(CryptoUtil.encodeBASE64(GsonUtils.GsonString(sysInfoBean)), AppConfig.SYSINFO);
                else
                    WriteLogToFile.info("写入sysinfo时没开启WRITE_EXTERNAL_STORAGE权限");
                //登录成功
                handler.sendEmptyMessage(10);
            }
        } catch (Exception e) {
            WriteLogToFile.info("解析Sysinfo数据异常");
            Logutil.e("解析Sysinfo数据异常--->>>" + e.getMessage());
        }
    }

    /**
     * 登录成功
     */
    private void LoginSuccessMethond() {
        //获取记住密码的状态
        isRememberPwd = checkRememberPwd.isChecked();
        //获取自动登录的状态
        isAutoLogin = checkAutoLogin.isChecked();
        //判断当前是否记住密码，如果记住密码就把配置信息提前插入数据库
        if (isRememberPwd) {
            //保存记住密码的状态
            SharedPreferencesUtils.putObject(LandLoginActivity.this, "isremember", isRememberPwd);
        }
        //保存自动登录的状态
        if (isAutoLogin) {
            SharedPreferencesUtils.putObject(LandLoginActivity.this, "autologin", isRememberPwd);
        }

        SharedPreferencesUtils.putObject(LandLoginActivity.this, "serverIp", server_IP);
        SharedPreferencesUtils.putObject(LandLoginActivity.this, "userPwd", pass);
        SharedPreferencesUtils.putObject(LandLoginActivity.this, "userName", name);
        //赋值给常量
        AppConfig.USERNAME = name;
        AppConfig.PWD = pass;
        AppConfig.SERVERIP = server_IP;
        //延迟半秒后登录成功（取消动画的加载状态）
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loginSuccessToCms();
            }
        }, 500);
    }

    /**
     * 成功登录cms（cms验证通过）
     */
    public void loginSuccessToCms() {
        handler.sendEmptyMessage(4);
        //跳转到主页面并finish本页面
        Intent intent = new Intent(LandLoginActivity.this, LandMainActivity.class);
        LandLoginActivity.this.startActivity(intent);
        ActivityUtils.removeActivity(LandLoginActivity.this);
        LandLoginActivity.this.finish();
    }

    /**
     * 权限申请回调
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        //判断是否勾选禁止后不再询问
                        boolean showRequestPermission = ActivityCompat.shouldShowRequestPermissionRationale(LandLoginActivity.this, permissions[i]);
                        if (showRequestPermission) {
                            //重新申请权限
                            checkAllPermissions();
                            return;
                        } else {
                            //已经禁止
                            mShowRequestPermission = false;
                            String permisson = permissions[i];
                            Logutil.e("未授予的权限:" + permisson);
                            WriteLogToFile.info("用户禁止申请以下的权限:" + permisson);
                        }
                    }
                }
                //初始化参数
                initParamaters();
                break;
            default:
                break;
        }
    }

    /**
     * Activity的回调
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 2:
                //悬浮窗口权限
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Settings.canDrawOverlays(this)) {
                        AppConfig.ARGEE_OVERLAY_PERMISSION = true;
                    } else {
                        AppConfig.ARGEE_OVERLAY_PERMISSION = false;
                    }
                }
                break;
            case 3:
                //系统设置权限
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Settings.System.canWrite(this)) {
                        AppConfig.ARGEE_WRITE_SETTINGS = true;
                    } else {
                        AppConfig.ARGEE_WRITE_SETTINGS = false;
                    }
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {

        if (mLoadingAnim != null)
            mLoadingAnim = null;

        if (handler != null)
            handler.removeCallbacksAndMessages(null);

        super.onDestroy();
    }

    /**
     * Handler处理子线程发送的消息
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 3:
                    //提示网络不可用
                    promptErrorInforTextView.setVisibility(View.VISIBLE);
                    promptErrorInforTextView.setText("网络不可用!");
                    break;
                case 4:
                    //登录成功
                    loadingImageView.setVisibility(View.GONE);
                    loadingImageView.clearAnimation();
                    promptErrorInforTextView.setVisibility(View.VISIBLE);
                    promptErrorInforTextView.setText("");
                    break;
                case 5:
                    //登录信息缺失
                    loadingImageView.setVisibility(View.GONE);
                    loadingImageView.clearAnimation();
                    promptErrorInforTextView.setVisibility(View.VISIBLE);
                    promptErrorInforTextView.setText("信息缺失");
                    break;
                case 6:
                    //ip不合法
                    promptErrorInforTextView.setVisibility(View.VISIBLE);
                    promptErrorInforTextView.setText("服务器ip不合法!");
                    loadingImageView.setVisibility(View.GONE);
                    loadingImageView.clearAnimation();
                    break;
                case 7:
                    //ip不合法
                    promptErrorInforTextView.setVisibility(View.VISIBLE);
                    promptErrorInforTextView.setText("登录未获取本机Ip!");
                    loadingImageView.setVisibility(View.GONE);
                    loadingImageView.clearAnimation();
                    break;
                case 8:
                    //登录失败
                    promptErrorInforTextView.setVisibility(View.VISIBLE);
                    promptErrorInforTextView.setText("登录失败");
                    loadingImageView.setVisibility(View.GONE);
                    loadingImageView.clearAnimation();
                    break;
                case 9:
                    //取消登录
                    promptErrorInforTextView.setVisibility(View.VISIBLE);
                    promptErrorInforTextView.setText("取消登录");
                    loadingImageView.setVisibility(View.GONE);
                    loadingImageView.clearAnimation();
                    break;
                case 10:
                    //登录成功
                    LoginSuccessMethond();
                    break;
                case 11:
                    String result = (String) msg.obj;
                    handlerSysinfoData(result);
                    break;
            }
        }
    };
}

