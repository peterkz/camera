package com.wetoop.camera.bean;

/**
 * Created by Administrator on 2016/3/17.
 */
public class SqlSnBean {
    private String sn;
    private String ip;
    private String username;
    private String uuid;
    private String camera_name;
    private String camera_imsi;
    private String camera_iccid;
    private String camera_device_id;
    private String camera_peer_id;
    private int user_id;
    private String camera_created_at;
    private String camera_update_at;
    private String camera_last_requested_at;

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public void setIp(String ip){
        this.ip = ip;
    }
    public String getIp() {
        return ip;
    }
    public void setSn(String sn){
        this.sn = sn;
    }
    public String getSn() {
        return sn;
    }
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getCamera_name() {
        return camera_name;
    }

    public void setCamera_name(String camera_name) {
        this.camera_name = camera_name;
    }

    public String getCamera_imsi() {
        return camera_imsi;
    }

    public void setCamera_imsi(String camera_imsi) {
        this.camera_imsi = camera_imsi;
    }

    public String getCamera_iccid() {
        return camera_iccid;
    }

    public void setCamera_iccid(String camera_iccid) {
        this.camera_iccid = camera_iccid;
    }

    public String getCamera_device_id() {
        return camera_device_id;
    }

    public void setCamera_device_id(String camera_device_id) {
        this.camera_device_id = camera_device_id;
    }

    public String getCamera_peer_id() {
        return camera_peer_id;
    }

    public void setCamera_peer_id(String camera_peer_id) {
        this.camera_peer_id = camera_peer_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getCamera_created_at() {
        return camera_created_at;
    }

    public void setCamera_created_at(String camera_created_at) {
        this.camera_created_at = camera_created_at;
    }

    public String getCamera_update_at() {
        return camera_update_at;
    }

    public void setCamera_update_at(String camera_update_at) {
        this.camera_update_at = camera_update_at;
    }

    public String getCamera_last_requested_at() {
        return camera_last_requested_at;
    }

    public void setCamera_last_requested_at(String camera_last_requested_at) {
        this.camera_last_requested_at = camera_last_requested_at;
    }
}