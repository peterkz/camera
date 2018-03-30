package com.wetoop.camera.bean;

/**
 * Created by Administrator on 2016/5/3.
 */
public class CameraListBean {
    private String online;
    private String sn;
    private String net_id;
    private String deviceName;
    private int shareNum;

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getNet_id() {
        return net_id;
    }

    public void setNet_id(String net_id) {
        this.net_id = net_id;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getOnline() {
        return online;
    }

    public void setOnline(String online) {
        this.online = online;
    }

    public int getShareNum() {
        return shareNum;
    }

    public void setShareNum(int shareNum) {
        this.shareNum = shareNum;
    }
}
