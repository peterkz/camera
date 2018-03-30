package com.wetoop.camera.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wetoop.camera.CameraJni;
import com.wetoop.camera.ui.dialog.SetLoadingDialog;
import com.wetoop.camera.sql.WifiBean;
import com.wetoop.camera.sql.WifiSql;
import com.wetoop.cameras.R;

import java.util.ArrayList;

/**
 * Created by User on 2017/8/24.
 */

public class ResetWifiActivity extends Activity{
    private Button next,galaeye_second_button,galaeye_three_button;
    private RelativeLayout firstR,secondR,threeR,back1,back2,backToLast;
    private ImageView galaeye_connect,galaeye_first_image1;
    private TextView setting,galaeye_second_textview_sn,galaeye_connect_text;
    private EditText wifi_username,wifi_password;
    private String wifi_usernameStr,wifi_passwordStr;
    private ArrayList<WifiBean> wifiBeans;
    private SetLoadingDialog loading1;
    private String sn;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_wifi);
        firstR = (RelativeLayout)findViewById(R.id.galaeye_first);
        secondR = (RelativeLayout)findViewById(R.id.galaeye_second);
        threeR = (RelativeLayout)findViewById(R.id.galaeye_three);
        back1 = (RelativeLayout)findViewById(R.id.back1);
        back2 = (RelativeLayout)findViewById(R.id.back2);
        backToLast = (RelativeLayout)findViewById(R.id.backToLast);
        //galaeye_connect = (ImageView)findViewById(R.id.galaeye_connect);
        galaeye_first_image1 = (ImageView)findViewById(R.id.galaeye_first_image1);
        galaeye_second_textview_sn = (TextView)findViewById(R.id.galaeye_second_textview_sn);
        galaeye_connect_text = (TextView)findViewById(R.id.galaeye_connect_text);
        Intent intent = getIntent();
        if(intent.getStringExtra("offLineSetSn")!=null){
            sn = intent.getStringExtra("offLineSetSn");
            galaeye_second_textview_sn.setText("GALA_"+sn);
        }
        next = (Button)findViewById(R.id.galaeye_first_button);
        next.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                firstR.setVisibility(View.GONE);
                threeR.setVisibility(View.GONE);
                secondR.setVisibility(View.VISIBLE);
            }
        });
        galaeye_second_button = (Button)findViewById(R.id.galaeye_second_button);
        galaeye_second_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firstR.setVisibility(View.GONE);
                threeR.setVisibility(View.VISIBLE);
                secondR.setVisibility(View.GONE);
            }
        });

        setting = (TextView)findViewById(R.id.galaeye_second_button_setting);
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            }
        });

        wifi_username = (EditText)findViewById(R.id.galaeye_wifi_username);
        wifi_password = (EditText)findViewById(R.id.galaeye_wifi_password);
        wifiBeans = new ArrayList<WifiBean>();
        wifiBeans.clear();
        wifiBeans = sqlList();
        if(wifiBeans.get(0).getWifiName()!=null){
            if(!wifiBeans.get(0).getWifiName().equals(""))
                wifi_username.setText(wifiBeans.get(0).getWifiName());
        }
        if(wifiBeans.get(0).getWifiPwd()!=null){
            if(!wifiBeans.get(0).getWifiPwd().equals(""))
                wifi_password.setText(wifiBeans.get(0).getWifiPwd());
        }

        loading1 = new SetLoadingDialog(ResetWifiActivity.this, "提示", "正在设置网络",new SetLoadingDialog.OnCustomDialogListener() {
            @Override
            public void back() {
                Toast.makeText(ResetWifiActivity.this,"设置成功",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.setAction("vitamioLive");
                intent.putExtra("stop", "setWifi");
                sendBroadcast(intent);
                finish();
            }
        });
        loading1.setCanceledOnTouchOutside(false);
        loading1.setCancelable(false);

        galaeye_three_button = (Button)findViewById(R.id.galaeye_three_button);
        galaeye_three_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wifi_usernameStr = wifi_username.getText().toString().trim();
                wifi_passwordStr = wifi_password.getText().toString().trim();
                if(!wifi_usernameStr.equals("")&&!wifi_passwordStr.equals("")){
                    WifiSql addressWifi = new WifiSql(ResetWifiActivity.this);
                    addressWifi.createTable();
                    String sqlWifi = "insert into wifi_list values ('"+wifi_usernameStr+"','"+wifi_passwordStr+"')";
                    addressWifi.insert(sqlWifi);
                    loading1.show();
                    setWifiThread();
                }else{
                    Toast.makeText(ResetWifiActivity.this,"请填写完整",Toast.LENGTH_SHORT).show();
                }
            }
        });

        back1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animationStart=false;
                finish();
            }
        });
        back2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firstR.setVisibility(View.VISIBLE);
                threeR.setVisibility(View.GONE);
                secondR.setVisibility(View.GONE);
            }
        });
        backToLast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firstR.setVisibility(View.GONE);
                threeR.setVisibility(View.GONE);
                secondR.setVisibility(View.VISIBLE);
            }
        });
        animationFirstGo();
    }
    private void animationFirst(){
        int fromXDelta = index2 * 50;
        int toXDelta = (index2+1) * 50;
        TranslateAnimation mAnimation = new TranslateAnimation(fromXDelta, toXDelta, 0, 0);
        mAnimation.setDuration(1500);
        mAnimation.setFillAfter(true);
        galaeye_first_image1.startAnimation(mAnimation);
        index2 ++;
    }
    private int index2 = 0;
    private boolean animationStart=true;
    private void animationFirstGo(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (animationStart){
                    Message msg1 = handler.obtainMessage();
                    msg1.what = 1;
                    handler.sendMessage(msg1);
                    try {
                        Thread.sleep(2*1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Message msg2 = handler.obtainMessage();
                    msg2.what = 2;
                    handler.sendMessage(msg2);
                    //galaeye_first_image1.clearAnimation();
                }
            }
        });
        thread.start();
    }
    private void setWifiThread(){
        Thread t = new Thread(new Runnable() {
            public void run() {
                //System.out.println("come to ret");
                int ret = CameraJni.setApwifiWay(wifi_usernameStr, wifi_passwordStr, 4);
                //System.out.println("ret="+ret);
                /*if(ret==0){
                    Message msg =handler.obtainMessage();
                    msg.what = 2;
                    handler.sendMessage(msg);
                }else */
                if(ret<0||ret==1){
                    Message msg =handler.obtainMessage();
                    msg.what = 3;
                    handler.sendMessage(msg);
                }
            }
        });
        t.start();
    }
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    animationFirst();
                    WifiManager wifiMgr = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    int wifiState = wifiMgr.getWifiState();
                    WifiInfo info = wifiMgr.getConnectionInfo();
                    String wifiId = info != null ? info.getSSID() : null;
                    if(wifiId!=null) {
                        //System.out.println("wifiId="+wifiId);
                        if (wifiId.startsWith("\"GALA_"+sn)){
                            galaeye_connect_text.setText("已连接热点");
                            galaeye_connect_text.setTextColor(getResources().getColor(R.color.galaeye_colorPrimary));
                            //galaeye_connect.setImageDrawable(getResources().getDrawable(R.drawable.galaeye_btn_select));
                            galaeye_second_button.setBackgroundDrawable(getResources().getDrawable(R.drawable.galaeye_first_choice_background));
                            galaeye_second_button.setEnabled(true);
                        }else{
                            galaeye_connect_text.setText("未连接热点");
                            galaeye_connect_text.setTextColor(getResources().getColor(R.color.galaeye_red));
                            //galaeye_connect.setImageDrawable(getResources().getDrawable(R.drawable.galaeye_btn_unselected));
                            galaeye_second_button.setBackgroundDrawable(getResources().getDrawable(R.drawable.galaeye_first_unchoice_background));
                            galaeye_second_button.setEnabled(false);
                        }
                    }
                    break;
                case 2:
                    index2=0;
                    galaeye_first_image1.clearAnimation();
                    break;
                case 3:
                    loading1.dismiss();
                    Toast.makeText(ResetWifiActivity.this,"设置失败",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private ArrayList<WifiBean> sqlList() {
        ArrayList<WifiBean> wifiBeanArrayList = new ArrayList<WifiBean>();
        WifiSql wifiSql = new WifiSql(ResetWifiActivity.this);
        wifiBeanArrayList = wifiSql.queryDataToSQLite();
        return wifiBeanArrayList;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            //firstR.setVisibility(View.GONE);
            //threeR.setVisibility(View.GONE);
            //secondR.setVisibility(View.VISIBLE);
            if(firstR.getVisibility()==View.VISIBLE){
                animationStart=false;
                finish();
            }else if(threeR.getVisibility()==View.VISIBLE){
                firstR.setVisibility(View.GONE);
                threeR.setVisibility(View.GONE);
                secondR.setVisibility(View.VISIBLE);
            }else if(secondR.getVisibility()==View.VISIBLE){
                firstR.setVisibility(View.VISIBLE);
                threeR.setVisibility(View.GONE);
                secondR.setVisibility(View.GONE);
            }

            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }
}
