package com.wetoop.camera.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.wetoop.camera.App;
import com.wetoop.camera.CameraJni;
import com.wetoop.camera.api.ResultMessage;
import com.wetoop.camera.bean.DeviceInfoBean;
import com.wetoop.camera.bean.SqlSnBean;
import com.wetoop.camera.service.VideoService;
import com.wetoop.camera.ui.dialog.AddDeviceDialog;
import com.wetoop.camera.ui.dialog.EditDialog;
import com.wetoop.camera.ui.dialog.HintDialog;
import com.wetoop.camera.ui.dialog.SetDeviceNameDialog;
import com.wetoop.camera.ui.dialog.UploadingDialog;
import com.wetoop.camera.service.AudioService;
import com.wetoop.camera.sql.CameraListSql;
import com.wetoop.camera.sql.CameraSqlSn;
import com.wetoop.camera.ui.vlc.ResultConstant;
import com.wetoop.cameras.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.wetoop.camera.tools.SqlOperation.CameraListSqlUpDateName;
import static com.wetoop.camera.tools.SqlOperation.CameraListSqlUpDatePwd;

/**
 * Created by Administrator on 2016/4/22.
 */
public class DeviceInfoActivity extends Activity implements View.OnClickListener{
    private RelativeLayout pb;
    private TextView titleText,model,version,sn;
    private TextView internetState,networkSignal,Lan,batteryState;//设备状态
    private TextView totalStorage,freeStorage;//设备存储状态
    private DeviceInfoBean deviceInfoBean;
    private String ssid,paw,setPwd,devicePwd;
    private int type,changeAlarmCount=0;
    private int auth_result;
    private int batteryInfo;
    private int alarmInfo;
    private ToggleButton detectionButton;
    private ProgressBar video_loading_progress;
    private String snStr,deviceName,netId;
    private UploadingDialog uploadingDialog;
    private EditDialog editDialog;
    private VideoService videoService = new VideoService();
    //private MyBroadcastReceiver broadcastReceiverLive;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_info);
        init();
        getIntentData();
        getDeviceInfoThread();//用子线程获取设备信息
    }

    private void init(){
        RelativeLayout update = (RelativeLayout) findViewById(R.id.update);
        RelativeLayout recording = (RelativeLayout) findViewById(R.id.recording);
        RelativeLayout rename = (RelativeLayout) findViewById(R.id.rename);
        RelativeLayout changePwd = (RelativeLayout) findViewById(R.id.change_pwd);
        RelativeLayout resetWifi = (RelativeLayout) findViewById(R.id.reset_wifi);
        RelativeLayout certificate = (RelativeLayout) findViewById(R.id.certificate);
        RelativeLayout back = (RelativeLayout) findViewById(R.id.back);
        RelativeLayout refresh = (RelativeLayout) findViewById(R.id.refresh);
        pb = (RelativeLayout)findViewById(R.id.pb);

        update.setOnClickListener(this);
        recording.setOnClickListener(this);
        rename.setOnClickListener(this);
        changePwd.setOnClickListener(this);
        resetWifi.setOnClickListener(this);
        certificate.setOnClickListener(this);
        back.setOnClickListener(this);
        refresh.setOnClickListener(this);
        pb.setVisibility(View.VISIBLE);

        titleText = (TextView)findViewById(R.id.title_text);
        model = (TextView)findViewById(R.id.model_text);
        version = (TextView)findViewById(R.id.version_text);
        sn = (TextView)findViewById(R.id.sn_text);
        internetState = (TextView)findViewById(R.id.network_status_text);
        networkSignal = (TextView)findViewById(R.id.network_signal);
        Lan = (TextView)findViewById(R.id.lan_text);
        networkSignal = (TextView)findViewById(R.id.battery_state);
        totalStorage = (TextView)findViewById(R.id.total_storage_text);
        freeStorage = (TextView)findViewById(R.id.free_storage_text);

        detectionButton = (ToggleButton)findViewById(R.id.detection_button);
        detectionButton.setOnClickListener(this);
        video_loading_progress = (ProgressBar)findViewById(R.id.video_loading_progress);

        deviceInfoBean = new DeviceInfoBean();
        App app = (App) getApplication();
        app.setAudioCome(1);
    }

    private void getIntentData(){
        App app = (App)getApplication();
        int pos = app.getVideoPos();
        Intent intent = getIntent();
        if(intent.getStringExtra("sn")!=null)
            snStr = intent.getStringExtra("sn");

        if(intent.getStringExtra("netId")!=null)
            netId = intent.getStringExtra("netId");

        if(intent.getStringExtra("deviceName")!=null)
            deviceName = intent.getStringExtra("deviceName");

        if(intent.getStringExtra("devicePwd")!=null)
            devicePwd = intent.getStringExtra("devicePwd");

        Intent intent2 = new Intent(DeviceInfoActivity.this, videoService.getClass());
        intent2.setPackage(getPackageName());
        intent2.putExtra("netId",netId);
        intent2.putExtra("usernameStr",app.getLoginNetId());
        intent2.putExtra("userpawStr",app.getLoginNetToken());
        intent2.putExtra("node",app.getNode());
        intent2.putExtra("audioFrom","true");
        DeviceInfoActivity.this.startService(intent2);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.back:
                CameraJni.ppsdevClose();
                Intent intent1 = new Intent(DeviceInfoActivity.this, videoService.getClass());
                intent1.setPackage(getPackageName());
                DeviceInfoActivity.this.stopService(intent1);
                App app = (App)getApplication();
                int auth = CameraJni.tcpAuthResult(app.getAudioPort(),true);
                int authResult = CameraJni.tcpResult(app.getAudioPort(),true);
                if(!(auth == -1 || authResult == 0 || authResult == -10))
                    CameraJni.tcpRemove(app.getAudioPort(),true);
                CameraJni.tcpStop(true);
                app.setTcpStop(1);
                app.setTcpStart("false");
                finish();
                break;
            case R.id.refresh:
                pb.setVisibility(View.VISIBLE);
                App appRefresh = (App)getApplication();
                auth_result = CameraJni.tcpAuthResult(appRefresh.getAudioPort(),true);
                if(auth_result != -1)
                    CameraJni.tcpRemove(appRefresh.getAudioPort(),true);
                /*CameraJni.tcpStop(true);
                appRefresh.setTcpStop(1);*/
                videoService.wrapperThread(appRefresh,"true");
                getDeviceInfoThread();
                break;
            case R.id.recording://录制的视频
                Intent intent2 = new Intent(DeviceInfoActivity.this,RecordedActivity.class);
                intent2.putExtra("title",deviceName);
                startActivity(intent2);
                //finish();
                break;
            case R.id.rename://修改摄像头名称
                SetDeviceNameDialog setDeviceNameDialog = new SetDeviceNameDialog(DeviceInfoActivity.this, "修改用户名",new SetDeviceNameDialog.OnCustomDialogListener() {

                    @Override
                    public void back(String usernameStr) {
                        CameraListSql cameraSql = new CameraListSql(DeviceInfoActivity.this);
                        cameraSql.updatetable(CameraListSqlUpDateName(snStr,usernameStr));
                    }
                });
                setDeviceNameDialog.show();

                break;
            case R.id.reset_wifi://重置摄像头
                AddDeviceDialog startDialog = new AddDeviceDialog(DeviceInfoActivity.this, "重置摄像头",deviceName,deviceInfoBean.getUuid(), new AddDeviceDialog.OnCustomDialogListener() {

                    @Override
                    public void back(String usernameStr, String userpawStr, int pg_themeStr) {
                        ssid = usernameStr;
                        paw = userpawStr;
                        type = pg_themeStr;
                        setWifiThread();
                    }
                });
                startDialog.show();
                break;
            case R.id.certificate://重新获取证书
                saveCertThread();
                break;
            case R.id.update://更新设备
                upgrade();
                break;
            case R.id.change_pwd://重新设置访问密码
                editDialog = new EditDialog(DeviceInfoActivity.this, "重设密码", "确定", 2, new EditDialog.OnCustomDialogListener() {
                    @Override
                    public void back(String resultStr) {
                        editDialog.progressBar.setVisibility(View.VISIBLE);
                        editDialog.comfirmBt.setVisibility(View.GONE);
                        setPwd = resultStr;
                        setPwd(resultStr);
                    }

                    @Override
                    public void back(String userStr, String pwdStr) {

                    }
                });
                editDialog.show();
                break;
            case R.id.detection_button://设置红外移动侦测通知
                video_loading_progress.setVisibility(View.VISIBLE);
                detectionButton.setVisibility(View.INVISIBLE);
                changeAlarm();
                break;
        }
    }

    private void changeAlarm() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                App app = (App)getApplication();
                int ret = -1;
                videoService.wrapperThread(app,"true");
                boolean loop = true;
                while (loop) {
                    if (app.getBroadcast() == 1) {
                        app.setBroadcast(0);
                        loop = false;
                        if (app.getAlarm_getcfg().equals("open")) {
                            changeAlarmCount = 1;
                            ret = CameraJni.setAlarmCfg(app.getAdminName(), devicePwd, "127.0.0.1", app.getAudioPortConn(), 0);
                        } else {
                            changeAlarmCount = 2;
                            ret = CameraJni.setAlarmCfg(app.getAdminName(), devicePwd, "127.0.0.1", app.getAudioPortConn(), 1);
                        }

                        if (ret >= 0) {
                            Message msg = handler.obtainMessage();
                            msg.what = 12;
                            handler.sendMessage(msg);
                        } else {
                            Message msg = handler.obtainMessage();
                            msg.what = 13;
                            handler.sendMessage(msg);
                        }
                    }
                }
            }
        });
        t.start();
    }

    private void setPwd(final String pwd) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run(){
                App app = (App)getApplication();
                videoService.wrapperThread(app,"true");
                boolean loop = true;
                while (loop) {
                    if (app.getBroadcast() == 1) {
                        app.setBroadcast(0);
                        loop = false;
                        int ret = CameraJni.setPwd(app.getAdminName(), devicePwd, "127.0.0.1", app.getAudioPortConn(), pwd);
                        if (ret >= 0) {
                            Message msg = handler.obtainMessage();
                            msg.what = 10;
                            handler.sendMessage(msg);
                        } else {
                            Message msg = handler.obtainMessage();
                            msg.what = 11;
                            handler.sendMessage(msg);
                        }
                    }
                }
            }
        });
        t.start();
    }

    private void upgrade(){

        App application = (App) getApplication();
        if(application.getToken()!=null){
            if(!application.getToken().equals("")){
                application.getApiService().cameraUpgrade(application.getToken(),snStr,deviceInfoBean.getSoftwareversion(), new Callback<ResultMessage>() {
                    @Override
                    public void success(final ResultMessage resultMessage, Response response) {
                        if (resultMessage.getCode() == 200) {
                            String fd = getSDPath() +"/cameraDownLoad/" + sn+".txt";
                            String sha = Encrypt(ReadTxtFile(fd),"SHA-256");
                            if(resultMessage.getFirmware()!=null){
                                if(resultMessage.getFirmware().get(0).getVersion().equals(deviceInfoBean.getSoftwareversion())){
                                    HintDialog checkedDialog = new HintDialog(DeviceInfoActivity.this, "提示","已经是最新版本","确定", new HintDialog.OnCustomDialogListener() {
                                        @Override
                                        public void back(String startTime) {

                                        }
                                    });
                                    checkedDialog.show();
                                }else{
                                    HintDialog checkedDialog = new HintDialog(DeviceInfoActivity.this, "提示","检测到新版本，是否要更新","更新", new HintDialog.OnCustomDialogListener() {
                                        @Override
                                        public void back(String startTime) {
                                            App application = (App) getApplication();
                                            application.setVersion(resultMessage.getFirmware().get(0).getVersion());
                                            download(resultMessage.getFirmware().get(0).getUrl());
                                            application.setVersion(resultMessage.getFirmware().get(0).getId());
                                            Toast.makeText(DeviceInfoActivity.this, resultMessage.getFirmware().get(0).getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    checkedDialog.show();
                                }
                            }
                        } else {
                            Toast.makeText(DeviceInfoActivity.this, resultMessage.getErrorMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                            Toast.makeText(DeviceInfoActivity.this, "服务器未响应", Toast.LENGTH_SHORT).show();
                    }

                });
            }
        }
    }

    //读取文本文件中的内容
    public  String ReadTxtFile(String strFilePath)
    {
        String content = ""; //文件内容字符串
        //打开文件
        File file = new File(strFilePath);
        //如果path是传递过来的参数，可以做一个非目录的判断
        if (file.isDirectory()) {
            Log.d("TestFile", "The File doesn't not exist.");
        } else {
            try {
                InputStream instream = new FileInputStream(file);
                if (instream != null) {
                    InputStreamReader inputreader = new InputStreamReader(instream);
                    BufferedReader buffreader = new BufferedReader(inputreader);
                    String line;
                    //分行读取
                    while (( line = buffreader.readLine()) != null) {
                        content += line + "\n";
                    }
                    instream.close();
                    }
                } catch (java.io.FileNotFoundException e) {
                Log.d("TestFile", "The File doesn't not exist.");
               }
            catch (IOException e) {
                Log.d("TestFile", e.getMessage());
                }
            }
        return content;
    }

    /**
     * 对字符串加密,加密算法使用MD5,SHA-1,SHA-256,默认使用SHA-256
     *
     * @param strSrc
     *            要加密的字符串
     * @param encName
     *            加密类型
     * @return
     */
    public String Encrypt(String strSrc, String encName) {
        MessageDigest md = null;
        StringBuilder sb = new StringBuilder();

        byte[] bt = strSrc.getBytes();
        try {
            md = MessageDigest.getInstance(encName);
            byte[] result = md.digest(bt);
            for (byte b : result) {
                sb.append(String.format("%02x", b));
            }
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        return sb.toString();
    }


    private void download(String url) {
        App app = (App) getApplication();
        uploadingDialog = new UploadingDialog(DeviceInfoActivity.this, "",app.getAdminName(),devicePwd,app.getAudioPortConn() ,snStr,url,new UploadingDialog.OnCustomDialogListener() {
            @Override
            public void back(int type) {
                switch (type){
                    case 2:
                        Toast.makeText(DeviceInfoActivity.this,"更新失败",Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        Toast.makeText(DeviceInfoActivity.this,"更新成功",Toast.LENGTH_SHORT).show();
                        break;
                }
                //Toast.makeText(DeviceInfo.this,"完成下载",Toast.LENGTH_SHORT).show();
                uploadingDialog.dismiss();
            }
        });
        uploadingDialog.setCanceledOnTouchOutside(false);
        uploadingDialog.show();
    }

    private void saveCertThread() {
        App app = (App) getApplication();
        String fd = app.getNetIDFd();
        int ret = CameraJni.tcpSavePeer(app.getLoginNetId(), app.getLoginNetToken(), netId, 7443, fd, true);
        if (ret == 0) {
            Toast.makeText(DeviceInfoActivity.this,"获取成功",Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(DeviceInfoActivity.this,"获取失败",Toast.LENGTH_SHORT).show();
        }
    }
    //获取sd卡路径
    public String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);//判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        }
        return sdDir.toString();
    }

    private void setWifiThread(){
        Thread t = new Thread(new Runnable() {
            public void run() {
                int ret = CameraJni.setApwifi(ssid, paw, type);
                if(ret==0){
                    Message msg =handler.obtainMessage();
                    msg.what = 10;
                    handler.sendMessage(msg);
                }else if(ret<0||ret==1){
                    Message msg =handler.obtainMessage();
                    msg.what = 11;
                    handler.sendMessage(msg);
                }
            }
        });
        t.start();
    }

    private void getDeviceInfoThread() {
        Thread t = new Thread(new Runnable() {
            public void run() {
                App app = (App) getApplication();
                try {
                    Thread.sleep(2000);//若不休眠一段时间，会拿不到信息，我也不懂为啥= =
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                boolean loop = true;
                while (loop) {
                    if (app.getBroadcast() == 1) {
                        app.setBroadcast(0);
                        loop = false;
                        Log.e("deviceInfo", "deviceInfo.devicePwd = " + devicePwd + "<getAudioPortConn>=" + app.getAudioPortConn());
                        String[] deviceInfo = CameraJni.deviceInfo(app.getAdminName(), devicePwd, "127.0.0.1", app.getAudioPortConn());
                        if ("false".equals(deviceInfo[0])) {
                            Message msg = handler.obtainMessage();
                            msg.what = 0;
                            handler.sendMessage(msg);
                        } else {
                            try {
                                deviceInfo[0] = new String(deviceInfo[0].getBytes("ISO8859-1"), "GBK");
                                deviceInfoBean.setDevname(deviceInfo[0]);//设备名称
                                deviceInfo[1] = new String(deviceInfo[1].getBytes("ISO8859-1"), "GBK");
                                deviceInfoBean.setModel(deviceInfo[1]);//设备型号
                                deviceInfo[2] = new String(deviceInfo[2].getBytes("ISO8859-1"), "GBK");
                                deviceInfoBean.setSeriano(deviceInfo[2]);//设备序列号
                                deviceInfo[3] = new String(deviceInfo[3].getBytes("ISO8859-1"), "GBK");
                                deviceInfoBean.setSoftwareversion(deviceInfo[3]);//软件版本
                                deviceInfo[4] = new String(deviceInfo[4].getBytes("ISO8859-1"), "GBK");
                                deviceInfoBean.setHardwareversion(deviceInfo[4]);//硬件版本
                                Message msg = handler.obtainMessage();
                                msg.what = 1;
                                handler.sendMessage(msg);
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }

        });
        t.start();
    }

    private void getNetlinkInfoThread(){
        Thread t = new Thread(new Runnable() {
            public void run() {
                App app = (App)getApplication();
                videoService.wrapperThread(app,"true");
                boolean loop = true;
                while (loop) {
                    if (app.getBroadcast() == 1) {
                        app.setBroadcast(0);
                        loop = false;
                        try {
                            String[] netlinkInfo = CameraJni.netlinkInfo(app.getAdminName(), devicePwd, "127.0.0.1", app.getAudioPortConn());
                            if (!"false".equals(netlinkInfo[0])) {
                                netlinkInfo[0] = new String(netlinkInfo[0].getBytes("ISO8859-1"), "GBK");
                                deviceInfoBean.setWifilevel(netlinkInfo[0]);//wifi信号
                                netlinkInfo[1] = new String(netlinkInfo[1].getBytes("ISO8859-1"), "GBK");
                                deviceInfoBean.setLtestatus(netlinkInfo[1]);//网络状态
                                netlinkInfo[2] = new String(netlinkInfo[2].getBytes("ISO8859-1"), "GBK");
                                deviceInfoBean.setLtelevel(netlinkInfo[2]);//网络信号
                            }
                            Message msg = handler.obtainMessage();
                            msg.what = 2;
                            handler.sendMessage(msg);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        t.start();
    }

    private void getSpaceInfoThread(){
        Thread t = new Thread(new Runnable() {
            public void run() {
                App app = (App)getApplication();
                videoService.wrapperThread(app,"true");
                boolean loop = true;
                while (loop) {
                    if (app.getBroadcast() == 1) {
                        app.setBroadcast(0);
                        loop = false;
                        int[] spaceInfo = CameraJni.spaceInfo(app.getAdminName(), devicePwd, "127.0.0.1", app.getAudioPortConn());
                        if (spaceInfo[0] >= 0) {
                            deviceInfoBean.setTotal_space(spaceInfo[0]);//储存卡总空间
                            deviceInfoBean.setFree_space(spaceInfo[1]);//储存卡剩余空间
                        }
                        Message msg = handler.obtainMessage();
                        msg.what = 4;
                        handler.sendMessage(msg);
                    }
                }
            }
        });
        t.start();
    }

    private void getBatteryInfoThread(){
        Thread t = new Thread(new Runnable() {
            public void run() {
                App app = (App)getApplication();
                videoService.wrapperThread(app,"true");
                boolean loop = true;
                while (loop) {
                    if (app.getBroadcast() == 1) {
                        app.setBroadcast(0);
                        loop = false;
                        batteryInfo = CameraJni.batteryInfo(app.getAdminName(), devicePwd, "127.0.0.1", app.getAudioPortConn());//电池剩余百分比：（<0）错误
                        //orderList.add(deviceInfoBean);
                        Message msg = handler.obtainMessage();
                        msg.what = 3;
                        handler.sendMessage(msg);
                    }
                }
            }
        });
        t.start();
    }

    private void getAlarmInfoThread(){
        Thread t = new Thread(new Runnable() {
            public void run() {
                App app = (App)getApplication();
                videoService.wrapperThread(app,"true");
                boolean loop = true;
                while (loop) {
                    if (app.getBroadcast() == 1) {
                        app.setBroadcast(0);
                        loop = false;
                        alarmInfo = CameraJni.alarmInfo(app.getAdminName(), devicePwd, "127.0.0.1", app.getAudioPortConn());//红外开关状态：（0）关闭；（1）开启；（<0）错误
                        //orderList.add(deviceInfoBean);
                        Message msg = handler.obtainMessage();
                        msg.what = 5;
                        handler.sendMessage(msg);
                    }
                }
            }
        });
        t.start();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            App app = (App)getApplication();
            switch (msg.what){
                case 0:
                    int tcpResult = CameraJni.tcpResult(app.getAudioPort(),true);
                    String hintTip = "";
                    if(tcpResult <= 0) switch (tcpResult) {
                        case ResultConstant.FORWARD_RESULT_PEER_OFFLINE:
                            hintTip = "设备不在线";
                            break;
                        case ResultConstant.FORWARD_RESULT_CONN_LOST:
                            hintTip = "连接被关闭";
                            break;
                        case ResultConstant.FORWARD_RESULT_CERT_ERROR:
                            hintTip = String.format("证书校验错误 (%ld)", CameraJni.tcpErrorResult(app.getAudioPort(),true));
                            break;
                        case ResultConstant.FORWARD_RESULT_KICKED_OUT:
                            hintTip = "别处已登录，连接被关闭";
                            break;
                        case ResultConstant.FORWARD_RESULT_CONN_CLOSE:
                            hintTip = "本机连接断开";
                            break;
                    }
                    auth_result = CameraJni.tcpAuthResult(app.getAudioPort(),true);
                    if(auth_result == 401){
                        hintTip = "访问密码错误";
                    }
                    Log.e("DeviceInfo", "tcpResult: "+tcpResult);
                    Log.e("DeviceInfo", "auth_result: "+auth_result);
                    if(auth_result == -1)
                        hintTip = "获取信息出错";
                    Toast.makeText(DeviceInfoActivity.this, hintTip, Toast.LENGTH_SHORT).show();
                    titleText.setText(hintTip);
                    internetState.setText("离线");
                    model.setText("未知");
                    version.setText("未知");
                    sn.setText(snStr);

                    networkSignal.setText("未知");
                    Lan.setText("未知");
                    networkSignal.setText("未知");
                    totalStorage.setText("未知");
                    freeStorage.setText("未知");
                    video_loading_progress.setVisibility(View.INVISIBLE);
                    detectionButton.setVisibility(View.VISIBLE);
                    if(app.getAlarm_getcfg().equals("open")){
                        detectionButton.setChecked(true);
                    }else{
                        detectionButton.setChecked(false);
                    }
                    pb.setVisibility(View.GONE);

                    break;
                case 1:
                    int authDevice = CameraJni.tcpAuthResult(app.getAudioPort(),true);
                    if(authDevice != -1)
                        CameraJni.tcpRemove(app.getAudioPort(),true);
                    getNetlinkInfoThread();
                    internetState.setText("在线");
                    titleText.setText(deviceName);
                    model.setText(deviceInfoBean.getModel());
                    version.setText(deviceInfoBean.getSoftwareversion());
                    sn.setText(snStr);
                    break;
                case 2:
                    int authNetlink = CameraJni.tcpAuthResult(app.getAudioPort(),true);//51365
                    if(authNetlink != -1)
                        CameraJni.tcpRemove(app.getAudioPort(),true);
                    getBatteryInfoThread();
                    if (deviceInfoBean.getLtestatus()!=null){
                        switch (deviceInfoBean.getLtestatus()){
                            case "1":
                                networkSignal.setText("2G："+deviceInfoBean.getLtelevel()+"%");
                                break;
                            case "2":
                                networkSignal.setText("3G："+deviceInfoBean.getLtelevel()+"%");
                                break;
                            case "3":
                                networkSignal.setText("4G："+deviceInfoBean.getLtelevel()+"%");
                                break;
                        }
                    }
                    if(deviceInfoBean.getWifilevel() != null)
                        Lan.setText(deviceInfoBean.getWifilevel()+"%");
                    break;
                case 3:
                    int authBattery = CameraJni.tcpAuthResult(app.getAudioPort(),true);
                    if(authBattery != -1)
                        CameraJni.tcpRemove(app.getAudioPort(),true);
                    getSpaceInfoThread();
                    if(batteryInfo > 0)
                        networkSignal.setText(batteryInfo+"%");
                    break;
                case 4:
                    int authSpace = CameraJni.tcpAuthResult(app.getAudioPort(),true);
                    if(authSpace != -1)
                        CameraJni.tcpRemove(app.getAudioPort(),true);
                    getAlarmInfoThread();
                    if(deviceInfoBean.getTotal_space()>=0)
                        totalStorage.setText(deviceInfoBean.getTotal_space()+"MB");
                    if(deviceInfoBean.getTotal_space()>=0)
                        freeStorage.setText(deviceInfoBean.getFree_space()+"MB");
                    break;
                case 5:
                    int authAlarmInfo = CameraJni.tcpAuthResult(app.getAudioPort(),true);
                    if(authAlarmInfo != -1)
                        CameraJni.tcpRemove(app.getAudioPort(),true);
                    if(alarmInfo == 1){
                        detectionButton.setChecked(true);
                        app.setAlarm_getcfg("open");
                    }else{
                        detectionButton.setChecked(false);
                        app.setAlarm_getcfg("close");
                    }
                    video_loading_progress.setVisibility(View.INVISIBLE);
                    detectionButton.setVisibility(View.VISIBLE);
                    pb.setVisibility(View.GONE);
                    break;
                case 6:
                    Toast.makeText(DeviceInfoActivity.this,"重设wifi成功",Toast.LENGTH_SHORT).show();
                    break;
                case 7:
                    Toast.makeText(DeviceInfoActivity.this,"重设wifi失败",Toast.LENGTH_SHORT).show();
                    break;
                case 8:
                    Toast.makeText(DeviceInfoActivity.this,"重新获取证书成功",Toast.LENGTH_SHORT).show();
                    break;
                case 9:
                    Toast.makeText(DeviceInfoActivity.this,"重新获取证书失败",Toast.LENGTH_SHORT).show();
                    break;
                case 10:
                    CameraListSql address = new CameraListSql(DeviceInfoActivity.this);
                    address.insert(CameraListSqlUpDatePwd(netId,setPwd));
                    devicePwd = setPwd;
                    if(editDialog != null) editDialog.dismiss();
                    Toast.makeText(DeviceInfoActivity.this,"设置密码成功",Toast.LENGTH_SHORT).show();
                    break;
                case 11:
                    if(editDialog != null) editDialog.dismiss();
                    Toast.makeText(DeviceInfoActivity.this,"设置密码失败",Toast.LENGTH_SHORT).show();
                    break;
                case 12:
                    int authAlarmS = CameraJni.tcpAuthResult(app.getAudioPort(),true);
                    if(authAlarmS != -1)
                        CameraJni.tcpRemove(app.getAudioPort(),true);
                    video_loading_progress.setVisibility(View.INVISIBLE);
                    detectionButton.setVisibility(View.VISIBLE);
                    if (changeAlarmCount == 1) {
                        detectionButton.setChecked(false);
                    }else if(changeAlarmCount == 2){
                        detectionButton.setChecked(true);
                    }
                    Toast.makeText(DeviceInfoActivity.this,"设置成功",Toast.LENGTH_SHORT).show();
                    break;
                case 13:
                    int authAlarmF = CameraJni.tcpAuthResult(app.getAudioPort(),true);
                    if(authAlarmF != -1)
                        CameraJni.tcpRemove(app.getAudioPort(),true);
                    video_loading_progress.setVisibility(View.INVISIBLE);
                    detectionButton.setVisibility(View.VISIBLE);
                    detectionButton.setChecked(false);
                    Toast.makeText(DeviceInfoActivity.this,"设置失败",Toast.LENGTH_SHORT).show();
                    break;
                /*case 14:
                    int authPwdF = CameraJni.tcpAuthResult(app.getAudioPort(),true);
                    if(authPwdF != -1)
                        CameraJni.tcpRemove(app.getAudioPort(),true);
                    Toast.makeText(DeviceInfoActivity.this,"更新失败",Toast.LENGTH_SHORT).show();
                    //upgradeThread();
                    break;
                case 15:
                    int authPwdS = CameraJni.tcpAuthResult(app.getAudioPort(),true);
                    if(authPwdS != -1)
                        CameraJni.tcpRemove(app.getAudioPort(),true);
                    Toast.makeText(DeviceInfoActivity.this,"更新成功",Toast.LENGTH_SHORT).show();
                    //upgradeThread();
                    break;*/
            }
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            CameraJni.ppsdevClose();
            Intent intent = new Intent(DeviceInfoActivity.this, videoService.getClass());
            intent.setPackage(getPackageName());
            DeviceInfoActivity.this.stopService(intent);
            App app = (App)getApplication();
            int auth = CameraJni.tcpAuthResult(app.getAudioPort(),true);
            int authResult = CameraJni.tcpResult(app.getAudioPort(),true);
            if(!(auth == -1 || authResult == 0 || authResult == -10))
                CameraJni.tcpRemove(app.getAudioPort(),true);
            CameraJni.tcpStop(true);
            app.setTcpStop(1);
            app.setTcpStart("false");
            finish();
            return false;
        }else {
            return super.onKeyDown(keyCode, event);
        }
    }
}
