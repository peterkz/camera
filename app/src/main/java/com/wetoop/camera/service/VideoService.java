package com.wetoop.camera.service;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.wetoop.camera.CameraJni;
import com.wetoop.camera.App;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Administrator on 2016/9/8.
 */
public class VideoService extends Service {

    private String usernameStr;
    private String userTokenStr;
    private String netId;
    private String audioFrom;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        App app = (App) getApplication();

        if (intent != null) {
            netId = intent.getStringExtra("netId");
            usernameStr = intent.getStringExtra("usernameStr");
            userTokenStr = intent.getStringExtra("userpawStr");
            String node = intent.getStringExtra("node");
            audioFrom = intent.getStringExtra("audioFrom");

            String fd = getSDPath()+ "/GalaEye4G/" +  netId + ".txt";
            app.setNetIDFd(fd);
            File f = new File(fd);
            int size = 1;
            if(app.getTcpStart().equals("false")){
                String[] s = node.split("#");
                CameraJni.tcpInit(true,s[0],s[1],false);
                threadInit();
            }

            if (!f.exists()) {
                saveCertThread(app);
            } else {
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(f);
                    size = fis.available();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (size == 0 || size == 2) {
                    saveCertThread(app);
                } else {
                    if(app.getTcpStart().equals("false")){
                        startThread();
                    }
                    wrapperThread(app,audioFrom);
                }
            }

        }
    }

    private void startThread() {
        Thread t1 = new Thread(new Runnable() {

            @Override
            public void run() {
                App app = (App)getApplication();
                if("false".equals(app.getTcpStart())) {
                    app.setTcpStart("true");
                    int ret = CameraJni.tcpStart(usernameStr, userTokenStr, netId,false);
                    Log.e("VideoService", "VideoServiceTcpStart: "+ret );
                    if(app.getTcpStop() == 1){
                        app.setTcpStart("false");
                        app.setTcpStop(0);
                        CameraJni.tcpFree(false);
                    }
                }
            }
        });
        t1.start();
    }

    public void saveCertThread(App app) {
        usernameStr = app.getLoginNetId();
        userTokenStr = app.getLoginNetToken();
        String fd = app.getNetIDFd();
        int ret = CameraJni.tcpSavePeer(usernameStr,userTokenStr,netId, 7554,fd,false);
        startThread();
    }

    public void wrapperThread(App app,String audioFrom) {
        String fdStr = app.getNetIDFd();
        File f = new File(fdStr);
        if(f.exists()) {
            int ret;
            if("true".equals(audioFrom))
                ret = CameraJni.tcpAdd(7443, fdStr,1,true);
            else
                ret = CameraJni.tcpAdd(7554, fdStr,0,false);
            Log.e("videoService", "tcpAdd: "+ret);
            if(ret<0){
                app.setTcpStart("false");
            }
        }
    }

    public void stopTcp() {
        int ret = CameraJni.tcpStop(false);
        System.out.println("stopTcp="+ret);
    }

    public void getData(int portCallback,int conn_idCallback,int audioCome){
        Intent intent = new Intent();
        intent.setAction("videoPortChange");
        if(audioCome == 1) {
            intent.putExtra("audio", "true");
        }else if(audioCome == 0){
            intent.putExtra("audio", "false");
        }
        intent.putExtra("port", conn_idCallback);
        intent.putExtra("port_conn",portCallback);
        sendBroadcast(intent);
        //threadClose();
    }
    private native void threadInit();
    private native void threadClose();
    static {
        System.loadLibrary("openssl_sdk");
    }

    //获取sd卡路径
    public String getSDPath() {
        File sdDir = VideoService.this.getFilesDir();
        File destDir = new File(sdDir + "/GalaEye4G");
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        return sdDir.toString();
    }

}