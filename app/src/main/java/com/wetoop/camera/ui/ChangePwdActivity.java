package com.wetoop.camera.ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wetoop.camera.App;
import com.wetoop.camera.api.ResultMessage;
import com.wetoop.cameras.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by User on 2017/7/31.
 */

public class ChangePwdActivity extends Activity{
    private RelativeLayout back,changePwdClick;
    private EditText loginNameEdit,getCodeEdit,newPwdEdit;
    private TextView getCode;
    private String loginNameEditStr,getCodeEditStr,newPwdEditStr,androidId;
    private Boolean isPhone=false,isEmail=false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chang_pwd_main);

        back = (RelativeLayout)findViewById(R.id.back);
        changePwdClick = (RelativeLayout)findViewById(R.id.changePwdClick);
        getCode = (TextView)findViewById(R.id.get_code_click);
        loginNameEdit = (EditText)findViewById(R.id.loginNameEdit);
        getCodeEdit = (EditText)findViewById(R.id.getCodeEdit);
        newPwdEdit = (EditText)findViewById(R.id.newPwdEdit);
        App app = (App)getApplication();
        app.addActivity(this);
        setViewData();
    }

    private void setViewData() {
        //获取android设备的ID
        androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        getCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(loginNameEdit.getText()!=null)
                    loginNameEditStr = loginNameEdit.getText().toString().trim();
                Pattern p = Pattern.compile("[0-9]*");
                Matcher m = p.matcher(loginNameEditStr);
                if(m.matches() ){
                    isPhone=true;
                }else{
                    isEmail=true;
                }
                if(!loginNameEditStr.equals("")) {
                    final App app = (App) getApplication();
                    app.getApiService().forgot_pwd_code(loginNameEditStr, androidId, isEmail, isPhone, new Callback<ResultMessage>() {
                        @Override
                        public void success(ResultMessage resultMessage, Response response) {
                            if (resultMessage.getCode() == 200) {
                                Log.e("Register", resultMessage.getErrorMessage());
                                Toast.makeText(ChangePwdActivity.this, "验证码发送成功", Toast.LENGTH_SHORT).show();
                            } else {
                                getCode.setBackgroundResource(R.drawable.getcodebackground);
                                getCode.setText("发送验证码");
                                Toast.makeText(ChangePwdActivity.this, resultMessage.getErrorMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            getCode.setBackgroundResource(R.drawable.getcodebackground);
                            getCode.setText("发送验证码");
                            Toast.makeText(ChangePwdActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
        changePwdClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(loginNameEdit.getText()!=null)
                    loginNameEditStr = loginNameEdit.getText().toString().trim();
                if(getCodeEdit.getText()!=null)
                    getCodeEditStr = getCodeEdit.getText().toString().trim();
                if(newPwdEdit.getText()!=null)
                    newPwdEditStr = newPwdEdit.getText().toString().trim();
                Pattern p = Pattern.compile("[0-9]*");
                Matcher m = p.matcher(loginNameEditStr);
                if(m.matches() ){
                    isPhone=true;
                }else{
                    isEmail=true;
                }
                if(!loginNameEditStr.equals("")&&!getCodeEditStr.equals("")&&!newPwdEditStr.equals("")) {
                    final App app = (App) getApplication();
                    app.getApiService().forget_pwd(loginNameEditStr,newPwdEditStr, isEmail, isPhone, Integer.parseInt(getCodeEditStr), androidId ,new Callback<ResultMessage>() {
                        @Override
                        public void success(ResultMessage resultMessage, Response response) {
                            if (resultMessage.getCode() == 200) {
                                Toast.makeText(ChangePwdActivity.this, "验证码发送成功", Toast.LENGTH_SHORT).show();
                            } else {
                                getCode.setBackgroundResource(R.drawable.getcodebackground);
                                getCode.setText("发送验证码");
                                Toast.makeText(ChangePwdActivity.this, resultMessage.getErrorMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            getCode.setBackgroundResource(R.drawable.getcodebackground);
                            Toast.makeText(ChangePwdActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }
    class TimeCount extends CountDownTimer {
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);//参数依次为总时长,和计时的时间间隔
        }

        @Override
        public void onFinish() {//计时完毕时触发
            getCode.setTextColor(getResources().getColor(R.color.colorPrimary));
            getCode.setText("重新验证");
            getCode.setClickable(true);
        }

        @Override
        public void onTick(long millisUntilFinished) {//计时过程显示
            getCode.setClickable(false);
            getCode.setText(millisUntilFinished / 1000 + "秒");
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (keyCode == KeyEvent.KEYCODE_BACK){
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}
