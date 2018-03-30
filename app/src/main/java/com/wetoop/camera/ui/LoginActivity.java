package com.wetoop.camera.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wetoop.camera.App;
import com.wetoop.camera.api.ResultMessage;
import com.wetoop.camera.sql.WifiSql;
import com.wetoop.cameras.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Administrator on 2016/4/18.
 */
public class LoginActivity extends AppCompatActivity {

    private final static String TAG = "MainActivity";
    private EditText username,userpaw;
    private String usernameStr,userpawStr,androidId;
    private App appCard;
    private Boolean isPhone=false,isEmail=false;
    private RelativeLayout ProgressView,r2,title;
    private LinearLayout l_register,l_forgetPwd;
    //private ImageView rememberPwd;
    private int rememberPwdCount=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //获取android设备的ID
        androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        appCard = (App) getApplication();
        appCard.setCard_spinner("");

        username = (EditText)findViewById(R.id.username);
        userpaw = (EditText)findViewById(R.id.password);
        ProgressView = (RelativeLayout)findViewById(R.id.video_loading);
        r2 = (RelativeLayout)findViewById(R.id.main);
        title = (RelativeLayout)findViewById(R.id.title);
        //rememberPwd = (ImageView)findViewById(R.id.rememberPwd);

        if(appCard.getLoginUsername()!=null&&appCard.getLoginUsername()!=""){
            username.setHint(appCard.getLoginUsername());
        }

        if(appCard.getLoginPwd()!=null&&appCard.getLoginPwd()!=""){
            userpaw.setHint(appCard.getLoginPwd());
        }

        /*rememberPwd.setOnClickListener(new View.OnClickListener() {
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
        });*/

        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,LoginRegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });

        TextView textView = (TextView)findViewById(R.id.pwd_forget);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,ForgetPwdActivity.class);
                startActivity(intent);
                finish();
            }
        });

        TextView button = (TextView)findViewById(R.id.login);
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                usernameStr = username.getText().toString().trim();
                userpawStr = userpaw.getText().toString().trim();
                App app = (App) getApplication();

                //setId(pg_themeStr);
                if (!usernameStr.equals("") && !userpawStr.equals("")){
                    ProgressView.setVisibility(View.VISIBLE);
                    r2.setVisibility(View.GONE);
                    hideInput(LoginActivity.this, username);
                    hideInput(LoginActivity.this,userpaw);
                    Pattern p = Pattern.compile("[0-9]*");
                    Matcher m = p.matcher(usernameStr);
                    if(m.matches() ){
                        isPhone=true;
                    }else{
                        isEmail=true;
                    }
                    app.getApiService().sign_in(usernameStr, userpawStr, isEmail, isPhone, androidId, new Callback<ResultMessage>() {

                        @Override
                        public void success(ResultMessage resultMessage, Response response) {
                            if (resultMessage.getCode() == 200) {
                                if(resultMessage.getToken()!=null){
                                    appCard.setToken(resultMessage.getToken());
                                    appCard.setLoginNetToken(resultMessage.getNetToken());
                                    appCard.setLoginNetId(resultMessage.getNetId());
                                }
                                appCard.setUsername(usernameStr);
                                if(rememberPwdCount==1){
                                    appCard.setLoginUsername(usernameStr);
                                    appCard.setLoginPwd(userpawStr);
                                }else if (rememberPwdCount==0){
                                    appCard.setLoginUsername("");
                                    appCard.setLoginPwd("");
                                }
                                WifiSql addressWifi = new WifiSql(LoginActivity.this);
                                addressWifi.createTable();
                                String sqlWifi = "insert into wifi_list values ('','')";
                                addressWifi.insert(sqlWifi);
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.putExtra("username", usernameStr);
                                intent.putExtra("userpaw", userpawStr);
                                //intent.putExtra("pg_theme",pg_themeStr);
                                startActivity(intent);
                                finish();
                                Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                            }else{
                                ProgressView.setVisibility(View.GONE);
                                r2.setVisibility(View.VISIBLE);
                                Toast.makeText(LoginActivity.this,resultMessage.getErrorMessage(),Toast.LENGTH_SHORT).show();
                            }

                        }

                        @Override
                        public void failure(RetrofitError error) {
                            ProgressView.setVisibility(View.GONE);
                            r2.setVisibility(View.VISIBLE);
                            Toast.makeText(LoginActivity.this,"服务器未响应",Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        });
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
            /*Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);*/
            finish();
            return false;
        }else {
            return super.onKeyDown(keyCode, event);
        }
    }
}