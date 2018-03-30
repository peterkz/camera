package com.wetoop.camera.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wetoop.camera.ui.adapter.AddDeviceAdapter;
import com.wetoop.camera.ui.dialog.AddDeviceWifiDialog;
import com.wetoop.camera.CameraJni;
import com.wetoop.camera.ui.dialog.CheckedDialog;
import com.wetoop.camera.ui.dialog.HintDialog;
import com.wetoop.cameras.R;
import com.wetoop.camera.App;
import com.wetoop.camera.api.ResultMessage;
import com.wetoop.camera.bean.DeviceInfoBean;
import com.wetoop.camera.bean.SqlSnBean;
import com.wetoop.camera.sql.CameraListSql;
import com.wetoop.camera.sql.CameraSqlSn;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.wetoop.camera.tools.SqlOperation.CameraListSqlInit;

/**
 * Created by Administrator on 2016/7/25.
 */
public class AddDeviceSecondFragment extends Fragment {
    private List list;
    private int type,pos=0;
    private AddDeviceAdapter adapter;
    private ListView cameraList;
    private DeviceInfoBean deviceInfoBean;
    private ArrayList<SqlSnBean> cameraIpList;
    private RelativeLayout ProgressView,refurbish,bossTextView,change_wifi;
    private TextView loadingText;
    private String ssid,paw,netToken,netId;
    private String adminUser="admin",adminPwd="admin";
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView;

        rootView = inflater.inflate(R.layout.fragment_add_device_second, container, false);

        list = new ArrayList();
        cameraList=(ListView)rootView.findViewById(R.id.camera_add_list);
        ProgressView = (RelativeLayout)rootView.findViewById(R.id.video_loading);
        refurbish = (RelativeLayout)rootView.findViewById(R.id.refurbish);
        bossTextView = (RelativeLayout)rootView.findViewById(R.id.bossTextView);
        change_wifi = (RelativeLayout)rootView.findViewById(R.id.change_wifi);
        loadingText = (TextView)rootView.findViewById(R.id.video_loading_text);

        bossTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddDeviceWifiDialog startDialog = new AddDeviceWifiDialog(getActivity(), "设置wifi信息", new AddDeviceWifiDialog.OnCustomDialogListener() {

                    @Override
                    public void back(String usernameStr, String userpawStr, int pg_themeStr) {
                        App app = (App)getActivity().getApplication();
                        app.setWifiName(usernameStr);
                        app.setWifiPwd(userpawStr);
                        app.setWifiType(pg_themeStr);
                        ProgressView.setVisibility(View.VISIBLE);
                        setWifiThread();
                    }
                });
                startDialog.show();
            }
        });

        change_wifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddDeviceWifiDialog startDialog = new AddDeviceWifiDialog(getActivity(), "设置wifi信息", new AddDeviceWifiDialog.OnCustomDialogListener() {

                    @Override
                    public void back(String usernameStr, String userpawStr, int pg_themeStr) {
                        App app = (App)getActivity().getApplication();
                        app.setWifiName(usernameStr);
                        app.setWifiPwd(userpawStr);
                        app.setWifiType(pg_themeStr);

                        ProgressView.setVisibility(View.VISIBLE);
                        setWifiThread();

                    }
                });
                startDialog.show();
            }
        });

        refurbish.setVisibility(View.INVISIBLE);
        change_wifi.setVisibility(View.INVISIBLE);
        cameraList.setVisibility(View.INVISIBLE);
        ProgressView.setVisibility(View.INVISIBLE);
        deviceInfoBean = new DeviceInfoBean();
        cameraIpList = new ArrayList<>();
        cameraIpList.clear();
        adapter = new AddDeviceAdapter(getActivity(),list);
        cameraList.setAdapter(adapter);

        cameraList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pos = position;
                cameraIpList = sql();
                deviceInfoThread(adminUser, adminPwd, pos);
                loadingText.setText("验证用户名密码···");
                ProgressView.setVisibility(View.VISIBLE);
                cameraList.setVisibility(View.INVISIBLE);
                refurbish.setVisibility(View.INVISIBLE);
                bossTextView.setVisibility(View.INVISIBLE);
                change_wifi.setVisibility(View.INVISIBLE);
            }
        });
        refurbish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refurbish.setVisibility(View.INVISIBLE);
                bossTextView.setVisibility(View.INVISIBLE);
                change_wifi.setVisibility(View.INVISIBLE);
                ProgressView.setVisibility(View.VISIBLE);
                loadingText.setText("搜索设备中···");
                cameraList.setVisibility(View.INVISIBLE);
                java_thread();
            }
        });
        return rootView;
    }

    private ArrayList<SqlSnBean> sql(){
        ArrayList<SqlSnBean> cameraList=new ArrayList<SqlSnBean>();
        CameraSqlSn cameraSql = new CameraSqlSn(getActivity().getApplicationContext());
        cameraList=cameraSql.queryDataToSQLite();
        return cameraList;
    }

    public String longToIp2(long ip) {
        return (ip & 0xFF)+"."+((ip >> 8) & 0xFF)+"."+((ip >> 16) & 0xFF)+"."+((ip >> 24) & 0xFF);
    }
    //用于搜索设备
    void java_thread(){
        Thread t = new Thread(new Runnable(){
            public void run(){
                int countTest=0;
                ArrayList<SqlSnBean> orderList = new ArrayList<SqlSnBean>();
                orderList.clear();
                String[] texts  = CameraJni.searchDevice();
                for (int i = 0; i < texts.length; i=i+2) {
                    SqlSnBean sql_sn = new SqlSnBean();
                    if(!texts[i].equals("")&&texts[i]!=null){
                        try {
                            texts[i] = new String(texts[i].getBytes("ISO8859-1"), "GBK");
                            countTest = 1;
                            sql_sn.setSn(texts[i]);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                        try {
                            texts[i + 1] = new String(texts[i + 1].getBytes("ISO8859-1"), "GBK");
                            int ipNum = Integer.parseInt(texts[i + 1]);
                            String ipStr = longToIp2(ipNum);
                            sql_sn.setIp(ipStr);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                        orderList.add(sql_sn);
                    }

                }

                if(countTest == 1&&getActivity()!=null){
                    CameraSqlSn address = new CameraSqlSn(getActivity().getApplicationContext());
                    address.createTable();
                    address.setDataToSQLite(orderList);//对地址数据信息进行更新
                    Message msg =handler.obtainMessage();
                    msg.what = 3;//搜索到设备，显示到listView上
                    handler.sendMessage(msg);
                }else{
                    Message msg =handler.obtainMessage();
                    msg.what = 4;//搜索不到设备
                    handler.sendMessage(msg);
                }


            }});
        t.start();
    }

    //在添加摄像头之前做的检测，使用的是获取摄像头的基本信息
    void deviceInfoThread(final String adminU, final String adminP,final int position) {
        Thread t = new Thread(new Runnable() {
            public void run() {
                if(cameraIpList.size()>0){
                    if(cameraIpList.get(position).getIp()!=null){
                        adminUser=adminU;
                        adminPwd=adminP;
                        String[] deviceInfo  = CameraJni.deviceInfo(adminUser, adminPwd, cameraIpList.get(position).getIp(),80);
                        if(!deviceInfo[0].equals("false")){
                            try {
                                deviceInfo[0]=new String(deviceInfo[0].getBytes("ISO8859-1"),"GBK");
                                deviceInfo[5]=new String(deviceInfo[5].getBytes("ISO8859-1"),"GBK");
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                            deviceInfoBean.setDevname(deviceInfo[0]);
                            deviceInfoBean.setUuid(deviceInfo[5]);
                            App app = (App)getActivity().getApplication();
                            app.setAdminName(adminUser);
                            app.setAdminPwd(adminPwd);

                            Message msg =handler.obtainMessage();
                            msg.what = 1;//是否要添加此摄像头
                            handler.sendMessage(msg);

                        }else{
                            Message msg =handler.obtainMessage();
                            msg.what = 2;//用户名或密码错误，重新输入
                            handler.sendMessage(msg);
                        }

                    }
                }
            }
        });
        t.start();
    }

    private void register(){
        App app = (App)getActivity().getApplication();
        app.getApiService().camera_add(app.getToken(), deviceInfoBean.getUuid(), cameraIpList.get(pos).getSn(), new retrofit.Callback<ResultMessage>() {

            @Override
            public void success(ResultMessage resultMessage, Response response) {
                bossTextView.setVisibility(View.INVISIBLE);
                if (resultMessage != null) {
                    if (resultMessage.getCode() == 200) {
                        if (resultMessage.getNetToken() != null) {
                            netToken = resultMessage.getNetToken();
                            netId = resultMessage.getNetId();
                            httpClient(netToken, netId, cameraIpList.get(pos).getIp());
                        }

                    } else {
                        ProgressView.setVisibility(View.INVISIBLE);
                        cameraList.setVisibility(View.VISIBLE);
                        refurbish.setVisibility(View.VISIBLE);
                        change_wifi.setVisibility(View.VISIBLE);
                        bossTextView.setVisibility(View.INVISIBLE);
                        Toast.makeText(getActivity(), resultMessage.getErrorMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    private void setWifiThread(){
        Thread t = new Thread(new Runnable() {
            public void run() {
                App app = (App)getActivity().getApplication();
                ssid = app.getWifiName();
                paw = app.getWifiPwd();
                type = app.getWifiType();
                int ret = CameraJni.setApwifiWay(ssid, paw, type);
                if(ret==0){
                    Message msg =handler.obtainMessage();
                    msg.what = 8;//进入搜索设备的线程
                    handler.sendMessage(msg);
                }else if(ret<0||ret==1){
                    Message msg =handler.obtainMessage();
                    msg.what = 5;//添加失败
                    handler.sendMessage(msg);
                }
            }
        });
        t.start();
    }

    //验证通过之后post一条数据给摄像头，若返回码为200则成功
    private void httpClient(final String net_token, final String net_id, final String ip) {
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    HttpURLConnection conn;
                    String baseUrl = "http://" + ip + "/devices/ict_token?" + "value=name:" + net_id + "&token:" + net_token;
                    URL post_url = new URL(baseUrl);
                    conn = (HttpURLConnection) post_url.openConnection();
                    conn.setRequestMethod("POST");

                    String data = "";
                    byte[] data_bytes = data.getBytes();
                    String headerDate = adminUser+":"+adminPwd;
                    byte[] headerDate_bytes = headerDate.getBytes();
                    try {
                        String base64 = Base64.encodeToString(headerDate_bytes, Base64.DEFAULT);
                        conn.setRequestProperty("Authorization", "Basic " + base64);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.setRequestProperty("Content-Length", data_bytes.length + "");
                    conn.setDoOutput(true);
                    OutputStream os = conn.getOutputStream();
                    os.write(data_bytes);
                    os.close();
                    conn.setConnectTimeout(5000);
                    if (conn.getResponseCode() == 200) {
                        CameraListSql address = new CameraListSql(getContext());
                        App app = (App) getActivity().getApplication();
                        address.insert(CameraListSqlInit(net_id,app.getAdminPwd()));
                        Message msg = handler.obtainMessage();
                        msg.what = 6;//添加成功
                        handler.sendMessage(msg);
                    } else {
                        Message msg = handler.obtainMessage();
                        msg.what = 7;//wifi用户名或密码错误
                        handler.sendMessage(msg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

    private Handler handler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    ProgressView.setVisibility(View.INVISIBLE);
                    cameraList.setVisibility(View.VISIBLE);
                    refurbish.setVisibility(View.VISIBLE);
                    bossTextView.setVisibility(View.INVISIBLE);
                    change_wifi.setVisibility(View.VISIBLE);
                    HintDialog addDialog = new HintDialog(getActivity(), "提示","是否要添加此摄像头","添加", new HintDialog.OnCustomDialogListener() {
                        @Override
                        public void back(String startTime) {
                            if (startTime.equals("logout")) {
                                register();
                            }
                        }
                    });
                    addDialog.show();
                    break;
                case 2:
                    refurbish.setVisibility(View.VISIBLE);
                    change_wifi.setVisibility(View.VISIBLE);
                    ProgressView.setVisibility(View.INVISIBLE);
                    cameraList.setVisibility(View.VISIBLE);
                    bossTextView.setVisibility(View.INVISIBLE);
                    Toast.makeText(getActivity(),"用户名或密码错误，请重新输入",Toast.LENGTH_LONG).show();
                    CheckedDialog checkedDialog = new CheckedDialog(getActivity(), "填写用户名密码", new CheckedDialog.OnCustomDialogListener() {

                        @Override
                        public void back(String usernameStr, String userpawStr) {
                            cameraIpList = sql();
                            deviceInfoThread(usernameStr,userpawStr,pos);
                        }
                    });
                    checkedDialog.show();
                    break;
                case 3:
                    refurbish.setVisibility(View.VISIBLE);
                    change_wifi.setVisibility(View.VISIBLE);
                    ProgressView.setVisibility(View.GONE);
                    cameraList.setVisibility(View.VISIBLE);
                    bossTextView.setVisibility(View.INVISIBLE);
                    list.clear();
                    list=sql();
                    adapter = new AddDeviceAdapter(getActivity(),list);
                    cameraList.setAdapter(adapter);
                    break;
                case 4:
                    refurbish.setVisibility(View.VISIBLE);
                    change_wifi.setVisibility(View.VISIBLE);
                    ProgressView.setVisibility(View.GONE);
                    cameraList.setVisibility(View.VISIBLE);
                    bossTextView.setVisibility(View.INVISIBLE);
                    Toast.makeText(getActivity(),"搜索失败",Toast.LENGTH_SHORT).show();
                    break;
                case 5:
                    Toast.makeText(getActivity(),"添加失败",Toast.LENGTH_SHORT).show();
                    break;
                case 6:
                    Toast.makeText(getActivity(),"添加成功",Toast.LENGTH_SHORT).show();
                    break;
                case 7:
                    Toast.makeText(getActivity(),"wifi用户名或密码错误",Toast.LENGTH_LONG).show();
                    break;
                case 8:
                    java_thread();
                    break;
                default:
                    if(msg.arg1 == 1)
                    {
                        Log.e("handleMessage: ", "set AP wifi failed" );
                        Toast.makeText(getActivity(), "设置wifi失败", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    if(getActivity()!=null) {
                        Log.e("handleMessage: ", "set smart wifi failed" );
                        Toast.makeText(getActivity(), "设置wifi失败", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };
}