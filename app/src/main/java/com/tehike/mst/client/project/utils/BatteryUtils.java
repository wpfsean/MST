package com.tehike.mst.client.project.utils;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * 描述：$desc$  获取电池电量
 * ===============================
 *
 * @author $user$ wpfsean@126.com
 * @version V1.0
 * @Create at:$date$ $time$
 */

public class BatteryUtils {


    /**
     * 获取当前的电量
     *
     * @param context
     * @return
     */
    public static int getSystemBattery(Context context) {
        int level = 0;
        Intent batteryInfoIntent = context.getApplicationContext().registerReceiver(null,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        level = batteryInfoIntent.getIntExtra("level", 0);
        int batterySum = batteryInfoIntent.getIntExtra("scale", 100);
        int percentBattery = 100 * level / batterySum;
        return percentBattery;
    }


}
