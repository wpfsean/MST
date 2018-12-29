package com.tehike.mst.client.project.sysinfo;

import android.text.TextUtils;

import com.tehike.mst.client.project.base.App;
import com.tehike.mst.client.project.global.AppConfig;
import com.tehike.mst.client.project.utils.CryptoUtil;
import com.tehike.mst.client.project.utils.FileUtil;
import com.tehike.mst.client.project.utils.GsonUtils;
import com.tehike.mst.client.project.utils.Logutil;
import com.tehike.mst.client.project.utils.SharedPreferencesUtils;

/**
 * 描述：获取本地的sysinfo数据
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2018/12/13 9:35
 */

public class SysinfoUtils {

    /**
     * 获取 Sysinfo数据
     *
     * @return
     */
    public static SysInfoBean getSysinfo() {
        try {
            //取出本地保存的Sysinfo数据
            String result = CryptoUtil.decodeBASE64(FileUtil.readFile(AppConfig.SYSINFO).toString());
            if (!TextUtils.isEmpty(result)) {
                //转成封闭对象
                SysInfoBean mSysInfoBean = GsonUtils.GsonToBean(result, SysInfoBean.class);
                if (mSysInfoBean == null) {
                    return null;
                }
                return mSysInfoBean;
            }
        } catch (Exception e) {
            Logutil.e("取sysinfo数据异常--->>>" + e.getMessage());
            return null;
        }
        return null;
    }


    /**
     * 获取中心服务器地址
     *
     * @return
     */
    public static String getServerIp() {
        String serverIp = "";
            SysInfoBean mSysInfoBean = SysinfoUtils.getSysinfo();
            if (mSysInfoBean != null) {
                if (!TextUtils.isEmpty(mSysInfoBean.getWebresourceServer())) {
                    return mSysInfoBean.getWebresourceServer();
                }
            } else {
                serverIp = (String) SharedPreferencesUtils.getObject(App.getApplication(), "serverIp", "");
                if (TextUtils.isEmpty(serverIp)) {
                    serverIp = AppConfig.USERNAME;
                    return serverIp;
                }
            }
        return "";
    }


    /**
     * 获取当前用户名
     *
     * @return
     */
    public static String getUserName() {
        String userName = (String) SharedPreferencesUtils.getObject(App.getApplication(), "userName", "");
        if (TextUtils.isEmpty(userName)) {
            userName = AppConfig.USERNAME;
            return userName;
        }
        return userName;
    }

    /**
     * 获取当前密码
     *
     * @return
     */
    public static String getUserPwd() {
        String userPwd = (String) SharedPreferencesUtils.getObject(App.getApplication(), "userPwd", "");
        if (TextUtils.isEmpty(userPwd)) {
            userPwd = AppConfig.PWD;
            return userPwd;
        }
        return userPwd;
    }

}
