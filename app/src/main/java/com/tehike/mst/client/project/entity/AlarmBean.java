package com.tehike.mst.client.project.entity;

import java.io.Serializable;

/**
 * 描述：报警时对应的实体类
 * ===============================
 * @author wpfse wpfsean@126.com
 * @Create at:2018/11/12 16:10
 * @version V1.0
 */

public class AlarmBean implements Serializable {

    private String sender;//报警者ip
    private VideoBean videoBen;//被报警的实体类对象
    private String alertType; //报警类型
    private String reserved; //保留字段

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public VideoBean getVideoBen() {
        return videoBen;
    }

    public void setVideoBen(VideoBean videoBen) {
        this.videoBen = videoBen;
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public String getReserved() {
        return reserved;
    }

    public void setReserved(String reserved) {
        this.reserved = reserved;
    }

    public AlarmBean(String sender, VideoBean videoBen, String alertType, String reserved) {
        super();
        this.sender = sender;
        this.videoBen = videoBen;
        this.alertType = alertType;
        this.reserved = reserved;
    }

    public AlarmBean() {
        super();
    }

    @Override
    public String toString() {
        return "AlarmBean [sender=" + sender + ", videoBen=" + videoBen + ", ALERT_TYPE=" + alertType + ", reserved="
                + reserved + "]";
    }
}
