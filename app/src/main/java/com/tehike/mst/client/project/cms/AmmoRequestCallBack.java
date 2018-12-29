package com.tehike.mst.client.project.cms;

import android.text.TextUtils;

import com.tehike.mst.client.project.global.AppConfig;
import com.tehike.mst.client.project.sysinfo.SysinfoUtils;
import com.tehike.mst.client.project.utils.ByteUtils;
import com.tehike.mst.client.project.utils.WriteLogToFile;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;


/**
 * 開箱申請(测试用)
 * <p>
 * 向后台服务器发送开箱申请
 *
 *
 */

public class AmmoRequestCallBack implements Runnable {
    GetDataListern listern;


    public interface GetDataListern {
        void getDataInformation(String result);
    }

    public AmmoRequestCallBack(GetDataListern listern) {
        this.listern = listern;
    }

    @Override
    public void run() {
        synchronized (this) {

            Socket socket = null;
            byte[] request = new byte[68];
            // 数据头
            byte[] flage1 = "ReqB".getBytes();
            System.arraycopy(flage1, 0, request, 0, flage1.length);
            // 版本号
            request[4] = 0;
            request[5] = 0;
            request[6] = 0;
            request[7] = 1;

            // 请求的action
            request[8] = 0;
            request[9] = 0;
            request[10] = 0;
            request[11] = 0;

            // ulReserved1(四位随机数)
            int ulReserved1 = (int) (Math.random() * (9999 - 1000 + 1)) + 1000;
            byte[] flage2 = ByteUtils.toByteArray(ulReserved1);
            System.arraycopy(flage2, 0, request, 12, flage2.length);

            // ulReserved2（四位随机数）
            int ulReserved2 = (int) (Math.random() * (9999 - 1000 + 1)) + 1000;
            byte[] flage3 = ByteUtils.toByteArray(ulReserved2);
            System.arraycopy(flage3, 0, request, 16, flage3.length);

            byte[] sender = new byte[48];

            String guid = SysinfoUtils.getSysinfo().getDeviceGuid();

            byte[] guidByte = guid.getBytes();
            System.arraycopy(guidByte, 0, sender, 0, guidByte.length);
            System.arraycopy(sender, 0, request, 20, 48);

            try {
                //获取报警服务器ip
                String serverIp = SysinfoUtils.getSysinfo().getAlertServer();
                //报警服务器端口
                int sendPort = SysinfoUtils.getSysinfo().getAlertPort();

                socket = new Socket(serverIp, sendPort);
                socket.setSoTimeout(6 * 1000);
                OutputStream os = socket.getOutputStream();
                os.write(request);
                os.flush();

                InputStream in = socket.getInputStream();
                byte[] headers = new byte[20];
                int read = in.read(headers);
                // 解析数据头
                byte[] action = new byte[4];
                for (int i = 0; i < 4; i++) {
                    action[i] = headers[i + 8];
                }
                int number1 = ByteUtils.bytesToInt(action, 0);
                //System.out.println(number1);
                if (listern != null) {
                    listern.getDataInformation(number1 + "");
                }
            } catch (Exception e) {
                if (listern != null) {
                    listern.getDataInformation("Execption:" + e.getMessage());
                }
                WriteLogToFile.info("申请开箱异常:"+e.getMessage());
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                        socket = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void start() {
        new Thread(this).start();
    }
}
