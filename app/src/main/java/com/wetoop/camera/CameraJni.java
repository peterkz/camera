package com.wetoop.camera;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.wetoop.camera.listener.CJMListener;

/**
 * Created by Administrator on 2016/3/14.
 */
public class CameraJni extends Activity{
    public int port=-1;
    public int conn_id=-1;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        /* Create a TextView and set its content.
         * the text is retrieved by calling a native
         * function.
         */
        //TextView tv = new TextView(this);
        //tv.setText( stringFromJNI(1,"","",1,"","") );
        //setContentView(tv);

    }
    /* A native method that is implemented by the
     * 'hello-jni' native library, which is packaged
     * with this application.
     */
    public native static int stringFromJNI(int WPA,String wifi,String password,int type,String tokenname,String tokenpassword);

    /* This is another native method declaration that is *not*
     * implemented by 'hello-jni'. This is simply to show that
     * you can declare as many native methods in your Java code
     * as you want, their implementation is searched in the
     * currently loaded native libraries only the first time
     * you call them.
     *
     * Trying to call this function will result in a
     * java.lang.UnsatisfiedLinkError exception !
     */
    public native String  unimplementedStringFromJNI();

    public static native int setApwifi(String wifi,String password,int type);

    public static native int setApwifiWay(String wifi,String password,int type);

    public native static String[] searchDevice();

    public native static String[] uuidDevice(String username,String pwd,String devip);

    public native static int[] searchDeviceIp();

    public native static String[] deviceInfo(String username,String pwd,String devip,int port);
    public native static String[] netlinkInfo(String username,String pwd,String devip,int port);
    public native static int[] spaceInfo(String username,String pwd,String devip,int port);
    public native static int batteryInfo(String username,String pwd,String devip,int port);
    public native static int alarmInfo(String username,String pwd,String devip,int port);

    public native static int setPwd(String username,String pwd,String devip,int port,String setPwd);

    //public native static int voiceTalk(String username,String pwd,String devip,int port,byte[] buffer);

    public native static int setAlarmCfg(String username,String pwd,String devip,int port,int check);

    public native static int upgrade(String username,String pwd,String devip,int port,String fileName);

    //public native static int opensslSaveCert(String sd,int post);

    //public native static void opensslConn(String sd,int post);

    //public native static int sslWrapper(String local_addr,String target_addr);

    //public native static void sslClose();

    public native static int[] searchByMonth(int year,int month);

    public native static String[] searchByDay(int year,int month,int day);

    public native static void ppsdevClose();

    public native static int voiceTalkOpen(String username,String pwd,String devip,int port);

    public native static int voiceTalkWrite(byte[] buffer);

    public native static void voiceTalkClose();

    public static native void addListener(CJMListener listener);

    public static native void getUseNum(String dataStr,long useNum);

    public static native void tcpInit(boolean debug,String route,String node,boolean audioFrom);

    public static native int tcpSavePeer(String name,String token,String netId,int connect_port,String ca_file,boolean audioFrom);

    public static native int tcpStart(String name,String token,String netId ,boolean audioFrom);

    public static native int tcpAdd(int connect_port,String ca_file,int audioCome,boolean audioFrom);

    public static native int tcpResult(int conn_id,boolean audioFrom);

    public static native int tcpAuthResult(int conn_id,boolean audioFrom);

    public static native int tcpErrorResult(int conn_id,boolean audioFrom);

    public static native int tcpRemove(int conn_id,boolean audioFrom);

    public static native int tcpStop(boolean audioFrom);

    public static native int tcpFree(boolean audioFrom);

    /*public void getData(int portCallback,int conn_idCallback){
        port = portCallback;
        conn_id = conn_idCallback;
        System.out.println("port="+port);
        System.out.println("conn_id="+conn_id);
        Intent intent = new Intent();
        intent.setAction("videoPortChange");
        intent.putExtra("port",conn_id);
        intent.putExtra("port_conn",port);
        sendBroadcast(intent);
    }*/

    static {
        System.loadLibrary("pp_sdk");
        System.loadLibrary("openssl_sdk");
    }

}