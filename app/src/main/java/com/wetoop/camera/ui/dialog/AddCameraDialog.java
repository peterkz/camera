package com.wetoop.camera.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.wetoop.cameras.R;

/**
 * Created by User on 2017/8/18.
 */

public class AddCameraDialog extends Dialog {
    private Context context;
    private OnCustomDialogListener customDialogListener;
    private String name;
    private String sn;
    private String deviceName,deviceStr;
    private TextView snTextView;
    private EditText nameEditText;
    private Button cancelBt;//取消按钮
    private Button comfirmBt;//确定按钮
    public AddCameraDialog(Context context, String name, String deviceName, String sn, OnCustomDialogListener customDialogListener) {
        super(context);
        this.context = context;
        this.name = name;
        this.deviceName = deviceName;
        this.sn = sn;
        this.customDialogListener = customDialogListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_camera);
        snTextView = (TextView)findViewById(R.id.sn);
        nameEditText = (EditText)findViewById(R.id.name);
        setTitle(name);
        snTextView.setText(sn);
        nameEditText.setText(deviceName);
        cancelBt = (Button) findViewById(R.id.cancel_dialog);
        comfirmBt = (Button) findViewById(R.id.comfirm_dialog);
        cancelBt.setOnClickListener(clickListener);
        comfirmBt.setOnClickListener(clickListener);
    }

    public interface OnCustomDialogListener {
        public void back(String deviceStr);
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.comfirm_dialog:
                    deviceStr = nameEditText.getText().toString().trim();

                    if (!deviceStr.equals("")){
                        customDialogListener.back(deviceStr);
                        AddCameraDialog.this.dismiss();
                    }else{
                        Toast.makeText(context, "设备名称不能为空", Toast.LENGTH_SHORT).show();
                        //customDialogListener.back_card(1);
                    }
                    break;
                case R.id.cancel_dialog:
                    AddCameraDialog.this.dismiss();
                    break;
            }
        }
    };
}
