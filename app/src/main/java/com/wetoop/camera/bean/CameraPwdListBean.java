package com.wetoop.camera.bean;

/**
 * Created by Administrator on 2016/5/3.
 */
public class CameraPwdListBean {
	private String netID;
	private String devicePwd;
	public void setNetID(String netID){
		this.netID = netID;
	}
	public String getNetID(){
		return netID;
	}
	public void setDevicePwd(String devicePwd){
		this.devicePwd = devicePwd;
	}
	public String getDevicePwd(){
		return devicePwd;
	}
}