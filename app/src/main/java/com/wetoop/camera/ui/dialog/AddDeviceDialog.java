package com.wetoop.camera.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.wetoop.cameras.R;

/**
 * Created by Administrator on 2016/7/21.
 */
public class AddDeviceDialog extends Dialog {
    private OnCustomDialogListener customDialogListener;
    private Button cancelBt;//取消按钮
    private Button comfirmBt;//确定按钮
    private String name,wifiNameStr,wifiPawStr,sn,deviceName;
    private TextView snText,deviceText;
    private int boxtype;
    private EditText wifiName,wifiPaw;
    private Context context;

    public AddDeviceDialog(Context context, String name,String deviceName,String sn, OnCustomDialogListener customDialogListener) {
        super(context);
        this.name = name;//dialog标题名称
        this.deviceName = deviceName;//设备名称
        this.sn = sn;//设备序列号
        this.context = context;
        this.customDialogListener = customDialogListener;
    }

    public interface OnCustomDialogListener {
        public void back(String usernameStr, String userpawStr,int boxtype);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_device);
        //设置标题
        setTitle(name);

        snText = (TextView)findViewById(R.id.sn);
        deviceText = (TextView)findViewById(R.id.deviceName);
        wifiName = (EditText)findViewById(R.id.wifiName);
        wifiPaw = (EditText)findViewById(R.id.wifiPwd);

        snText.setText(sn);
        deviceText.setText(deviceName);

        Spinner spinner = (Spinner) findViewById(R.id.who_group);

        boxtype=0;

        cancelBt = (Button) findViewById(R.id.cancel_dialog);
        comfirmBt = (Button) findViewById(R.id.comfirm_dialog);
        cancelBt.setOnClickListener(clickListener);
        comfirmBt.setOnClickListener(clickListener);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        boxtype = 2;
                        break;
                    case 1:
                        boxtype = 4;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.comfirm_dialog:
                    wifiNameStr = wifiName.getText().toString().trim();
                    wifiPawStr = wifiPaw.getText().toString().trim();

                    if (!wifiNameStr.equals("") && !wifiPawStr.equals("")&&boxtype!=0){
                        customDialogListener.back(wifiNameStr,wifiPawStr,boxtype);
                        AddDeviceDialog.this.dismiss();
                    }else{
                        Toast.makeText(context, "未填写完整", Toast.LENGTH_SHORT).show();
                        //customDialogListener.back_card(1);
                    }
                    break;
                case R.id.cancel_dialog:
                    AddDeviceDialog.this.dismiss();
                    break;
            }
        }
    };

}