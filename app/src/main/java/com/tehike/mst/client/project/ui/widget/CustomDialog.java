package com.tehike.mst.client.project.ui.widget;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.tehike.mst.client.project.R;

public class CustomDialog extends Dialog {

    private static int default_width = 160; // 默认宽度

    private static int default_height = 120;// 默认高度

    private Context mContext;

    public CustomDialog(Context context, View layout, int style) {

        super(context, style);

        mContext = context;
    }

    public CustomDialog(Context context, View layout, int width, int height,
                        int gravity, int style) {
        super(context, style);
        setContentView(layout);
        mContext = context;
        // initSocialSDK();
        // initPlatformMap();
        Window window = getWindow();

        WindowManager.LayoutParams params = window.getAttributes();
        params.height = height;
        params.width = width;
        params.gravity = gravity;
        window.setAttributes(params);
    }

    public CustomDialog(Context context, View layout, int width, int height,
                        int gravity) {
        super(context, R.style.dialog);
        setContentView(layout);
        mContext = context;
        // initSocialSDK();
        // initPlatformMap();
        Window window = getWindow();

        WindowManager.LayoutParams params = window.getAttributes();
        params.height = height;
        params.width = width;
        params.gravity = gravity;
        window.setAttributes(params);
    }

    @Override
    public void show() {
        super.show();
    }
}
