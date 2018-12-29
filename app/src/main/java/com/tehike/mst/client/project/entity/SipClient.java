package com.tehike.mst.client.project.entity;

import java.io.Serializable;

/**
 * 描述：Sip状态对封装实体类
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2018/11/12 15:58
 */

public class SipClient implements Serializable {

    //当前sip名称
    private String usrname;
    //描述
    private String description;
    //displayname
    private String dispname;
    //设备（本身）Ip地址
    private String addr;
    //sip状态
    private String state;//0：unregistered 1:registered 2: ring 3:calling
    //
    private String userAgent;
    //通过sip号码确定deviceName
    private String deviceName;

    public SipClient(String usrname, String description, String dispname, String addr, String state, String userAgent, String deviceName) {
        this.usrname = usrname;
        this.description = description;
        this.dispname = dispname;
        this.addr = addr;
        this.state = state;
        this.userAgent = userAgent;
        this.deviceName = deviceName;
    }

    public SipClient() {
    }

    @Override
    public String toString() {
        return "SipClient{" +
                "usrname='" + usrname + '\'' +
                ", description='" + description + '\'' +
                ", dispname='" + dispname + '\'' +
                ", addr='" + addr + '\'' +
                ", state='" + state + '\'' +
                ", userAgent='" + userAgent +
                '}';
    }

    public String getUsrname() {
        return usrname;
    }

    public void setUsrname(String usrname) {
        this.usrname = usrname;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDispname() {
        return dispname;
    }

    public void setDispname(String dispname) {
        this.dispname = dispname;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }


}
