package com.wetoop.camera.api;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2017/5/22.
 */

public class JoinedInfo {
    @SerializedName("net_id")
    private String net_id;

    @SerializedName("online")
    private String online;

    @SerializedName("from")
    private String from;

    @SerializedName("share_id")
    private String share_id;

    @SerializedName("peer")
    private String peer;

    public String getFrom() {
        return from;
    }

    public String getShare_id() {
        return share_id;
    }

    public String getPeer() {
        return peer;
    }

    public String getOnline() {
        return online;
    }

    public String getNet_id() {
        return net_id;
    }
}
