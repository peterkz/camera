package com.wetoop.camera.api;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2016/9/1.
 */
public class FirmwareInfo {
    @SerializedName("version")
    private String version;//版本号
    @SerializedName("url")
    private String url;//下载地址
    @SerializedName("message")
    private String message;//更新提示信息
    @SerializedName("id")
    private String id;//sha256校验

    public String getVersion() {
        return version;
    }

    public String getUrl() {
        return url;
    }

    public String getMessage() {
        return message;
    }

    public String getId() {
        return id;
    }
}
