package com.wetoop.camera.service;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.wetoop.camera.App;
import com.wetoop.camera.api.ResultMessage;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by User on 2017/11/9.
 */

public class FCMInstanceService extends FirebaseInstanceIdService {
    public static final String FCM_TOKEN_VALUE = "FCM_TOKEN_VALUE";
    private static String TAG = "FCMInstanceService";
    private String token;
    private Handler handler = new Handler();

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        final String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String token) {
        App app = (App) this.getApplication();
        App.session.put(FCM_TOKEN_VALUE, token);
        if (!TextUtils.isEmpty(app.getToken()))
            app.getApiService().postToken(app.getToken(), token, "android", new Callback<ResultMessage>() {
                @Override
                public void success(ResultMessage resultMessage, Response response) {
                    Log.d(TAG, "postToken success");
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.d(TAG, "postToken failure");
                }
            });
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
