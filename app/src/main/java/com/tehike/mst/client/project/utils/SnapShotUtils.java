package com.tehike.mst.client.project.utils;

import android.graphics.Bitmap;
import android.os.Environment;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.tehike.mst.client.project.base.App;
import com.tehike.mst.client.project.global.AppConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 描述：onvif抓图工具类
 * <p>
 * 通过mediaUrl和paramater获取截图的url
 * <p>
 * 再通过volly请求上面的url
 * <p>
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2018/11/13 14:59
 */

public class SnapShotUtils extends Thread {

    //回调监听
    CallbackBitmap listern;

    String PicUrl;


    /**
     * 构造函数
     *
     * @param PicUrl
     * @param listern
     */
    public SnapShotUtils(String PicUrl, CallbackBitmap listern) {
        this.PicUrl = PicUrl;
        this.listern = listern;
    }

    @Override
    public void run() {

        //拼加参数
        try {

            //使用volly请求图片
            ImageRequest imageRequest = new ImageRequest(PicUrl, new Response.Listener<Bitmap>() {
                @Override
                public void onResponse(Bitmap bitmap) {
                    //返回的图片不为空
                    if (bitmap != null) {
                        //回调信息
                        if (listern != null) {
                            listern.getBitMap(bitmap);
                            //并保存图片
                            saveBitmap(bitmap);
                        }
                    }
                }
            }, 0, 0, null, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    //请求失败时的回调
                    if (listern != null) {
                        listern.fail("图片请求失败:" + volleyError.getMessage());
                    }
                }
            });
            //添加到队列
            App.getQuest().add(imageRequest);
        } catch (Exception e) {
            //异常回调
            if (listern != null) {
                listern.fail("图片请求异常:" + e.getMessage());
            }
        }
    }

    /**
     * 保存图片到sd卡
     *
     * @param bitmap
     */
    private void saveBitmap(Bitmap bitmap) {
        //sd卡路路径
        File appDir = new File(Environment.getExternalStorageDirectory(), AppConfig.SHOT_DIR);
        //判断目录是否存在
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        //根据当前的年月日生成图标名称
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");//获取当前时间，进一步转化为字符串
        Date date = new Date();
        String str = format.format(date);
        //要保存的名称
        String fileName = str + ".jpg";
        //生成一个file
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            //压缩80%并写入文件
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            Logutil.i("图片保存异常:" + e.getMessage());
        }
    }

    /**
     * 接口回调
     * 成功 返回bitmap
     * 失败 返回失败原因
     */
    public interface CallbackBitmap {
        public void getBitMap(Bitmap bitmap);

        public void fail(String fail);

    }

    /**
     * 执行子线程
     */
    public void start() {
        new Thread(this).start();
    }

}
