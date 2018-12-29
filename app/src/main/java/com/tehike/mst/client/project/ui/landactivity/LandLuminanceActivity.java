package com.tehike.mst.client.project.ui.landactivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.WindowManager;
import android.widget.SeekBar;

import com.tehike.mst.client.project.R;
import com.tehike.mst.client.project.base.BaseActivity;

import butterknife.BindView;

/**
 * 描述：横屏的亮度高度功能
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2018/11/2 14:17
 */

public class LandLuminanceActivity extends BaseActivity {


    /**
     * 亮度高度拖动条
     */
    @BindView(R.id.land_luminance_seekbar_layout)
    SeekBar lumninanceBar;


    @Override
    protected int intiLayout() {
        return R.layout.activity_land_luminance;
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {


        checkPermission();

        //初始化数据
        initData();

        //更改屏幕亮度
        updateDate();

    }

    /**
     * 申请修改系统信息的权限
     */
    private void checkPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //如果当前平台版本大于23平台
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
     * 拖动修改屏幕亮度
     */
    private void updateDate() {
        lumninanceBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setScreenBrightness(seekBar.getProgress());
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
     * 设置屏幕的亮度
     */
    private void setScreenBrightness(int process) {

        //设置当前窗口的亮度值.这种方法需要权限android.permission.WRITE_EXTERNAL_STORAGE
        WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
        float f = process / 255.0F;
        localLayoutParams.screenBrightness = f;
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


    /**
     * 初始化数据
     */
    private void initData() {
        lumninanceBar.setProgress(0);
        lumninanceBar.setMax(255);
    }

    @Override
    public void onNetChange(int state, String name) {

    }
}
