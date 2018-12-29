
package com.tehike.mst.client.project.entity;

import java.io.Serializable;

/**
 * 描述：聊天消息对应的实体类
 * ===============================
 * @author wpfse wpfsean@126.com
 * @Create at:2018/11/12 15:55
 * @version V1.0
 */
public class ChatMsgEntity implements Serializable {

    //当前聊天者的用户名
    private String name;

    //日期
    private String date;

    //聊天内容
    private String text;

    //用于区分对方还是自己
    private boolean isComMeg = true;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean getMsgType() {
        return isComMeg;
    }

    public void setMsgType(boolean isComMsg) {
        isComMeg = isComMsg;
    }

    public ChatMsgEntity() {
    }

    public ChatMsgEntity(String name, String date, String text, boolean isComMsg) {
        super();
        this.name = name;
        this.date = date;
        this.text = text;
        this.isComMeg = isComMsg;
    }

    @Override
    public String toString() {
        return "ChatMsgEntity{" +
                "name='" + name + '\'' +
                ", date='" + date + '\'' +
                ", text='" + text + '\'' +
                ", isComMeg=" + isComMeg +
                '}';
    }
}
