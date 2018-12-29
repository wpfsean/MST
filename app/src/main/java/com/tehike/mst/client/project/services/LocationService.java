package com.tehike.mst.client.project.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.tehike.mst.client.project.global.AppConfig;
import com.tehike.mst.client.project.utils.Logutil;

/**
 * 描述：$desc$
 * ===============================
 *
 * @author $user$ wpfsean@126.com
 * @version V1.0
 * @Create at:$date$ $time$
 */

public class LocationService extends Service {


    /**
     * 位置
     */
    LocationManager locationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        initializeLocation();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
            locationManager = null;
        }
        super.onDestroy();
    }

    /**
     * 初始化位置监听
     */
    @SuppressLint("MissingPermission")
    private void initializeLocation() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //查看定位是否打开
        boolean isGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (isNetwork) {
            Logutil.d("定位类型--->>isNetwork");
            @SuppressLint("MissingPermission") Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location != null) {
                AppConfig.LOCATION_LAT = location.getLatitude();
                AppConfig.LOCATION_LOG = location.getLongitude();
            }
            //定位监听
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        } else if (isGps) {
            Logutil.d("定位类型--->>isGps");
            @SuppressLint("MissingPermission") Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                AppConfig.LOCATION_LAT = location.getLatitude();
                AppConfig.LOCATION_LOG = location.getLongitude();
            }
            //定位监听
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
    }

    /**
     * 位置监听回调
     */
    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
         //   Logutil.e("定位经纬度-->>" + location.getLatitude() + "\t" + location.getLongitude());
            AppConfig.LOCATION_LAT = location.getLatitude();
            AppConfig.LOCATION_LOG = location.getLongitude();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };
}
