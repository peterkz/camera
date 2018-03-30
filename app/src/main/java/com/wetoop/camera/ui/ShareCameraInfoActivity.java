package com.wetoop.camera.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wetoop.camera.App;
import com.wetoop.camera.CameraJni;
import com.wetoop.camera.api.ResultMessage;
import com.wetoop.camera.bean.ShareListBean;
import com.wetoop.camera.listview.MyListView;
import com.wetoop.camera.service.AudioService;
import com.wetoop.camera.service.VideoService;
import com.wetoop.camera.sql.CameraListSql;
import com.wetoop.camera.ui.dialog.EditDialog;
import com.wetoop.cameras.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.wetoop.camera.tools.SqlOperation.CameraListSqlUpDatePwd;

/**
 * Created by Administrator on 2017/5/24.
 */

public class ShareCameraInfoActivity extends Activity implements MyListView.IXListViewListener, AdapterView.OnItemClickListener, AbsListView.OnScrollListener{
    private MyListView listView;
    private String sn;
    private String shareCode;
    private String netId;
    private String devicePwd;
    private String getErrorMessage;
    private ArrayList<ShareListBean> cameraList = new ArrayList<>();
    private ProgressBar pb;
    private TextView noMyShareDate;
    private View parentView;
    private PopupWindow pop = null;
    private LinearLayout ll_popup;
    private ProgressDialog checkPwdDialog,getShareCodeDialog,addShareDialog;
    private EditDialog editDialog;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_camera_info);
        parentView =  getLayoutInflater().inflate(R.layout.activity_share_camera_info, null);
        listView = (MyListView)findViewById(R.id.shareListView) ;
        pb = (ProgressBar)findViewById(R.id.progressBar) ;
        noMyShareDate = (TextView)findViewById(R.id.noMyShareDate);

        Intent intent = getIntent();
        if(intent.getStringExtra("sn")!=null){
            sn = intent.getStringExtra("sn");
        }
        if(intent.getStringExtra("netId")!=null){
            netId = intent.getStringExtra("netId");
        }
        if(intent.getStringExtra("devicePwd")!=null){
            devicePwd = intent.getStringExtra("devicePwd");
        }

        getListData();
        initDialog();
        popWindow();
        RelativeLayout addShare = (RelativeLayout) findViewById(R.id.addShare);
        addShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popShow();
            }
        });
        RelativeLayout back = (RelativeLayout) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent(ShareCameraInfoActivity.this, VideoService.class);
                intent2.setPackage(getPackageName());
                ShareCameraInfoActivity.this.stopService(intent2);
                App app = (App)getApplication();
                int tcpResult = CameraJni.tcpAuthResult(app.getAudioPort(),true);
                if(tcpResult != -1)
                    CameraJni.tcpRemove(app.getAudioPort(),true);
                CameraJni.tcpStop(true);
                app.setTcpStop(1);
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        ClipboardManager mClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clipData = mClipboard.getPrimaryClip();
        if(clipData!=null) {
            ClipData.Item item = clipData.getItemAt(0);
            if(item!=null) {
                String clipboardStr = item.getText().toString();
                if(clipboardStr.indexOf("#GALA")>0)
                    addShareApi(clipboardStr);
            }
        }
    }

    private void initDialog() {
        checkPwdDialog = new ProgressDialog(this);
        checkPwdDialog.setTitle("验证密码");
        checkPwdDialog.setMessage("正在验证密码长度···");
        checkPwdDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消验证", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        checkPwdDialog.setCancelable(false);
        checkPwdDialog.setCanceledOnTouchOutside(false);
        getShareCodeDialog = new ProgressDialog(this);
        getShareCodeDialog.setTitle("获取共享码");
        getShareCodeDialog.setMessage("正在获取共享码···");
        getShareCodeDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消获取", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        getShareCodeDialog.setCancelable(false);
        getShareCodeDialog.setCanceledOnTouchOutside(false);
        addShareDialog = new ProgressDialog(this);
        addShareDialog.setTitle("添加共享");
        addShareDialog.setMessage("正在添加共享···");
        addShareDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        addShareDialog.setCancelable(false);
        addShareDialog.setCanceledOnTouchOutside(false);
    }

    private void checkPwd(){
        if(devicePwd.length()<8){
            checkPwdDialog.dismiss();
            Toast.makeText(ShareCameraInfoActivity.this,"密码长度小于8位，请重新设置密码",Toast.LENGTH_SHORT).show();
            changePwd();
        }else{
            checkPwdDialog.dismiss();
            Toast.makeText(ShareCameraInfoActivity.this,"验证通过",Toast.LENGTH_SHORT).show();
            getShareCode(devicePwd);
        }
    }

    private void changePwd(){
        editDialog = new EditDialog(ShareCameraInfoActivity.this, "重设密码", "确定", 2, new EditDialog.OnCustomDialogListener() {
            @Override
            public void back(String resultStr) {
                editDialog.progressBar.setVisibility(View.VISIBLE);
                editDialog.comfirmBt.setVisibility(View.GONE);
                setPwd(resultStr);
            }

            @Override
            public void back(String userStr, String pwdStr) {

            }
        });
        editDialog.show();
    }

    private void setPwd(final String pwd) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run(){
                App app = (App)getApplication();
                int ret = CameraJni.setPwd(app.getAdminName(), devicePwd, "127.0.0.1", app.getAudioPortConn(),pwd);
                if(ret>=0){
                    CameraListSql address = new CameraListSql(ShareCameraInfoActivity.this);
                    address.insert(CameraListSqlUpDatePwd(netId,pwd));
                    Message msg =mHandler_code.obtainMessage();
                    msg.what = 3;
                    mHandler_code.sendMessage(msg);
                }else{
                    Message msg =mHandler_code.obtainMessage();
                    msg.what = 5;
                    mHandler_code.sendMessage(msg);
                }
            }
        });
        t.start();
    }

    private void getShareCode(String devicePwd){
        getShareCodeDialog.show();
        App app = (App)getApplication();
        app.getApiService().share_code(app.getToken(),sn ,1,devicePwd, new Callback<ResultMessage>() {
            @Override
            public void success(ResultMessage resultMessage, Response response) {
                getShareCodeDialog.dismiss();
                if(resultMessage.getCode()==200){
                    shareCode = resultMessage.getErrorMessage();
                    //addShareApi();
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT,shareCode);
                    sendIntent.setType("text/plain");
                    startActivity(sendIntent);
                }else {
                    getErrorMessage = resultMessage.getErrorMessage();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                getShareCodeDialog.dismiss();
                Toast.makeText(ShareCameraInfoActivity.this,"网络连接失败",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addShareApi(String shareCode) {
        addShareDialog.show();
        App app = (App)getApplication();
        app.getApiService().share_add(app.getToken(),shareCode , new Callback<ResultMessage>() {
            @Override
            public void success(ResultMessage resultMessage, Response response){
                addShareDialog.dismiss();
                if(resultMessage.getCode()==200){
                    getErrorMessage = resultMessage.getErrorMessage();
                    Toast.makeText(ShareCameraInfoActivity.this,getErrorMessage,Toast.LENGTH_SHORT).show();
                }else {
                    getErrorMessage = resultMessage.getErrorMessage();
                    Toast.makeText(ShareCameraInfoActivity.this,getErrorMessage,Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                addShareDialog.dismiss();
                Toast.makeText(ShareCameraInfoActivity.this,"网络连接失败",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getListData() {
        App app = (App)getApplication();
        app.getApiService().share_list(app.getToken(),sn,new Callback<ResultMessage>(){
            @Override
            public void success(ResultMessage resultMessage, Response response){
                if(resultMessage.getCode()==200){
                    cameraList.clear();
                    if(resultMessage.getShare_list()!=null){
                        int size = resultMessage.getShare_list().size();
                        for(int i=0;i<size;i++){
                            ShareListBean shareListBean = new ShareListBean();
                            shareListBean.setShareId(resultMessage.getShare_list().get(i).getShare_id());
                            shareListBean.setShareTo(resultMessage.getShare_list().get(i).getShare_to());
                            cameraList.add(shareListBean);
                        }
                    }
                    if(cameraList.size()>0){
                        Message msg = new Message();
                        msg.what = 1;//标志是哪个线程传数据
                        mHandler_code.sendMessage(msg);//发送message信息
                        getData(cameraList);
                    }else{
                        Message msg = new Message();
                        msg.what = 2;//标志是哪个线程传数据
                        mHandler_code.sendMessage(msg);//发送message信息
                    }

                }
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(ShareCameraInfoActivity.this,"网络连接失败",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getData(ArrayList<ShareListBean> cameraList) {
        onLoad();
        listView.setVisibility(View.VISIBLE);
        pb.setVisibility(View.GONE);
        MyShowAdapter myadapter = new MyShowAdapter(this,cameraList.size(),cameraList);
        listView.setAdapter(myadapter);
    }

    public void popShow() {
        ll_popup.startAnimation(AnimationUtils.loadAnimation(this, R.anim.activity_translate_in));
        pop.showAtLocation(parentView, Gravity.BOTTOM, 0, 0);
    }
    private void popWindow() {
        pop = new PopupWindow(this);
        View view = getLayoutInflater().inflate(R.layout.share_list_ppw, null);

        ll_popup = (LinearLayout) view.findViewById(R.id.ll_popup);

        pop.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        pop.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        pop.setBackgroundDrawable(new BitmapDrawable());
        pop.setFocusable(true);
        pop.setOutsideTouchable(true);
        pop.setContentView(view);

        RelativeLayout parent = (RelativeLayout) view.findViewById(R.id.parent);
        Button bt1 = (Button) view
                .findViewById(R.id.get_code);
        Button bt2 = (Button) view
                .findViewById(R.id.cancel);

        parent.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                pop.dismiss();
                ll_popup.clearAnimation();
            }
        });
        bt1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                checkPwdDialog.show();
                checkPwd();
                pop.dismiss();
                ll_popup.clearAnimation();
            }
        });
        bt2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pop.dismiss();
                ll_popup.clearAnimation();
            }
        });

    }

    public class MyShowAdapter extends BaseAdapter {

        private Context context;
        private int size;
        private ArrayList<ShareListBean> cameraList = new ArrayList<>();
        private TextView name;

        public MyShowAdapter(Context context, int size,ArrayList<ShareListBean> cameraList){
            this.context = context;
            this.size = size;
            this.cameraList = cameraList;
        }

        @Override
        public int getCount(){
            // TODO Auto-generated method stub
            //return list.size();
            return size;
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            // TODO Auto-generated method stub
            if(convertView==null){
                convertView= LayoutInflater.from(context).inflate(R.layout.share_info_item, null);
                name = (TextView)convertView.findViewById(R.id.name);
            }
            if(cameraList.size()>0){
                name.setText(cameraList.get(position).getShareTo());
            }
            return convertView;
        }
    }

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
        String strDate = null;
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        strDate = format.format(date);
        return strDate;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

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
                    noMyShareDate.setVisibility(View.GONE);
                    break;
                case 2:
                    pb.setVisibility(View.GONE);
                    noMyShareDate.setVisibility(View.VISIBLE);
                    Toast.makeText(ShareCameraInfoActivity.this,getErrorMessage,Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    if(editDialog != null) editDialog.dismiss();
                    Toast.makeText(ShareCameraInfoActivity.this,"设置密码成功",Toast.LENGTH_SHORT).show();
                    break;
                case 4:
                    Toast.makeText(ShareCameraInfoActivity.this,getErrorMessage,Toast.LENGTH_SHORT).show();
                    break;
                case 5:
                    if(editDialog != null) editDialog.dismiss();
                    Toast.makeText(ShareCameraInfoActivity.this,"设置密码失败",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    public void onDestroy(){
        super.onDestroy();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode == KeyEvent.KEYCODE_BACK){
            App app = (App)getApplication();
            int tcpResult = CameraJni.tcpAuthResult(app.getAudioPort(),true);
            Intent intent = new Intent(ShareCameraInfoActivity.this, VideoService.class);
            intent.setPackage(getPackageName());
            ShareCameraInfoActivity.this.stopService(intent);
            if(tcpResult != -1)
                CameraJni.tcpRemove(app.getAudioPort(),true);
            CameraJni.tcpStop(true);
            app.setTcpStop(1);
            finish();
        }
        return false;
    }
}
