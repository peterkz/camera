package com.wetoop.camera.api;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2017/5/18.
 */

public class ShareListInfo {
    @SerializedName("share_to")
    private String share_to;//分享给谁
    @SerializedName("share_id")
    private String share_id;//分享Id,可用于删除

    public String getShare_to() {
        return share_to;
    }

    public String getShare_id() {
        return share_id;
    }
}
