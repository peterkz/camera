package com.wetoop.camera.ui.dialog;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.wetoop.camera.CameraJni;
import com.wetoop.cameras.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Administrator on 2016/10/8.
 */
public class UploadingDialog extends ProgressDialog {
    private OnCustomDialogListener customDialogListener;
    private Button cancelBt;//取消按钮
    private Button comfirmBt;//确定按钮
    private String name,adminName,adminPwd,sn,url;
    private Context context;
    //private CirclePercentView mCirclePercentView;
    private  ProgressBar bar;
    private int[] randData = new int[100];
    private int index = 0;
    private int mProgressStatus = 0;
    private int adminPortConn = 0,contentLength=0;
    private TextView textView,progress_title;
    public int upgradeNum=0;
    private long total = 0;
    //private RelativeLayout ProgressView;
    //private LinearLayout linearLayout;

    public UploadingDialog(Context context, String name,String adminName,String adminPwd,int adminPortConn,String sn,String url, OnCustomDialogListener customDialogListener) {
        super(context);
        this.name = name;//dialog标题名称
        this.context = context;
        this.customDialogListener = customDialogListener;
        this.adminName = adminName;
        this.adminPwd = adminPwd;
        this.adminPortConn = adminPortConn;
        this.sn = sn;
        this.url = url;
    }

    public interface OnCustomDialogListener {
        public void back(int type);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.uploading_dialog);
        //mCirclePercentView = (CirclePercentView) findViewById(R.id.circleView);
        bar = (ProgressBar)findViewById(R.id.progress);
        textView = (TextView)findViewById(R.id.progressText);
        progress_title = (TextView)findViewById(R.id.progress_title);
        int n = (int)(Math.random()*100);
        //mCirclePercentView.setPercent(n);
        /*new Thread() {
            @Override
            public void run() {
                super.run();
                while(index < 100) {
                    doWork();
                    Message msg = new Message();
                    msg.what = 0;
                    mHandler.sendMessage(msg);
                }
            }

        }.start();*/
    }

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    mProgressStatus = ((int) (total * 100 / contentLength))/2;
                    textView.setText(mProgressStatus + "%");
                    progress_title.setText("下载更新的文件");
                    bar.setProgress(mProgressStatus);
                    break;
                case 1:
                    if(upgradeNum%2==0){
                        mProgressStatus = upgradeNum/2+50;
                    }
                    textView.setText(mProgressStatus + "%");
                    progress_title.setText("更新设备");
                    bar.setProgress(mProgressStatus);
                    break;
                case 2://更新失败
                    customDialogListener.back(2);
                    break;
                case 3://更新成功
                    customDialogListener.back(3);
                    break;
            }

        }

    };

    private void download(String url) {
        final String downloadUrl = url;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(downloadUrl);
                    //打开连接
                    URLConnection conn = url.openConnection();
                    //打开输入流
                    InputStream is = conn.getInputStream();
                    //获得长度
                    contentLength = conn.getContentLength();
                    //创建文件夹 MyDownLoad，在存储卡下
                    String dirName = getSDPath() + "/cameraDownLoad/";
                    File file = new File(dirName);
                    //不存在创建
                    if (!file.exists()) {
                        file.mkdir();
                    }
                    //下载后的文件名
                    String fileName = dirName + sn + ".txt";
                    File file1 = new File(fileName);
                    if (file1.exists()){
                        file1.delete();
                    }
                    //创建字节流
                    byte[] bs = new byte[1024];
                    int len;
                    OutputStream os = new FileOutputStream(fileName);
                    //写数据
                    while ((len = is.read(bs)) != -1){
                        os.write(bs, 0, len);
                        if(contentLength>0){
                            Message msg = new Message();
                            msg.what = 0;
                            mHandler.sendMessage(msg);
                        }
                    }
                    upgradeThread();
                    //完成后关闭流
                    os.close();
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();

    }

    //upgradeNum
    private void upgradeThread(){
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                String fd = getSDPath() +"/cameraDownLoad/" + sn+".txt";
                File f = new File(fd);
                FileOutputStream fOut = null;
                try {
                    fOut = new FileOutputStream(f);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                int ret = CameraJni.upgrade(adminName, adminPwd, "127.0.0.1", adminPortConn, fd);
                while (upgradeNum>100||upgradeNum<0){
                    Message msg =mHandler.obtainMessage();
                    msg.what = 1;
                    mHandler.sendMessage(msg);
                    System.out.println(upgradeNum+"%");
                }
                if(ret<0){
                    Message msg =mHandler.obtainMessage();
                    msg.what = 2;
                    mHandler.sendMessage(msg);
                }else{
                    Message msg =mHandler.obtainMessage();
                    msg.what = 3;
                    mHandler.sendMessage(msg);
                    //Toast.makeText(DeviceInfo.this,"重新获取证书失败",Toast.LENGTH_SHORT).show();
                }

            }
        });
        t.start();
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

    private int doWork() {
        randData[index++] = (int)(Math.random() * 100);
        //模拟一个比较耗时的操作
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return index;
    }

}