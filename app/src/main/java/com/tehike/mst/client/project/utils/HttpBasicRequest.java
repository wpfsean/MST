package com.tehike.mst.client.project.utils;

import android.text.TextUtils;
import android.util.Base64;

import com.tehike.mst.client.project.base.App;
import com.tehike.mst.client.project.global.AppConfig;
import com.tehike.mst.client.project.ui.portactivity.PortMainActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * 描述：WebApi的basic认证请求
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2018/12/13 10:00
 */

public class HttpBasicRequest implements Runnable {

    String url;
    GetHttpData listern;

    public HttpBasicRequest(String url, GetHttpData listern) {
        this.url = url;
        this.listern = listern;
    }

    @Override
    public void run() {
        synchronized (this) {
            //取出本地的用戶名
            String userName = (String) SharedPreferencesUtils.getObject(App.getApplication(), "userName", "");
            if (TextUtils.isEmpty(userName))
                userName = AppConfig.USERNAME;
            //取出本地的密碼
            String userPwd = (String) SharedPreferencesUtils.getObject(App.getApplication(), "userPwd", "");
            if (TextUtils.isEmpty(userPwd))
                userPwd = AppConfig.PWD;

            try {
                HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
                con.setRequestMethod("GET");
                con.setConnectTimeout(3000);
                String authString = userName + ":" + userPwd;
                con.setRequestProperty("Authorization", "Basic " + new String(Base64.encode(authString.getBytes(), 0)));
                con.connect();
                if (con.getResponseCode() == 200) {
                    InputStream in = con.getInputStream();
                    String result = readTxt(in);
                    if (listern != null) {
                        listern.httpData(result);
                    }
                } else {
                    if (listern != null) {
                        listern.httpData("Execption:code != 200");
                    }
                }
                con.disconnect();
            } catch (Exception e) {
                if (listern != null) {
                    listern.httpData("Execption:" + e.getMessage());
                }

            }
        }
    }


    public void start() {
        new Thread(this).start();
    }

    public String readTxt(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuffer sb = new StringBuffer();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        return sb.toString();
    }

    public interface GetHttpData {
        void httpData(String result);
    }
}
