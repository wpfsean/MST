package com.tehike.mst.client.project.base;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.tehike.mst.client.project.R;
import com.tehike.mst.client.project.receiver.NetChangedReceiver;
import com.tehike.mst.client.project.ui.widget.NetworkStateView;
import com.tehike.mst.client.project.utils.ActivityUtils;
import com.tehike.mst.client.project.utils.ProgressDialogUtils;

import java.lang.reflect.Method;

import butterknife.ButterKnife;
import butterknife.Unbinder;


/**
 * 描述：简单的BaseActivity父类，用于子线继承
 * <p>
 * 网络状态变化的监听回调
 * ProgressBar的提示
 * ButterKnife的注解
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2018/12/28 9:01
 */

public abstract class BaseActivity extends AppCompatActivity implements NetworkStateView.OnRefreshListener, NetChangedReceiver.NetStatusChangeEvent {

    /**
     * 一个unbinder合同，在调用时将取消绑定视图
     */
    private Unbinder unbinder;

    /**
     * Dialog工具类
     */
    private ProgressDialogUtils progressDialog;

    /**
     * 网络监听
     */
    private NetChangedReceiver mNetReceiver;

    /**
     * 网编状态的view
     */
    private NetworkStateView networkStateView;

    /**
     * 网络状态改变回调
     */
    public static NetChangedReceiver.NetStatusChangeEvent event;

    /**
     * 电量管理类
     */
    PowerManager powerManager;

    /**
     * 控制屏幕亮度时间的锁
     */
    PowerManager.WakeLock mWakeLock;

    /**
     * 当前页面是否可见
     */
    public boolean isVisible = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //回调赋值
        event = this;

        //隐藏状态栏
        hideSystemBar();

        //判断是否有虚拟按键
        if (checkDeviceHasNavigationBar(this)) {
            //隐藏
            hideBottomUIMenu();
        }

        //设置布局
        setContentView(intiLayout());

        //添加注解
        unbinder = ButterKnife.bind(this);

        //网络状态变化广播监听
        initNetworkStatuChangeReceiver();

        //activity加入栈中
        ActivityUtils.addActivity(this);

        //初始化dialog
        initDialog();

        afterCreate(savedInstanceState);

        //电源锁屏
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (powerManager != null) {
            mWakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "WakeLock");
        }
    }

    /**
     * 隐藏状态栏
     */
    private void hideSystemBar() {
        //去除Title
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        //全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        //如果有状态栏就隐藏
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) actionBar.hide();
    }

    /**
     * 隐藏虚拟按键
     */
    protected void hideBottomUIMenu() {
        //隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    /**
     * 判断是否有虚拟按键
     */
    public static boolean checkDeviceHasNavigationBar(Context context) {
        boolean hasNavigationBar = false;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            hasNavigationBar = rs.getBoolean(id);
        }
        try {
            Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method m = systemPropertiesClass.getMethod("get", String.class);
            String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                hasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                hasNavigationBar = true;
            }
        } catch (Exception e) {
        }
        return hasNavigationBar;
    }

    @SuppressLint("InflateParams")
    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        View view = getLayoutInflater().inflate(R.layout.base_activity, null);

        //设置填充activity_base布局
        super.setContentView(view);

        //再次设置全屏
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            view.setFitsSystemWindows(true);
        }
        //加载子类Activity的布局
        initDefaultView(layoutResID);
    }

    /**
     * 初始化默认布局的View
     */
    private void initDefaultView(int layoutResId) {
        networkStateView = (NetworkStateView) findViewById(R.id.nsv_state_view);
        FrameLayout container = (FrameLayout) findViewById(R.id.fl_activity_child_container);
        View childView = LayoutInflater.from(this).inflate(layoutResId, null);
        container.addView(childView, 0);
    }

    /**
     * 页面跳转
     */
    public void openActivity(Class cls) {
        startActivity(new Intent(this, cls));
    }

    /**
     * 页面跳转并结束本页面
     */
    public void openActivityAndCloseThis(Class cls) {
        startActivity(new Intent(this, cls));
        finish();
    }

    protected abstract int intiLayout();

    protected abstract void afterCreate(Bundle savedInstanceState);

    /**
     * 初始化Dialog
     */
    private void initDialog() {
        progressDialog = new ProgressDialogUtils(this, R.style.dialog_transparent_style);
    }

    /**
     * 显示加载中的布局
     */
    public void showLoadingView() {
        networkStateView.showLoading();
    }

    /**
     * 显示加载完成后的布局(即子类Activity的布局)
     */
    public void showContentView() {
        networkStateView.showSuccess();
    }

    /**
     * 显示没有网络的布局
     */
    public void showNoNetworkView() {
        networkStateView.showNoNetwork();
        networkStateView.setOnRefreshListener(this);
    }

    /**
     * 显示没有数据的布局
     */
    public void showEmptyView() {
        networkStateView.showEmpty();
        networkStateView.setOnRefreshListener(this);
    }

    /**
     * 显示数据错误，网络错误等布局
     */
    public void showErrorView() {
        networkStateView.showError();
        networkStateView.setOnRefreshListener(this);
    }

    @Override
    public void onRefresh() {
        onNetworkViewRefresh();
    }

    /**
     * 重新请求网络
     */
    public void onNetworkViewRefresh() {
    }

    /**
     * 显示加载的ProgressDialog
     */
    public void showProgressDialog() {
        progressDialog.showProgressDialog();
    }

    /**
     * 显示有加载文字ProgressDialog，文字显示在ProgressDialog的下面
     */
    public void showProgressDialogWithText(String text) {
        progressDialog.showProgressDialogWithText(text);
    }

    /**
     * 显示加载成功的ProgressDialog，文字显示在ProgressDialog的下面
     */
    public void showProgressSuccess(String message, long time) {
        progressDialog.showProgressSuccess(message, time);
    }

    /**
     * 显示加载成功的ProgressDialog，文字显示在ProgressDialog的下面
     * ProgressDialog默认消失时间为1秒(1000毫秒)
     */
    public void showProgressSuccess(String message) {
        progressDialog.showProgressSuccess(message);
    }

    /**
     * 显示加载失败的ProgressDialog，文字显示在ProgressDialog的下面
     */
    public void showProgressFail(String message, long time) {
        progressDialog.showProgressFail(message, time);
    }

    /**
     * 显示加载失败的ProgressDialog，文字显示在ProgressDialog的下面
     * ProgressDialog默认消失时间为1秒(1000毫秒)
     */
    public void showProgressFail(String message) {
        progressDialog.showProgressFail(message);
    }

    /**
     * 隐藏加载的ProgressDialog
     */
    public void dismissProgressDialog() {
        progressDialog.dismissProgressDialog();
    }

    /**
     * 添加网络状态变化广播
     */
    private void initNetworkStatuChangeReceiver() {
        mNetReceiver = new NetChangedReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mNetReceiver, intentFilter);
    }

    /**
     * 判断点击过快
     */
    public boolean fastClick() {
        long lastClick = 0;
        if (System.currentTimeMillis() - lastClick <= 1000) {
            return false;
        }
        lastClick = System.currentTimeMillis();
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isVisible = false;
        if (mWakeLock != null) {
            mWakeLock.release();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isVisible = true;
        if (mWakeLock != null) {
            mWakeLock.acquire();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //取消注册的广播
        if (mNetReceiver != null) {
            unregisterReceiver(mNetReceiver);
            mNetReceiver = null;
        }

        //注释解绑
        unbinder.unbind();

        //移除此Acyivity
        ActivityUtils.removeActivity(this);
    }
}
