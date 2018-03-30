package com.wetoop.camera.api;

import com.google.gson.annotations.SerializedName;

/**
 * Created by User on 2017/7/31.
 */

public class UserInfo {
    @SerializedName("mail")
    private String mail;

    @SerializedName("phone")
    private String phone;

    public String getMail() {
        return mail;
    }

    public String getPhone() {
        return phone;
    }
}
