package com.tehike.mst.client.project.utils;

import android.text.TextUtils;
import android.util.Base64;
import com.tehike.mst.client.project.sysinfo.SysinfoUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 描述：基于 HTTP Basic 方式设计webapi接口
 * <p>
 * 详见文档:
 * <p>
 * CMS Server WebAPI
 * =================
 * /webapi/sysinfo 		输出服务资源定义
 * /webapi/videos[?groupid=xx] 	输出视频资源
 * /webapi/videogroups		输出视频资源分组
 * /webapi/sips[?groupid=xx]	输出 SIP 资源
 * /webapi/sipgroups		输出 SIP 资源分组
 * /webapi/sipstatus		输出 SIP 注册，振铃，通话的状态 (不在此列表中的SIP客户端视为离线)
 * /webapi/onlinedevices		输出心跳保活的设备，带有弹箱等状态
 * <p>
 * 身份认证基于 HTTP Basic 方式，用户名口令为 users 表内的登录账号
 * <p>
 * <p>
 * <p>
 * <p>
 * http://19.0.0.229:8080/webapi/sipstatus
 * <p>
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2018/11/13 10:25
 */


public class HttpUtils implements Runnable {

    //传入的url
    String url;

    //回调结果
    GetHttpData listern;

    /**
     * 构造方法
     *
     * @param url
     * @param listern
     */
    public HttpUtils(String url, GetHttpData listern) {
        this.url = url;
        this.listern = listern;
    }

    @Override
    public void run() {
        //加同步锁
        synchronized (this) {
            try {
                //获取用户名和密码
                String userName = SysinfoUtils.getUserName();
                String userPwd = SysinfoUtils.getUserPwd();
                //用HttpURLConnection请求
                HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
                con.setRequestMethod("GET");
                con.setConnectTimeout(3000);
                String authString = userName + ":" + userPwd;
                //添加 basic参数
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
    /**
     * 运行子线程
     */
    public void start() {
        new Thread(this).start();
    }


    /**
     * 字节 流转可见字符
     *
     * @param in
     * @return
     * @throws IOException
     */
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

    /**
     * 暴露接口回调
     */
    public interface GetHttpData {
        void httpData(String result);
    }
}
