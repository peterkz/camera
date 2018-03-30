package com.wetoop.camera.ui.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wetoop.cameras.R;

/**
 * Created by Administrator on 2017/5/3.
 */

public class HintDialog extends Dialog {
    private HintDialog.OnCustomDialogListener customDialogListener;
    private String name,message,comfirmStr;
    private Context context;
    public RelativeLayout cancelBt,progressBar;//取消按钮
    public Button comfirmBt;//确定按钮

    public HintDialog(Context context, String name, String message, String comfirmStr, HintDialog.OnCustomDialogListener customDialogListener){
        super(context);
        this.name = name;//dialog标题名称
        this.message = message;
        this.context = context;
        this.comfirmStr = comfirmStr;
        this.customDialogListener = customDialogListener;
    }
    public interface OnCustomDialogListener {
        void back(String startTime);
    }

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_hint);

        TextView titleText = (TextView) findViewById(R.id.titleText);
        TextView messageText = (TextView) findViewById(R.id.messageText);
        progressBar = (RelativeLayout) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        titleText.setText(name);
        messageText.setText(message);

        cancelBt = (RelativeLayout) findViewById(R.id.cancel_dialog);
        comfirmBt = (Button) findViewById(R.id.comfirm_dialog);
        cancelBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customDialogListener.back("cancel");
                HintDialog.this.dismiss();
            }
        });
        comfirmBt.setText(comfirmStr);
        comfirmBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                customDialogListener.back("logout");
                HintDialog.this.dismiss();
            }
        });
    }
}