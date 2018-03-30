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
import android.widget.ImageView;
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
 * Created by Administrator on 2016/4/12.
 */
public class RegisterActivity extends Activity implements View.OnClickListener{
    private EditText pwd,username,auth_code;
    private String pwdStr,usernameStr,androidId;
    private int auth_codeInt=0;
    private Boolean isPhone=false,isEmail=false;
    private TextView getCode,register;
    private View mProgressView;
    private RelativeLayout ProgressView,r2,title;
    private TimeCount time;
    private ImageView rememberPwd;
    private int rememberPwdCount=0;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        username = (EditText)findViewById(R.id.username_register);
        pwd = (EditText)findViewById(R.id.pwd_register);
        auth_code = (EditText)findViewById(R.id.auth_code);
        getCode = (TextView)findViewById(R.id.get_code);
        //textView_code = (TextView)findViewById(R.id.r3_text);
        register = (TextView)findViewById(R.id.register);
        ProgressView = (RelativeLayout)findViewById(R.id.video_loading);
        r2 = (RelativeLayout)findViewById(R.id.main);
        title = (RelativeLayout)findViewById(R.id.title);
        rememberPwd = (ImageView)findViewById(R.id.rememberPwd);

        getCode.setOnClickListener(this);
        register.setOnClickListener(this);
        title.setOnClickListener(this);

        int count = 60*1000;//如果发送验证码成功，则使验证码的按钮失效60s
        time = new TimeCount(count, 1000);//构造CountDownTimer对象

        //获取android设备的ID
        androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        rememberPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(rememberPwdCount==0){
                    rememberPwd.setImageResource(R.mipmap.chance1);
                    rememberPwdCount=1;
                }else{
                    rememberPwd.setImageResource(R.mipmap.chance2);
                    rememberPwdCount=0;
                }

            }
        });

    }

    @Override
    public void onClick(View v) {
        usernameStr = username.getText().toString().trim();
        pwdStr = pwd.getText().toString().trim();
        /*if(auth_code.toString()!=null||!auth_code.toString().equals("")){
            auth_codeInt = Integer.parseInt(auth_code.toString().trim());
        }*/

        switch (v.getId()){
            case R.id.get_code:
                if(rememberPwdCount==1){
                    if(!usernameStr.equals("")&&!pwdStr.equals("")){
                        getCode.setTextColor(Color.GRAY);
                        getCode.setText("正在获取验证码");
                        Pattern p = Pattern.compile("[0-9]*");
                        Matcher m = p.matcher(usernameStr);
                        if(m.matches() ){
                            isPhone=true;
                        }else{
                            isEmail=true;
                        }
                        final App app = (App) getApplication();
                        app.getApiService().getVerifyCode(usernameStr, androidId, isEmail, isPhone, new Callback<ResultMessage>() {
                            @Override
                            public void success(ResultMessage resultMessage, Response response) {
                                if (resultMessage != null) {
                                    if (resultMessage.getCode() == 200) {
                                        getCode.setText("发送验证码");
                                        time.start();
                                        Log.e("Register", resultMessage.getErrorMessage());
                                        Toast.makeText(RegisterActivity.this, "验证码获取成功", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(RegisterActivity.this,resultMessage.getErrorMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }

                            }

                            @Override
                            public void failure(RetrofitError error) {
                                getCode.setTextColor(getResources().getColor(R.color.colorPrimary));
                                getCode.setText("发送验证码");
                                Toast.makeText(RegisterActivity.this,"服务器未响应",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }else if(rememberPwdCount==0){
                    Toast.makeText(RegisterActivity.this,"未同意服务协议",Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.register:
                if(!auth_code.getText().toString().trim().equals("")){
                    auth_codeInt = Integer.parseInt(auth_code.getText().toString().trim());
                }

                if(!usernameStr.equals("")&&!pwdStr.equals("")&&auth_codeInt!=0){
                    hideInput(RegisterActivity.this,username);
                    hideInput(RegisterActivity.this,pwd);
                    hideInput(RegisterActivity.this,auth_code);
                    ProgressView.setVisibility(View.VISIBLE);
                    r2.setVisibility(View.GONE);
                    Pattern p = Pattern.compile("[0-9]*");
                    Matcher m = p.matcher(usernameStr);
                    if(m.matches() ){
                        isPhone=true;
                    }else{
                        isEmail=true;
                    }
                    Log.e("auth_codeInt",""+auth_codeInt);
                    final App app = (App) getApplication();
                    app.getApiService().register(usernameStr, pwdStr, isEmail, isPhone, auth_codeInt, androidId, new Callback<ResultMessage>() {
                        @Override
                        public void success(ResultMessage resultMessage, Response response) {
                            if(resultMessage !=null){
                                if(resultMessage.getCode()==200){
                                    Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                    Toast.makeText(RegisterActivity.this,resultMessage.getErrorMessage(),Toast.LENGTH_LONG).show();
                                }else{
                                    ProgressView.setVisibility(View.GONE);
                                    r2.setVisibility(View.VISIBLE);
                                    Toast.makeText(RegisterActivity.this,resultMessage.getErrorMessage(),Toast.LENGTH_LONG).show();
                                }
                            }
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            ProgressView.setVisibility(View.GONE);
                            r2.setVisibility(View.VISIBLE);
                            Toast.makeText(RegisterActivity.this,"服务器未响应",Toast.LENGTH_SHORT).show();
                        }
                    });
                }else{
                    Toast.makeText(RegisterActivity.this,"未填写完整",Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.title:
                Intent intent = new Intent(RegisterActivity.this,LoginRegisterActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.rememberPwd:
                if(rememberPwdCount==0){
                    rememberPwd.setImageResource(R.mipmap.chance1);
                    rememberPwdCount=1;
                }else{
                    rememberPwd.setImageResource(R.mipmap.chance2);
                    rememberPwdCount=0;
                }
                break;
        }
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

    /**
     * 强制隐藏输入法键盘
     */
    private void hideInput(Context context,View view){
        InputMethodManager inputMethodManager =
                (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (keyCode == KeyEvent.KEYCODE_BACK){
            Intent intent1 = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent1);
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}
