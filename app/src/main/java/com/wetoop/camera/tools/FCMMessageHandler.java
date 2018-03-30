package com.wetoop.camera.tools;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Base64;

import com.wetoop.camera.App;
import com.wetoop.camera.api.CameraInfo;
import com.wetoop.camera.api.ResultMessage;
import com.wetoop.camera.bean.CameraPwdListBean;
import com.wetoop.camera.service.VideoService;
import com.wetoop.camera.sql.CameraListSql;
import com.wetoop.camera.ui.vlc.VideoActivity;

import java.util.ArrayList;

/**
 * @Author WETOOP
 * @Date 2018/3/20.
 * @Description
 */

public class FCMMessageHandler {

    public static String decodeDeviceId(Context context, String crypt) {
        if (TextUtils.isEmpty(crypt)) return null;
        byte[] bytes = Base64.decode(crypt, Base64.DEFAULT);
        byte[] token = new byte[16];
        byte[] tem = ((App) context.getApplicationContext()).getToken().getBytes();
        for (int i = 0; i < 16; i++) {
            token[i] = tem[i];
        }
        tem = ((App) context.getApplicationContext()).getLoginNetId().getBytes();
        byte[] netId = new byte[16];
        for (int i = 0; i < 16; i++) {
            netId[i] = tem[i];
        }
        byte[] decrypt = AesEncryptionUtil.decrypt(bytes, new String(token), new String(netId));
        if (decrypt != null) return new String(decrypt);
        else return null;
    }

    public static void showMessageDialog(final Context context, String crypt) {
        if (context == null || crypt == null) return;
        String id = FCMMessageHandler.decodeDeviceId(context, crypt);
        if (id == null) return;
        final ResultMessage result = (ResultMessage) App.session.get(App.CAMERA_INFO);
        CameraInfo cameraInfo = null;
        int position = 0;
        ArrayList<CameraInfo> cameras = result.getCameraInfos();
        for (int i = 0; i < cameras.size(); i++) {
            if (cameras.get(i).getSn().equals(id)) {
                cameraInfo = cameras.get(i);
                position = i;
                break;
            }
        }
        if (cameraInfo == null) return;
        final int finalPosition = position;
        final CameraInfo finalCameraInfo = cameraInfo;
        new AlertDialog.Builder(context)
                .setTitle("变动侦测通知")
                .setMessage(cameraInfo.getCamera_name() + "（" + cameraInfo.getSn() + "）出现了变动！")
                .setPositiveButton("立即查看", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        App app = (App) context.getApplicationContext();
                        app.setVideoPos(finalPosition);
                        Intent intent2 = new Intent(context, VideoService.class);
                        intent2.setPackage(context.getPackageName());
                        intent2.putExtra("netId", finalCameraInfo.getNet_id());
                        intent2.putExtra("usernameStr", app.getLoginNetId());
                        intent2.putExtra("userpawStr", app.getLoginNetToken());
                        intent2.putExtra("port_conn", app.getVideoPortConn());
                        intent2.putExtra("port", app.getVideoPort());
                        intent2.putExtra("node", app.getNode());
                        context.startService(intent2);

                        ProgressDialog catchTheOrderDialog = new ProgressDialog(App.getCurrentActivity());
                        catchTheOrderDialog.setTitle("连接摄像头");
                        catchTheOrderDialog.setMessage("正在连接摄像头···");
                        catchTheOrderDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消连接", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        catchTheOrderDialog.setCancelable(false);
                        catchTheOrderDialog.setCanceledOnTouchOutside(false);
                        catchTheOrderDialog.show();
                        startPlayThread(context, finalCameraInfo, catchTheOrderDialog);
                    }
                }).show();
    }

    public static void showMessageDialog(Context context, String crypt, final OnSureClickListener listener) {
        if (context == null || crypt == null) return;
        String id = FCMMessageHandler.decodeDeviceId(context, crypt);
        final ResultMessage result = (ResultMessage) App.session.get(App.CAMERA_INFO);
        CameraInfo cameraInfo = null;
        int position = 0;
        ArrayList<CameraInfo> cameras = result.getCameraInfos();
        for (int i = 0; i < cameras.size(); i++) {
            if (cameras.get(i).getSn().equals(id)) {
                cameraInfo = cameras.get(i);
                position = i;
                break;
            }
        }
        if (cameraInfo == null) return;
        new AlertDialog.Builder(context)
                .setTitle("变动侦测通知")
                .setMessage(cameraInfo.getCamera_name() + "（" + cameraInfo.getSn() + "）出现了变动！")
                .setPositiveButton("立即查看", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (listener != null) listener.onClick(result);
                    }
                }).show();
    }

    private static void startPlayThread(final Context context, final CameraInfo cameraInfo, final ProgressDialog catchTheOrderDialog) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Thread.sleep(5 * 1000);//进入线程时先睡10s
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                catchTheOrderDialog.dismiss();
                final App app = (App) context.getApplicationContext();
                int pos = app.getVideoPos();
                String devicePwd = sqlList(context).get(pos).getDevicePwd();
                Intent intent = new Intent();
                intent.setClass(context, VideoActivity.class);
                intent.putExtra("sn", cameraInfo.getSn());
                intent.putExtra("netId", cameraInfo.getNet_id());
                intent.putExtra("deviceName", "Mini");
                intent.putExtra("devicePwd", devicePwd);
                intent.putExtra("admin_pwd", devicePwd);
                context.startActivity(intent);

            }
        }).start();
    }

    private static ArrayList<CameraPwdListBean> sqlList(Context context) {
        CameraListSql cameraSql = new CameraListSql(context);
        return cameraSql.queryDataToSQLite();
    }

    public interface OnSureClickListener {
        void onClick(ResultMessage result);
    }
}
