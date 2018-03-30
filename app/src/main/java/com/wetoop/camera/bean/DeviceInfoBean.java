package com.wetoop.camera.bean;

/**
 * Created by Administrator on 2016/4/29.
 */
public class DeviceInfoBean {
    private String uuid;
    private String devname;
    private String model;
    private String seriano;
    private String softwareversion;//软件版本
    private String hardwareversion;//硬件版本
    private String wifilevel;
    private String ltestatus;
    private String ltelevel;
    private int total_space = -1;
    private int free_space = -1;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getDevname() {
        return devname;
    }

    public void setDevname(String devname) {
        this.devname = devname;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSeriano() {
        return seriano;
    }

    public void setSeriano(String seriano) {
        this.seriano = seriano;
    }

    public String getSoftwareversion() {
        return softwareversion;
    }

    public void setSoftwareversion(String softwareversion) {
        this.softwareversion = softwareversion;
    }

    public String getHardwareversion() {
        return hardwareversion;
    }

    public void setHardwareversion(String hardwareversion) {
        this.hardwareversion = hardwareversion;
    }

    public String getWifilevel() {
        return wifilevel;
    }

    public void setWifilevel(String wifilevel) {
        this.wifilevel = wifilevel;
    }

    public String getLtestatus() {
        return ltestatus;
    }

    public void setLtestatus(String ltestatus) {
        this.ltestatus = ltestatus;
    }

    public String getLtelevel() {
        return ltelevel;
    }

    public void setLtelevel(String ltelevel) {
        this.ltelevel = ltelevel;
    }

    public int getTotal_space() {
        return total_space;
    }

    public void setTotal_space(int total_space) {
        this.total_space = total_space;
    }

    public int getFree_space() {
        return free_space;
    }

    public void setFree_space(int free_space) {
        this.free_space = free_space;
    }
}
