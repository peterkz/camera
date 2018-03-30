package com.wetoop.camera.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.wetoop.camera.App;
import com.wetoop.camera.MyCrashHandler;
import com.wetoop.camera.api.ResultMessage;
import com.wetoop.camera.sql.CameraListSql;
import com.wetoop.cameras.R;

import java.util.Timer;
import java.util.TimerTask;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Administrator on 2016/4/13.
 */
public class LogoActivity extends Activity {
    private Intent it;
    TimerTask task = new TimerTask() {

        @Override
        public void run() {
            startActivity(it); //执行
            finish();
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        /**/
        setContentView(R.layout.activity_logo);
        final Timer timer = new Timer();
        App app = (App) getApplication();
        app.setTcpStart("false");
        if (app.getFirstUser() < 2) {
            it = new Intent(this, LoginActivity.class);
            CameraListSql address = new CameraListSql(this);
            address.createTable();
            app.setFirstUser(2);
            timer.schedule(task, 1000 * 1); //3秒后
        } else {
            if (app.getToken().length() > 0) {
                it = new Intent(this, MainActivity.class);
                final Bundle extras = getIntent().getExtras();
                if (extras == null) timer.schedule(task, 1000 * 1); //3秒后
                else {
                    app.getApiService().cameraList(app.getToken(), new Callback<ResultMessage>() {
                        @Override
                        public void success(ResultMessage resultMessage, Response response) {
                            App.session.put(App.CAMERA_INFO, resultMessage);
                            it.putExtra(App.FCM_MESSAGE_EXTRAS, extras);
                            startActivity(it);
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            startActivity(it);
                        }
                    });
                }

            } else {
                it = new Intent(this, LoginActivity.class);
                timer.schedule(task, 1000 * 1); //3秒后
            }
        }
    }

}