package com.wetoop.camera.api;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/4/13.
 */
public class ResultMessage {
    @SerializedName("errorCode")
    private Integer errorCode;
    @SerializedName("errorMessage")
    private String errorMessage;
    @SerializedName("result")
    private String token;
    @SerializedName("netToken")
    private String netToken;
    @SerializedName("netId")
    private String netId;
    @SerializedName("node")
    private String node;
    @SerializedName("user")
    private UserInfo user;
    @SerializedName("list")
    private ArrayList<CameraInfo> cameraInfos;
    @SerializedName("joined")
    private ArrayList<JoinedInfo> cameraJoined;
    @SerializedName("firmware")
    private ArrayList<FirmwareInfo> firmware;
    @SerializedName("share_list")
    private ArrayList<ShareListInfo> share_list;

    public ArrayList<FirmwareInfo> getFirmware() {
        return firmware;
    }

    public ArrayList<CameraInfo> getCameraInfos() {
        return cameraInfos;
    }

    public ArrayList<JoinedInfo> getCameraJoined() {
        return cameraJoined;
    }

    public Integer getCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getToken() {
        return token;
    }

    public String getNetToken(){
        return netToken;
    }

    public String getNetId(){
        return netId;
    }

    public ArrayList<ShareListInfo> getShare_list() {
        return share_list;
    }

    public UserInfo getUser() {
        return user;
    }

    public String getNode() {
        return node;
    }

}
