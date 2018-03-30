package com.wetoop.camera.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wetoop.camera.App;
import com.wetoop.camera.api.ResultMessage;
import com.wetoop.camera.ui.dialog.HintDialog;
import com.wetoop.cameras.R;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by User on 2017/7/10.
 */

public class UserInfoActivity extends Activity {
    private LinearLayout changePwd,phoneNum,emile;
    private RelativeLayout logout,back;
    private TextView phoneNumTextView,EmileTextView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        changePwd = (LinearLayout)findViewById(R.id.changePwd);
        phoneNum = (LinearLayout)findViewById(R.id.phoneNum);
        emile = (LinearLayout)findViewById(R.id.emile);
        logout = (RelativeLayout)findViewById(R.id.out_login);
        back = (RelativeLayout)findViewById(R.id.back);
        phoneNumTextView = (TextView)findViewById(R.id.phoneNumTextView);
        EmileTextView = (TextView)findViewById(R.id.EmileTextView);

        setViewText();

        clicked();
    }

    private void setViewText() {
        final App app = (App)getApplication();
        app.addActivity(this);
        app.getApiService().user_info(app.getToken(), new Callback<ResultMessage>() {
            @Override
            public void success(ResultMessage resultMessage, Response response) {
                if(resultMessage.getCode()==200){
                    if((resultMessage.getUser().getMail()==null)&&(resultMessage.getUser().getPhone()!=null)){
                        phoneNumTextView.setText(resultMessage.getUser().getPhone());
                        EmileTextView.setText("（未填写）");
                    }else if((resultMessage.getUser().getMail()!=null)&&(resultMessage.getUser().getPhone()==null)){
                        phoneNumTextView.setText("（未填写）");
                        EmileTextView.setText(resultMessage.getUser().getMail());
                    }else if((resultMessage.getUser().getMail()!=null)&&(resultMessage.getUser().getPhone()==null)){
                        phoneNumTextView.setText(resultMessage.getUser().getPhone());
                        EmileTextView.setText(resultMessage.getUser().getMail());
                    }
                }
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });

    }

    private void clicked() {
        changePwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserInfoActivity.this,ChangePwdActivity.class);
                startActivity(intent);
            }
        });
        phoneNum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        emile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HintDialog checkedDialog = new HintDialog(UserInfoActivity.this, "提示","是否要退出登录","退出", new HintDialog.OnCustomDialogListener() {

                    @Override
                    public void back(String query) {
                        if (query.equals("logout")) {
                            App application = (App) getApplication();
                            application.getApiService().sign_out(application.getToken(), new Callback<ResultMessage>() {
                                @Override
                                public void success(ResultMessage resultMessage, Response response) {
                                    if(resultMessage.getCode()==200){
                                        Toast.makeText(UserInfoActivity.this, "退出登录成功", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(UserInfoActivity.this,LoginActivity.class);
                                        startActivity(intent);
                                        App app = (App) getApplication();
                                        app.setToken("");
                                        app.exit();
                                    }
                                }

                                @Override
                                public void failure(RetrofitError error){
                                    Toast.makeText(UserInfoActivity.this, "连接服务器失败", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }

                });
                checkedDialog.show();
            }
        });

        /*phoneNumTextView = (TextView)findViewById(R.id.phoneNumTextView);
        EmileTextView = (TextView)findViewById(R.id.EmileTextView);*/
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (keyCode == KeyEvent.KEYCODE_BACK){
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}