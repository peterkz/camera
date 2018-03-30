package com.wetoop.camera.api;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2016/4/25.
 */
public class CameraInfo {
    @SerializedName("online")//1在线，0不在线
    private String online;

    @SerializedName("sn")
    private String sn;

    @SerializedName("net_id")
    private String net_id;

    @SerializedName("camera_name")
    private String camera_name;

    @SerializedName("camera_imsi")
    private String camera_imsi;

    @SerializedName("camera_iccid")
    private String camera_iccid;

    @SerializedName("camera_device_id")
    private String camera_device_id;

    @SerializedName("camera_peer_id")
    private String camera_peer_id;

    @SerializedName("user_id")
    private int user_id;

    @SerializedName("camera_created_at")
    private String camera_created_at;

    @SerializedName("camera_update_at")
    private String camera_update_at;

    @SerializedName("camera_last_requested_at")
    private String camera_last_requested_at;

    @SerializedName("share")
    private int share;

    public String getCamera_name() {
        return camera_name;
    }

    public String getCamera_imsi() {
        return camera_imsi;
    }

    public String getCamera_iccid() {
        return camera_iccid;
    }

    public String getCamera_device_id() {
        return camera_device_id;
    }

    public String getCamera_peer_id() {
        return camera_peer_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public String getCamera_created_at() {
        return camera_created_at;
    }

    public String getCamera_update_at() {
        return camera_update_at;
    }

    public String getCamera_last_requested_at() {
        return camera_last_requested_at;
    }

    public String getSn() {
        return sn;
    }

    public String getNet_id() {
        return net_id;
    }

    public String getOnline() {
        return online;
    }

    public int getShare() {
        return share;
    }
}
