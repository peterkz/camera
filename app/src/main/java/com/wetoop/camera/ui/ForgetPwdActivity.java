package com.wetoop.camera.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
 * Created by Administrator on 2016/4/14.
 */
public class ForgetPwdActivity extends Activity implements View.OnClickListener{
    private EditText pwd,username,auth_code;
    private String pwdStr,usernameStr,androidId;
    private RelativeLayout getCode,register,back,ProgressView,r2;
    private Boolean isPhone=false,isEmail=false;
    private int auth_codeInt=0;
    private TextView textView_code;
    private TimeCount time;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_pwd);

        ProgressView = (RelativeLayout)findViewById(R.id.video_loading);
        r2 = (RelativeLayout)findViewById(R.id.main);
        username = (EditText)findViewById(R.id.username_forget);
        pwd = (EditText)findViewById(R.id.pwd_forget);
        auth_code = (EditText)findViewById(R.id.auth_code_forget);
        getCode = (RelativeLayout)findViewById(R.id.r3);
        textView_code = (TextView)findViewById(R.id.r3_text);
        register = (RelativeLayout)findViewById(R.id.r5);
        back = (RelativeLayout)findViewById(R.id.back);
        getCode.setOnClickListener(this);
        register.setOnClickListener(this);
        back.setOnClickListener(this);

        int count = 60*1000;//如果发送验证码成功，则使验证码的按钮失效60s
        time = new TimeCount(count, 1000);//构造CountDownTimer对象

        //获取android设备的ID
        androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    @Override
    public void onClick(View v) {
        usernameStr = username.getText().toString().trim();
        pwdStr = pwd.getText().toString().trim();
        switch (v.getId()){
            case R.id.r3:
                if(!usernameStr.equals("")&&!pwdStr.equals("")){
                    getCode.setBackgroundColor(Color.GRAY);
                    textView_code.setText("正在获取验证码");
                    Pattern p = Pattern.compile("[0-9]*");
                    Matcher m = p.matcher(usernameStr);
                    if(m.matches() ){
                        isPhone=true;
                    }else{
                        isEmail=true;
                    }
                    final App app = (App) getApplication();
                    app.getApiService().forgot_pwd_code(usernameStr, androidId, isEmail, isPhone, new Callback<ResultMessage>() {
                        @Override
                        public void success(ResultMessage resultMessage, Response response) {
                            if (resultMessage.getCode() == 200) {
                                time.start();
                                Log.e("Register", resultMessage.getErrorMessage());
                                Toast.makeText(ForgetPwdActivity.this, "验证码发送成功", Toast.LENGTH_SHORT).show();
                            } else {
                                getCode.setBackground(getResources().getDrawable(R.drawable.getcodebackground));
                                textView_code.setText("发送验证码");
                                Toast.makeText(ForgetPwdActivity.this, resultMessage.getErrorMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            getCode.setBackground(getResources().getDrawable(R.drawable.getcodebackground));
                            textView_code.setText("发送验证码");
                            Log.e("Register", error.getMessage());
                        }
                    });
                }
                break;
            case R.id.r5:
                if(!auth_code.getText().toString().trim().equals("")){
                    auth_codeInt = Integer.parseInt(auth_code.getText().toString().trim());
                }

                if(!usernameStr.equals("")&&!pwdStr.equals("")&&auth_codeInt!=0){
                    ProgressView.setVisibility(View.VISIBLE);
                    r2.setVisibility(View.GONE);
                    hideInput(ForgetPwdActivity.this,username);
                    hideInput(ForgetPwdActivity.this,pwd);
                    hideInput(ForgetPwdActivity.this,auth_code);
                    Pattern p = Pattern.compile("[0-9]*");
                    Matcher m = p.matcher(usernameStr);
                    if(m.matches() ){
                        isPhone=true;
                    }else{
                        isEmail=true;
                    }
                    Log.e("auth_codeInt",""+auth_codeInt);
                    final App app = (App) getApplication();
                    app.getApiService().forget_pwd(usernameStr, pwdStr, isEmail, isPhone, auth_codeInt, androidId, new Callback<ResultMessage>() {
                        @Override
                        public void success(ResultMessage resultMessage, Response response) {
                            if (resultMessage != null) {
                                if (resultMessage.getCode() == 200) {
                                    Toast.makeText(ForgetPwdActivity.this, "重置密码成功", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(ForgetPwdActivity.this,LoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    ProgressView.setVisibility(View.GONE);
                                    r2.setVisibility(View.VISIBLE);
                                    Toast.makeText(ForgetPwdActivity.this, resultMessage.getErrorMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            ProgressView.setVisibility(View.GONE);
                            r2.setVisibility(View.VISIBLE);
                            Toast.makeText(ForgetPwdActivity.this,"服务器未响应",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                break;
            case R.id.back:
                Intent intent = new Intent(ForgetPwdActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }

    class TimeCount extends CountDownTimer {
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);//参数依次为总时长,和计时的时间间隔
        }

        @Override
        public void onFinish() {//计时完毕时触发
            getCode.setBackground(getResources().getDrawable(R.drawable.getcodebackground));
            textView_code.setText("重新验证");
            getCode.setClickable(true);
        }

        @Override
        public void onTick(long millisUntilFinished) {//计时过程显示
            getCode.setClickable(false);
            textView_code.setText(millisUntilFinished / 1000 + "秒");
        }
    }

    /**
     * 强制隐藏输入法键盘
     */
    private void hideInput(Context context,View view){
        InputMethodManager inputMethodManager =
                (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            Intent intent = new Intent(ForgetPwdActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return false;
        }else {
            return super.onKeyDown(keyCode, event);
        }
    }
}
