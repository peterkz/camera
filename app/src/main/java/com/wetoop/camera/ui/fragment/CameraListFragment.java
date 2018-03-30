package com.wetoop.camera.ui.fragment;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wetoop.camera.ui.AddDeviceActivity;
import com.wetoop.camera.CameraJni;
import com.wetoop.camera.ui.DeviceInfoActivity;
import com.wetoop.camera.ui.ShareCameraInfoActivity;
import com.wetoop.camera.App;
import com.wetoop.camera.ui.adapter.CameraListAdapter;
import com.wetoop.camera.ui.dialog.EditDialog;
import com.wetoop.camera.ui.dialog.HintDialog;
import com.wetoop.camera.ui.ppw.CameraListPopWindow;
import com.wetoop.camera.ui.vlc.VideoActivity;
import com.wetoop.camera.api.ResultMessage;
import com.wetoop.camera.bean.AllCameraListBean;
import com.wetoop.camera.bean.CameraPwdListBean;
import com.wetoop.camera.listener.CJMListener;
import com.wetoop.camera.listview.MyListView;
import com.wetoop.camera.service.VideoService;
import com.wetoop.camera.sql.CameraListSql;
import com.wetoop.cameras.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.wetoop.camera.tools.SqlOperation.CameraListSqlInit;
import static com.wetoop.camera.tools.SqlOperation.CameraListSqlUpDatePwd;

/**
 * Created by Administrator on 2016/4/18.
 */
