package com.wetoop.camera.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wetoop.camera.listener.OnProgressBarListener;
import com.wetoop.cameras.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2017/1/16.
 */
public class SetLoadingDialog extends Dialog implements OnProgressBarListener {
    private OnCustomDialogListener customDialogListener;
    private String name,loadingText,numbertStr;
    private Button cancelBt;//取消按钮
    private Button comfirmBt;//确定按钮
    private Context context;
    private int position;
    private EditText numbert;
    private TextView loading;
    private ProgressBar bar;
    private Timer timer;
    private boolean hasJump=false;

    public SetLoadingDialog(Context context, String name, String loadingText, OnCustomDialogListener customDialogListener){
        super(context);
        this.name = name;//dialog标题名称
        this.context = context;
        this.loadingText = loadingText;
        this.customDialogListener = customDialogListener;
    }

    @Override
    public void onProgressChange(int current, int max){
        System.out.println("current="+current+"<->max="+max);
        /*if(current == max){
            SettingWifiDialog.this.customDialogListener.back();
            SettingWifiDialog.this.dismiss();
            bnp.setProgress(0);
        }*/
    }

    public interface OnCustomDialogListener {
        public void back();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_set_loading);
        //设置标题
        setTitle(name);
        //bnp = (NumberProgressBar)findViewById(R.id.galaeye_numberbar1);
        bar = (ProgressBar)findViewById(R.id.probar);
        loading = (TextView)findViewById(R.id.video_loading_text);
        loading.setText(loadingText);
        //bnp.setOnProgressBarListener(this);
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(!hasJump) {
                    Message msg = handler.obtainMessage();
                    msg.what = 1;
                    handler.sendMessage(msg);
                }
            }
        }, 1000, 500);
        //bar.getProgress();
        //checkThreadTalk();
    }
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    bar.incrementProgressBy(1);
                    //System.out.println("bar.incrementProgressBy="+bar.getProgress());
                    if(bar.getProgress() == 100){
                        hasJump=true;
                        SetLoadingDialog.this.customDialogListener.back();
                        SetLoadingDialog.this.dismiss();
                        bar.setProgress(0);
                    }
                    break;
            }
        }
    };
}
