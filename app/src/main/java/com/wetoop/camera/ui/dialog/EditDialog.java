package com.wetoop.camera.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wetoop.cameras.R;

/**
 * Created by User on 2018/3/19.
 */

public class EditDialog extends Dialog {
    private String title;
    private String buttonText;
    private String usernameStr;
    private String pwdStr;
    /**
     * 1：更改用户名
     * 2：更改密码
     * 3：输入用户名和密码*/
    private int editType;
    private RelativeLayout username;
    private RelativeLayout pwd;
    private RelativeLayout cancel_dialog;
    private TextView titleText;
    private AutoCompleteTextView userEdit;
    private AutoCompleteTextView pwdEdit;
    private EditDialog.OnCustomDialogListener onCustomDialogListener;
    public Button comfirmBt;
    public RelativeLayout progressBar;

    public EditDialog(Context context , String title, String buttonText , int editType, EditDialog.OnCustomDialogListener onCustomDialogListener) {
        super(context);
        this.title = title;
        this.buttonText = buttonText;
        this.editType = editType;
        this.onCustomDialogListener = onCustomDialogListener;
    }
    public interface OnCustomDialogListener {
        void back(String resultStr);
        void back(String userStr,String pwdStr);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_edit);

        userEdit = (AutoCompleteTextView) findViewById(R.id.username_edit);
        pwdEdit = (AutoCompleteTextView) findViewById(R.id.pwd_edit);
        titleText = (TextView) findViewById(R.id.title_text);
        username = (RelativeLayout) findViewById(R.id.username);
        pwd = (RelativeLayout) findViewById(R.id.pwd);
        cancel_dialog = (RelativeLayout) findViewById(R.id.cancel_dialog);
        comfirmBt = (Button) findViewById(R.id.comfirm_dialog);
        progressBar = (RelativeLayout) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        switch (editType){
            case 1://更改用户名
                username.setVisibility(View.VISIBLE);
                pwd.setVisibility(View.GONE);
                break;
            case 2://更改密码
                username.setVisibility(View.GONE);
                pwd.setVisibility(View.VISIBLE);
                break;
            case 3://输入用户名和密码
                username.setVisibility(View.VISIBLE);
                pwd.setVisibility(View.VISIBLE);
                break;
        }
        titleText.setText(title);
        comfirmBt.setText(buttonText);
        cancel_dialog.setOnClickListener(clickListener);
        comfirmBt.setOnClickListener(clickListener);
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.cancel_dialog:
                    EditDialog.this.dismiss();
                    break;
                case R.id.comfirm_dialog:
                    switch (editType){
                        case 1://更改用户名
                            usernameStr = userEdit.getText().toString().trim();
                            if (TextUtils.isEmpty(usernameStr)) {
                                userEdit.setError("用户名不能为空");
                            }else{
                                onCustomDialogListener.back(usernameStr);
                            }
                            break;
                        case 2://更改密码
                            pwdStr = pwdEdit.getText().toString().trim();
                            if (TextUtils.isEmpty(pwdStr)) {
                                pwdEdit.setError("密码不能为空");
                            }else{
                                onCustomDialogListener.back(pwdStr);
                            }
                            break;
                        case 3://输入用户名和密码
                            usernameStr = userEdit.getText().toString().trim();
                            if (TextUtils.isEmpty(usernameStr)) {
                                userEdit.setError("用户名不能为空");
                            }else {
                                pwdStr = pwdEdit.getText().toString().trim();
                                if (TextUtils.isEmpty(pwdStr))
                                    pwdEdit.setError("密码不能为空");
                                else{
                                    onCustomDialogListener.back(usernameStr,pwdStr);
                                }
                            }
                            break;
                    }
                    break;
            }
        }
    };
}