public class CameraListFragment extends Fragment implements MyListView.IXListViewListener, AdapterView.OnItemClickListener, AbsListView.OnScrollListener{
    private MyListView listView;
    private int size,sizeJoin,pos=0,savePos=-1;
    private ArrayList<CameraPwdListBean> cameraPwdListArrayList=new ArrayList<>();
    private ArrayList<AllCameraListBean> allCameraListArrayList = new ArrayList<>();
    private ArrayList<AllCameraListBean> joinBeanArrayList = new ArrayList<>();
    private ArrayList<AllCameraListBean> myShareArrayList = new ArrayList<>();
    private ArrayList<AllCameraListBean> getDataToList = new ArrayList<>();
    private ProgressDialog catchTheOrderDialog;
    private String  admin_pwd,sn;
    private View parentView;
    private ProgressBar progressBar;
    private boolean isLoadMor;
    private RelativeLayout allCamera;
    private RelativeLayout joinCamera;
    private RelativeLayout myJoinCamera;
    private TextView allCameraText;
    private TextView joinCameraText;
    private TextView myJoinCameraText;
    private TextView noDate;
    private TextView noJoinDate;
    private TextView noMyShareDate;
    private int titleChange=1;
    private boolean looping = true;
    private EditDialog editDialog;
    private CameraListAdapter myAdapter;
    private CameraListPopWindow cameraListPopWindow;
    private MyBroadcastReceiver broadcastReceiverLive;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView;
        rootView = inflater.inflate(R.layout.fragment_camera_list, container, false);
        parentView =  getActivity().getLayoutInflater().inflate(R.layout.fragment_camera_list, null);
        listView = (MyListView)rootView.findViewById(R.id.camera_list);
        progressBar = (ProgressBar)rootView.findViewById(R.id.progressBar);
        listView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        cameraPwdListArrayList.clear();
        cameraPwdListArrayList = sqlList();
        allCamera = (RelativeLayout)rootView.findViewById(R.id.allCamera);
        joinCamera = (RelativeLayout)rootView.findViewById(R.id.joinCamera);
        myJoinCamera = (RelativeLayout)rootView.findViewById(R.id.myJoinCamera);
        allCameraText = (TextView)rootView.findViewById(R.id.allCameraText);
        joinCameraText = (TextView)rootView.findViewById(R.id.joinCameraText);
        myJoinCameraText = (TextView)rootView.findViewById(R.id.myJoinCameraText);
        noDate = (TextView)rootView.findViewById(R.id.noDate);
        noJoinDate = (TextView)rootView.findViewById(R.id.noJoinDate);
        noMyShareDate = (TextView)rootView.findViewById(R.id.noMyShareDate);
        getListData();//获取摄像头列表数据
        initDialog();
        setViewClickListener(rootView);
        return rootView;
    }

    private void setViewClickListener(View rootView){
        allCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noJoinDate.setVisibility(View.GONE);
                noMyShareDate.setVisibility(View.GONE);
                titleChange=1;
                allCamera.setBackgroundColor(getResources().getColor(R.color.action_bar_color_text));
                allCameraText.setTextColor(getResources().getColor(R.color.action_bar_color));
                joinCamera.setBackgroundColor(getResources().getColor(R.color.action_bar_color));
                joinCameraText.setTextColor(getResources().getColor(R.color.action_bar_color_text));
                myJoinCamera.setBackgroundColor(getResources().getColor(R.color.action_bar_color));
                myJoinCameraText.setTextColor(getResources().getColor(R.color.action_bar_color_text));
                if(getActivity()!=null){
                    getDataToList.clear();
                    if(myAdapter!=null)
                        myAdapter.notifyDataSetChanged();
                    listView.setVisibility(View.VISIBLE);
                    getDataToList.addAll(allCameraListArrayList);
                    if(getDataToList.size() > 0) {
                        getData(size, getDataToList);
                    }else{
                        listView.setVisibility(View.INVISIBLE);
                        noDate.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
        joinCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noMyShareDate.setVisibility(View.GONE);
                noDate.setVisibility(View.GONE);
                titleChange=2;
                allCamera.setBackgroundColor(getResources().getColor(R.color.action_bar_color));
                allCameraText.setTextColor(getResources().getColor(R.color.action_bar_color_text));
                joinCamera.setBackgroundColor(getResources().getColor(R.color.action_bar_color_text));
                joinCameraText.setTextColor(getResources().getColor(R.color.action_bar_color));
                myJoinCamera.setBackgroundColor(getResources().getColor(R.color.action_bar_color));
                myJoinCameraText.setTextColor(getResources().getColor(R.color.action_bar_color_text));
                if(getActivity()!=null){
                    getDataToList.clear();
                    if(myAdapter!=null)
                        myAdapter.notifyDataSetChanged();
                    if(joinBeanArrayList.size()>0){
                        listView.setVisibility(View.VISIBLE);
                        getDataToList.addAll(joinBeanArrayList);
                        getData(size,getDataToList);
                    }else{
                        listView.setVisibility(View.INVISIBLE);
                        noJoinDate.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
        myJoinCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noJoinDate.setVisibility(View.GONE);
                noDate.setVisibility(View.GONE);
                titleChange=3;
                allCamera.setBackgroundColor(getResources().getColor(R.color.action_bar_color));
                allCameraText.setTextColor(getResources().getColor(R.color.action_bar_color_text));
                joinCamera.setBackgroundColor(getResources().getColor(R.color.action_bar_color));
                joinCameraText.setTextColor(getResources().getColor(R.color.action_bar_color_text));
                myJoinCamera.setBackgroundColor(getResources().getColor(R.color.action_bar_color_text));
                myJoinCameraText.setTextColor(getResources().getColor(R.color.action_bar_color));
                if(getActivity()!=null){
                    getDataToList.clear();
                    if(myAdapter!=null)
                        myAdapter.notifyDataSetChanged();
                    if(myShareArrayList.size()>0){
                        listView.setVisibility(View.VISIBLE);
                        getDataToList.addAll(myShareArrayList);
                        getData(size,getDataToList);
                    }else{
                        listView.setVisibility(View.INVISIBLE);
                        noMyShareDate.setVisibility(View.VISIBLE);
                    }

                }
            }
        });
        listView.setXListViewListener(this);//设置刷新加载监听
        listView.setOnScrollListener(this);//设置滑动隐藏头部选择栏
        listView.setOnItemClickListener(this);//设置item点击监听
        RelativeLayout add = (RelativeLayout)rootView.findViewById(R.id.add_user);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),AddDeviceActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initDialog() {
        catchTheOrderDialog = new ProgressDialog(getActivity());
        catchTheOrderDialog.setTitle("连接摄像头");
        catchTheOrderDialog.setMessage("正在连接摄像头···");
        catchTheOrderDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消连接", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                looping = false;
            }
        });
        catchTheOrderDialog.setCancelable(false);
        catchTheOrderDialog.setCanceledOnTouchOutside(false);

        broadcastReceiverLive = new MyBroadcastReceiver();
        IntentFilter intentFilterLive = new IntentFilter("videoPortChange");
        getActivity().registerReceiver(broadcastReceiverLive, intentFilterLive);

    }

    private void getListData(){
        App application = (App) getActivity().getApplication();
        if(application.getToken()!=null){
            if(!application.getToken().equals("")){
                application.getApiService().cameraList(application.getToken(), new Callback<ResultMessage>() {

                    @Override
                    public void success(ResultMessage resultMessage, Response response) {
                        if(resultMessage.getCode()==200){
                            App application = (App) getActivity().getApplication();
                            application.setNode(resultMessage.getNode());
                            joinBeanArrayList.clear();
                            allCameraListArrayList.clear();
                            myShareArrayList.clear();
                            cameraPwdListArrayList.clear();
                            if(resultMessage.getCameraInfos()!=null){
                                size = resultMessage.getCameraInfos().size();
                                for(int i=0;i<resultMessage.getCameraInfos().size();i++){
                                    AllCameraListBean camera_list = new AllCameraListBean();
                                    camera_list.setSn(resultMessage.getCameraInfos().get(i).getSn());
                                    camera_list.setNet_id(resultMessage.getCameraInfos().get(i).getNet_id());
                                    camera_list.setShareNum(resultMessage.getCameraInfos().get(i).getShare());
                                    camera_list.setOnline(resultMessage.getCameraInfos().get(i).getOnline());
                                    camera_list.setJoin(false);
                                    camera_list.setDeviceName("Mini");
                                    allCameraListArrayList.add(camera_list);
                                    if(resultMessage.getCameraInfos().get(i).getShare()>0){
                                        myShareArrayList.add(camera_list);
                                    }
                                    if(cameraPwdListArrayList.size()>0){
                                        int count=0;
                                        for(int j=0;j<cameraPwdListArrayList.size();j++){
                                            if(!cameraPwdListArrayList.get(j).getNetID().equals(camera_list.getNet_id())){
                                                count=1;
                                            }
                                        }
                                        if(count==1){
                                            if(getActivity()!=null){
                                                CameraListSql address = new CameraListSql(getContext());
                                                address.insert(CameraListSqlInit(camera_list.getNet_id(),""));
                                            }
                                        }

                                    }else {
                                        if(getActivity()!=null){
                                            CameraListSql address = new CameraListSql(getContext());
                                            address.insert(CameraListSqlInit(camera_list.getNet_id(),""));
                                        }
                                    }
                                }

                            }
                            if(resultMessage.getCameraJoined()!=null){
                                sizeJoin = resultMessage.getCameraJoined().size();
                                for(int i=0;i<sizeJoin;i++){
                                    AllCameraListBean joinList = new AllCameraListBean();
                                    joinList.setNet_id(resultMessage.getCameraJoined().get(i).getNet_id());
                                    joinList.setOnline(resultMessage.getCameraJoined().get(i).getOnline());
                                    joinList.setShareId(resultMessage.getCameraJoined().get(i).getShare_id());
                                    joinList.setFrom(resultMessage.getCameraJoined().get(i).getFrom());
                                    joinList.setJoin(true);
                                    allCameraListArrayList.add(joinList);
                                    joinBeanArrayList.add(joinList);
                                }
                            }

                            if(getActivity()!=null){
                                if(titleChange==1){
                                    getDataToList.clear();
                                    getDataToList.addAll(allCameraListArrayList);
                                    Message msg = new Message();
                                    msg.what = 1;
                                    mHandler_code.sendMessage(msg);
                                    getData(size,getDataToList);
                                }else if(titleChange==2){
                                    getDataToList.clear();
                                    Message msg = new Message();
                                    msg.what = 2;
                                    mHandler_code.sendMessage(msg);
                                    if(joinBeanArrayList.size()>0){
                                        getDataToList.addAll(joinBeanArrayList);
                                        getData(size,joinBeanArrayList);
                                    }else{
                                        Message msg1 = new Message();
                                        msg1.what = 3;
                                        mHandler_code.sendMessage(msg1);
                                    }
                                }else if(titleChange==3){
                                    getDataToList.clear();
                                    Message msg = new Message();
                                    msg.what = 4;
                                    mHandler_code.sendMessage(msg);
                                    if(myShareArrayList.size()>0){
                                        getDataToList.addAll(myShareArrayList);
                                        getData(size,getDataToList);
                                    }else{
                                        Message msg1 = new Message();
                                        msg1.what = 5;
                                        mHandler_code.sendMessage(msg1);
                                    }
                                }
                            }
                        }else{
                            listView.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getActivity(), resultMessage.getErrorMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if(getActivity()!=null){
                            listView.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getActivity(),"服务器未响应",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    }

    public void checkPwd(){
        cameraPwdListArrayList = sqlList();
        String devicePwd = cameraPwdListArrayList.get(pos).getDevicePwd();
        if(TextUtils.isEmpty(devicePwd)){
            changePwd();
        }else{
            App app = (App)getActivity().getApplication();
            app.setVideoPos(pos);
            catchTheOrderDialog.show();
            admin_pwd = cameraPwdListArrayList.get(pos).getDevicePwd();

            Intent intent2 = new Intent(getActivity(), VideoService.class);
            intent2.setPackage(getActivity().getPackageName());
            intent2.putExtra("netId", getDataToList.get(app.getVideoPos()).getNet_id());
            intent2.putExtra("usernameStr", app.getLoginNetId());
            intent2.putExtra("userpawStr", app.getLoginNetToken());
            intent2.putExtra("port_conn", app.getVideoPortConn());
            intent2.putExtra("port", app.getVideoPort());
            intent2.putExtra("node",app.getNode());
            intent2.putExtra("audioFrom","false");
            getActivity().startService(intent2);
            startPlayThread();
            //showContacts();
        }
    }

    private void changePwd(){
        editDialog = new EditDialog(getActivity(), "输入密码", "确定", 2, new EditDialog.OnCustomDialogListener() {
            @Override
            public void back(String resultStr) {
                editDialog.progressBar.setVisibility(View.VISIBLE);
                editDialog.comfirmBt.setVisibility(View.GONE);
                CameraListSql address = new CameraListSql(getActivity());
                String netId = cameraPwdListArrayList.get(pos).getNetID();
                address.insert(CameraListSqlUpDatePwd(netId,resultStr));
                editDialog.dismiss();
                Toast.makeText(getActivity(),"输入密码成功",Toast.LENGTH_SHORT).show();
                checkPwd();
            }

            @Override
            public void back(String userStr, String pwdStr) {

            }
        });
        editDialog.show();
    }

    private ArrayList<CameraPwdListBean> sqlList(){
        CameraListSql cameraSql = new CameraListSql(getContext());
        return cameraSql.queryDataToSQLite();
    }

    private void startPlayThread(){
        new Thread(new Runnable() {

            @Override
            public void run() {
                App app = (App) getActivity().getApplication();
                try {
                    Thread.sleep(4000);//进入线程时先睡2s
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
                looping = true;
                while (looping) {
                    if (app.getBroadcast() == 1) {
                        app.setBroadcast(0);
                        looping = false;
                        if (getDataToList.get(app.getVideoPos()).isJoin()) {
                            CameraJni.addListener(new CJMListener() {
                                @Override
                                public void eventOccurred(final long useNum, String dataStr) {
                                    String s0[] = dataStr.split("realm=\"");
                                    String s1[] = s0[0].split("RTSP/1.0");
                                    String s2[] = s1[0].split(" rtsp://");//s2[0]=方法名m
                                    String s3[] = s0[1].split("\",nonce=\"");//s3[0]=认证r
                                    String s4[] = s3[1].split("\",uri=\"");//s4[0]=认证nonce
                                    String s5[] = s4[1].split("\",response=\"");//s5[0]=认证url
                                    App app = (App) getActivity().getApplication();
                                    app.getApiService().share_auth(app.getToken(), joinBeanArrayList.get(pos).getShareId(), s2[0], s0[1], s4[0], s5[0], new Callback<ResultMessage>() {
                                        @Override
                                        public void success(ResultMessage resultMessage, Response response) {
                                            if (resultMessage.getCode() == 200) {
                                                CameraJni.getUseNum(resultMessage.getErrorMessage(), useNum);
                                            } else {
                                                Toast.makeText(getActivity(), resultMessage.getErrorMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void failure(RetrofitError error) {

                                        }
                                    });
                                }
                            });
                        }
                        catchTheOrderDialog.dismiss();
                        Intent intent = new Intent();
                        intent.setClass(getActivity(),
                                VideoActivity.class);
                        if (getDataToList.get(app.getVideoPos()).isJoin()) {
                            intent.putExtra("sn", getDataToList.get(app.getVideoPos()).getFrom());
                        } else {
                            intent.putExtra("sn", getDataToList.get(app.getVideoPos()).getSn());
                        }
                        cameraPwdListArrayList = sqlList();
                        int pos = app.getVideoPos();
                        app.setTcpStart("false");
                        intent.putExtra("netId", getDataToList.get(pos).getNet_id());
                        intent.putExtra("deviceName", getDataToList.get(pos).getDeviceName());
                        intent.putExtra("devicePwd", cameraPwdListArrayList.get(pos).getDevicePwd());
                        intent.putExtra("admin_pwd", admin_pwd);
                        startActivity(intent);
                    }
                }
            }
        }).start();
    }
    private void getData(int size,ArrayList<AllCameraListBean> cameraList) {
        onLoad();
        listView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        myAdapter = new CameraListAdapter(getActivity(),size,cameraList,listView);
        listView.setAdapter(myAdapter);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                pos=position-1;
                sn = getDataToList.get(pos).getSn();
                userImage();
                return true;
            }
        });
    }

    public void userImage() {
        cameraPwdListArrayList = sqlList();
        String netId = cameraPwdListArrayList.get(pos).getNetID();
        if(getDataToList.get(pos).isJoin()){
            cameraListPopWindow = new CameraListPopWindow(CameraListFragment.this,1,netId);
        }else{
            cameraListPopWindow = new CameraListPopWindow(CameraListFragment.this,2,netId);
        }
        cameraListPopWindow.ll_popup.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.activity_translate_in));
        cameraListPopWindow.showAtLocation(parentView, Gravity.BOTTOM, 0, 0);
    }

    public void cameraInfo(){
        App app = (App)getActivity().getApplication();
        app.setVideoPos(pos);
        app.setTcpStart("false");
        cameraPwdListArrayList = sqlList();
        Intent intent = new Intent(getActivity(),DeviceInfoActivity.class);
        intent.putExtra("sn",getDataToList.get(app.getVideoPos()).getSn());
        intent.putExtra("netId",getDataToList.get(app.getVideoPos()).getNet_id());
        intent.putExtra("deviceName",getDataToList.get(app.getVideoPos()).getDeviceName());
        intent.putExtra("devicePwd",cameraPwdListArrayList.get(app.getVideoPos()).getDevicePwd());
        startActivity(intent);
    }

    public void cameraShare(){
        catchTheOrderDialog.show();
        App app = (App)getActivity().getApplication();
        Intent intent2 = new Intent(getActivity(), VideoService.class);
        intent2.setPackage(getActivity().getPackageName());
        intent2.putExtra("netId", getDataToList.get(app.getVideoPos()).getNet_id());
        intent2.putExtra("usernameStr", app.getLoginNetId());
        intent2.putExtra("userpawStr", app.getLoginNetToken());
        intent2.putExtra("port_conn", app.getVideoPortConn());
        intent2.putExtra("port", app.getVideoPort());
        intent2.putExtra("node",app.getNode());
        intent2.putExtra("audioFrom","false");
        getActivity().startService(intent2);

        new Thread(new Runnable() {

            @Override
            public void run() {
                App app = (App) getActivity().getApplication();
                try {
                    Thread.sleep(2000);//进入线程时先睡2s
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                looping = true;
                while (looping) {
                    if (app.getBroadcast() == 1) {
                        app.setBroadcast(0);
                        looping = false;
                        cameraPwdListArrayList = sqlList();
                        app.setTcpStart("false");
                        catchTheOrderDialog.dismiss();
                        Intent intent = new Intent(getActivity(), ShareCameraInfoActivity.class);
                        intent.putExtra("sn", sn);
                        intent.putExtra("netId", cameraPwdListArrayList.get(pos).getNetID());
                        intent.putExtra("devicePwd", cameraPwdListArrayList.get(pos).getDevicePwd());
                        startActivity(intent);
                    }
                }
            }
        }).start();
    }

    public void cameraDelete(){
        HintDialog addDialog = new HintDialog(getActivity(), "提示","是否要删除摄像头","删除", new HintDialog.OnCustomDialogListener() {
            @Override
            public void back(String startTime) {
                if (startTime.equals("logout")) {
                    if(getDataToList.get(pos).isJoin()){
                        cameraShareRemove();
                    }else{
                        cameraRemove();
                    }
                }
            }
        });
        addDialog.show();
    }

    //用于删除自己的摄像头
    private void cameraRemove(){

        App application = (App)getActivity().getApplication();
        if(application.getToken()!=null){
            if(!application.getToken().equals("")){
                application.getApiService().camera_remove(application.getToken(),sn, new Callback<ResultMessage>() {

                    @Override
                    public void success(ResultMessage resultMessage, Response response) {
                        if (resultMessage.getCode() == 200) {
                            Toast.makeText(getActivity(), resultMessage.getErrorMessage(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), resultMessage.getErrorMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Toast.makeText(getActivity(), "服务器未响应", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }
    //用于删除分享的摄像头
    private void cameraShareRemove(){

        App application = (App)getActivity().getApplication();
        if(application.getToken()!=null){
            if(!application.getToken().equals("")){
                application.getApiService().share_remove(application.getToken(),joinBeanArrayList.get(pos).getShareId(), new Callback<ResultMessage>() {

                    @Override
                    public void success(ResultMessage resultMessage, Response response) {
                        if (resultMessage.getCode() == 200) {
                            Toast.makeText(getActivity(), resultMessage.getErrorMessage(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), resultMessage.getErrorMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Toast.makeText(getActivity(), "服务器未响应", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    int lastPosition = 0;// 上次滚动到的第一个可见元素在listview里的位置——firstVisibleItem
    int state = SCROLL_STATE_IDLE;
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (firstVisibleItem == 0) {

            if (isLoadMor) {
                isLoadMor = false;
            }
        }
        if (firstVisibleItem > 0) {

            if (firstVisibleItem > lastPosition && state == SCROLL_STATE_FLING) {
            } else {
                isLoadMor = false;
            }

            if (firstVisibleItem < lastPosition && state == SCROLL_STATE_FLING) {
                if (isLoadMor) {
                    isLoadMor = false;
                }
            }
        }
        lastPosition = firstVisibleItem;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(broadcastReceiverLive);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        pos=position-1;
        sn = getDataToList.get(pos).getSn();
        if(getDataToList.get(pos).getOnline().equals("0")){
            Toast.makeText(getActivity(),"摄像头不在线",Toast.LENGTH_SHORT).show();
        }else
            checkPwd();
    }
    /**
     * listview头部加载情况
     */
    private void onLoad() {
        listView.stopRefresh();
        listView.stopLoadMore();
        listView.setRefreshTime(getNowDate());//下拉时显示上次刷新时间
    }
    /**
     * 获取当前系统时间
     *
     * @return 返回时间类型（yy月）
     */
    public static String getNowDate() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        return format.format(date);
    }
    @Override
    public void onRefresh() {
        getListData();
    }

    @Override
    public void onLoadMore() {

    }

    Handler mHandler_code = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    noJoinDate.setVisibility(View.GONE);
                    noMyShareDate.setVisibility(View.GONE);
                    break;
                case 2:
                    noMyShareDate.setVisibility(View.GONE);
                    break;
                case 3:
                    noJoinDate.setVisibility(View.VISIBLE);
                    break;
                case 4:
                    noJoinDate.setVisibility(View.GONE);
                    break;
                case 5:
                    noMyShareDate.setVisibility(View.VISIBLE);
                    break;
            }
        }
    };

    public class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            App app = (App)getActivity().getApplication();
            if(intent.getExtras().getString("changePos") == null){
                app.setBroadcast(1);
                if("false".equals(intent.getExtras().getString("audio"))) {
                    app.setVideoPort(intent.getExtras().getInt("port"));
                    app.setVideoPortConn(intent.getExtras().getInt("port_conn"));
                }else if("true".equals(intent.getExtras().getString("audio"))){
                    app.setAudioPort(intent.getExtras().getInt("port"));
                    app.setAudioPortConn(intent.getExtras().getInt("port_conn"));
                }
            }else{
                if ("true".equals(intent.getExtras().getString("changePos"))) {
                    savePos = -1;
                }
            }
        }
    }

}