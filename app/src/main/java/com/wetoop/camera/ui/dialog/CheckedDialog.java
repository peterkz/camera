package com.wetoop.camera.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.wetoop.cameras.R;

/**
 * Created by Administrator on 2016/5/6.
 */
public class CheckedDialog extends Dialog {
    private OnCustomDialogListener customDialogListener;
    private Button cancelBt;//取消按钮
    private Button comfirmBt;//确定按钮
    private String name,usernameStr,pawStr;
    private EditText username,paw;
    private Context context;
    //private RelativeLayout ProgressView;
    //private LinearLayout linearLayout;

    public CheckedDialog(Context context, String name, OnCustomDialogListener customDialogListener) {
        super(context);
        this.name = name;//dialog标题名称
        this.context = context;
        this.customDialogListener = customDialogListener;
    }

    public interface OnCustomDialogListener {
        public void back(String usernameStr, String userpawStr);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_checked);
        //设置标题
        setTitle(name);

        username = (EditText)findViewById(R.id.r1_editText);
        paw = (EditText)findViewById(R.id.r2_editText);

        cancelBt = (Button) findViewById(R.id.cancel_dialog);
        comfirmBt = (Button) findViewById(R.id.comfirm_dialog);
        cancelBt.setOnClickListener(clickListener);
        comfirmBt.setOnClickListener(clickListener);

        //ProgressView = (RelativeLayout)findViewById(R.id.video_loading);
        //linearLayout = (LinearLayout)findViewById(R.id.dialog);
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.comfirm_dialog:
                    usernameStr = username.getText().toString().trim();
                    pawStr = paw.getText().toString().trim();

                    if (!usernameStr.equals("") && !pawStr.equals("")){
                        customDialogListener.back(usernameStr,pawStr);
                    }else{
                        Toast.makeText(context, "未填写完整", Toast.LENGTH_SHORT).show();
                        //customDialogListener.back_card(1);
                    }
                    CheckedDialog.this.dismiss();
                    break;
                case R.id.cancel_dialog:
                    CheckedDialog.this.dismiss();
                    break;
            }
        }
    };

}