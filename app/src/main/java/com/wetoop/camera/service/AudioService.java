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
public class AudioService extends Service {

    private String usernameStr;
    private String userTokenStr;
    private String netId;

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
            String node = app.getNode();

            String fd = getSDPath() + "/save_cert" + netId + ".txt";
            File f = new File(fd);
            int size = 1;

            String[] s = node.split("#");
            String start = app.getTcpStart();
            if(app.getTcpStart().equals("false")){
                CameraJni.tcpInit(true, s[0], s[1],true);
                audioThreadInit();
            }

            if (!f.exists()) {
                saveCertThread(netId);
            } else {
                try {
                    FileInputStream fis = new FileInputStream(f);
                    size = fis.available();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (size == 0 || size == 2) {
                    saveCertThread(netId);
                } else {
                    if(app.getTcpStart().equals("true")){
                        wrapperThread();
                    }else if(app.getTcpStart().equals("false")){
                        startThread();
                        wrapperThread();
                    }

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
                    int ret = CameraJni.tcpStart(usernameStr, userTokenStr, netId,true);
                    if(app.getTcpStop() == 1){
                        app.setTcpStart("false");
                        app.setTcpStop(0);
                        CameraJni.tcpFree(true);
                    }
                    Log.e("AudioService", "AudioServiceTcpStart: "+ret );
                }
            }
        });
        t1.start();
    }

    private void saveCertThread(final String netId) {
        Thread t1 = new Thread(new Runnable() {

            @Override
            public void run() {
                String fd = getSDPath() + "/save_cert"+netId+".txt";
                File f = new File(fd);
                try {
                    FileOutputStream fOut = new FileOutputStream(f);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                int ret = CameraJni.tcpSavePeer(usernameStr,userTokenStr,netId, 7554,fd,true);
                App app = (App)getApplication();
                if("false".equals(app.getTcpStart())) {
                    startThread();
                }
                wrapperThread();

            }
        });
        t1.start();
    }

    private void wrapperThread() {
        Thread t1 = new Thread(new Runnable() {

            @Override
            public void run() {

                String fd = getSDPath() + "/save_cert" + netId + ".txt";
                File f = new File(fd);
                if(f.exists()) {
                    int ret = CameraJni.tcpAdd(7443, fd,1,true);
                }

            }
        });
        t1.start();
    }

    public void getDataAudio(int portCallback,int conn_idCallback,int audioCome){
        Intent intent = new Intent();
        intent.setAction("videoPortChange");
        if(audioCome == 1) {
            intent.putExtra("audio", "true");
        }else if(audioCome == 0){
            intent.putExtra("audio", "false");
        }
        intent.putExtra("port", conn_idCallback);
        intent.putExtra("port_conn", portCallback);
        sendBroadcast(intent);
        //audioThreadClose();
    }
    private native void audioThreadInit();
    private native void audioThreadClose();
    static {
        System.loadLibrary("openssl_sdk");
    }

    //获取sd卡路径
    public String getSDPath() {
        File sdDir = AudioService.this.getFilesDir();
        /*boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);//判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        }*/
        assert sdDir != null;
        return sdDir.toString();
    }

}
