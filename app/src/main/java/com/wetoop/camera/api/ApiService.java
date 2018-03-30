package com.wetoop.camera.api;

import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.Path;

/**
 * Created by Administrator on 2016/4/13.
 */
public interface ApiService {
    //获取注册验证码
    @FormUrlEncoded
    @POST("/register/verify-code/for-register")
    public void getVerifyCode(@Field("u") String username, @Field("deviceId") String androidID, @Field("isEmail") Boolean isEmail, @Field("isPhone") Boolean isPhone, Callback<ResultMessage> callback);

    //注册
    @FormUrlEncoded
    @POST("/register")
    public void register(@Field("u") String username, @Field("p") String pwd, @Field("isEmail") Boolean isEmail, @Field("isPhone") Boolean isPhone, @Field("verifyCode") int verifyCode, @Field("deviceId") String androidID, Callback<ResultMessage> callback);

    //修改个人资料（获取验证码）
    @FormUrlEncoded
    @POST("/register/verify-code/for-modify-information")
    public void change_myInfo(@Header("GE-Token") String token, @Field("u") String username, @Field("p") String pwd, @Field("deviceId") String androidID, @Field("isEmail") Boolean isEmail, @Field("isPhone") Boolean isPhone, Callback<ResultMessage> callback);

    //修改个人资料（电子邮箱）
    @FormUrlEncoded
    @POST("/modify-information/modify-email")
    public void change_email(@Header("GE-Token") String token, @Field("email") String username, @Field("p") String pwd, @Field("verifyCode") int verifyCode, @Field("deviceId") String androidID, Callback<ResultMessage> callback);

    //修改个人资料（手机号码）
    @FormUrlEncoded
    @POST("/modify-information/modify-phone-number")
    public void change_phone(@Header("GE-Token") String token, @Field("phoneNumber") String username, @Field("p") String pwd, @Field("verifyCode") int verifyCode, @Field("deviceId") String androidID, Callback<ResultMessage> callback);

    //添加摄像头到当前用户
    @FormUrlEncoded
    @POST("/camera-add")
    public void camera_add(@Header("GE-Token") String token, @Field("UUID") String uuid, @Field("SN") String sn, Callback<ResultMessage> callback);

    //获取重置密码验证码
    @FormUrlEncoded
    @POST("/register/verify-code/for-forgot-password")
    public void forgot_pwd_code(@Field("u") String username, @Field("deviceId") String androidID, @Field("isEmail") Boolean isEmail, @Field("isPhone") Boolean isPhone, Callback<ResultMessage> callback);

    //重置密码
    @FormUrlEncoded
    @POST("/register/forgot-password")
    public void forget_pwd(@Field("u") String username, @Field("p") String pwd, @Field("isEmail") Boolean isEmail, @Field("isPhone") Boolean isPhone, @Field("verifyCode") int verifyCode, @Field("deviceId") String androidID, Callback<ResultMessage> callback);

    //登陆
    @FormUrlEncoded
    @POST("/camera/sign-in")
    public void sign_in(@Field("u") String username, @Field("p") String pwd, @Field("isEmail") Boolean isEmail, @Field("isPhone") Boolean isPhone, @Field("deviceId") String androidID, Callback<ResultMessage> callback);

    //退出登录
    @GET("/camera/sign-out")
    public void sign_out(@Header("GE-Token") String token, Callback<ResultMessage> callback);

    //获取共享码
    //c允许的共享数量，目前固定传1，t摄像头设备密码（不够8位提示修改后才能获取共享码）
    @FormUrlEncoded
    @POST("/camera-share-code")
    public void share_code(@Header("GE-Token") String token, @Field("SN") String sn, @Field("c") int c, @Field("t") String t, Callback<ResultMessage> callback);

    //共享列表
    @FormUrlEncoded
    @POST("/camera-share-list")
    public void share_list(@Header("GE-Token") String token, @Field("SN") String sn, Callback<ResultMessage> callback);

    //通过共享码添加共享,c 共享码（GALA开头，全部为大写字母和数字）
    @FormUrlEncoded
    @POST("/camera-share-add")
    public void share_add(@Header("GE-Token") String token, @Field("c") String c, Callback<ResultMessage> callback);

    //删除共享码,s 共享ID
    @FormUrlEncoded
    @POST("/camera-share-remove")
    public void share_remove(@Header("GE-Token") String token, @Field("s") String s, Callback<ResultMessage> callback);

    //共享RTSP查看摄像头时请求的认证码，s 共享ID；m 方法名；r 认证realm；n 认证nonce；i 认证uri
    @FormUrlEncoded
    @POST("/camera-share-auth")
    public void share_auth(@Header("GE-Token") String token, @Field("s") String s, @Field("m") String m, @Field("r") String r, @Field("n") String n, @Field("i") String i, Callback<ResultMessage> callback);

    @GET("/camera-list")
    public void cameraList(@Header("GE-Token") String token, Callback<ResultMessage> callback);

    //添加摄像头到当前用户
    @FormUrlEncoded
    @POST("/camera-remove")
    public void camera_remove(@Header("GE-Token") String token, @Field("SN") String sn, Callback<ResultMessage> callback);

    //检测摄像头版本
    @GET("/firmware-info/{SN}&{V}")
    public void cameraUpgrade(@Header("GE-Token") String token, @Path("SN") String sn, @Path("V") String v, Callback<ResultMessage> callback);

    //在详情页接收用户信息
    @GET("/user-info")
    public void user_info(@Header("GE-Token") String token, Callback<ResultMessage> callback);

    //集成FCM推送
    @FormUrlEncoded
    @POST("/user-id-update")
    public void postToken(@Header("GE-Token") String token, @Field("id") String id, @Field("type ") String type, Callback<ResultMessage> callback);
}
