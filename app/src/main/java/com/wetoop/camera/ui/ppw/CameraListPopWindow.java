package com.wetoop.camera.ui.ppw;


import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.wetoop.camera.sql.CameraListSql;
import com.wetoop.camera.ui.dialog.EditDialog;
import com.wetoop.camera.ui.fragment.CameraListFragment;
import com.wetoop.cameras.R;

import java.util.List;

import static com.wetoop.camera.tools.SqlOperation.CameraListSqlUpDatePwd;

/**
 * Created by Administrator on 2016/2/16.
 */
public class CameraListPopWindow extends PopupWindow {

    private CameraListFragment mContext;
    public LinearLayout ll_popup;
    private EditDialog editDialog;
    private int type;
    private String netId;

    public CameraListPopWindow(CameraListFragment context,int type,String netId)
    {
        super(context.getContext());
        mContext = context;
        this.type = type;
        this.netId = netId;
        init();
    }

    private void init()
    {
        View view = LayoutInflater.from(mContext.getContext()).inflate(R.layout.item_camera_list_ppw, null);
        setContentView(view);
        setWidth(LayoutParams.MATCH_PARENT);
        setHeight(LayoutParams.WRAP_CONTENT);
        setFocusable(true);
        setBackgroundDrawable(new BitmapDrawable());

        ll_popup = (LinearLayout)view.findViewById(R.id.ll_popup);

        RelativeLayout parent = (RelativeLayout) view.findViewById(R.id.parent);
        Button resumePwd = (Button) view
                .findViewById(R.id.resume_pwd);
        Button bt1 = (Button) view
                .findViewById(R.id.play);
        Button bt2 = (Button) view
                .findViewById(R.id.info);
        Button bt3 = (Button) view
                .findViewById(R.id.shareInfo);
        Button bt4 = (Button) view
                .findViewById(R.id.delete);
        Button bt5 = (Button) view
                .findViewById(R.id.cancel);
        LinearLayout infoL = (LinearLayout) view.findViewById(R.id.infoL);
        LinearLayout shareInfoL = (LinearLayout) view.findViewById(R.id.shareInfoL);
        parent.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                dismiss();
            }
        });
        resumePwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editDialog = new EditDialog(mContext.getActivity(), "输入密码", "确定", 2, new EditDialog.OnCustomDialogListener() {
                    @Override
                    public void back(String resultStr) {
                        editDialog.progressBar.setVisibility(View.VISIBLE);
                        editDialog.comfirmBt.setVisibility(View.GONE);
                        CameraListSql address = new CameraListSql(mContext.getActivity());
                        address.insert(CameraListSqlUpDatePwd(netId,resultStr));
                        editDialog.dismiss();
                        Toast.makeText(mContext.getActivity(),"输入密码成功",Toast.LENGTH_SHORT).show();
                        mContext.checkPwd();
                    }

                    @Override
                    public void back(String userStr, String pwdStr) {

                    }
                });
                editDialog.show();
                dismiss();
            }
        });
        bt1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mContext.checkPwd();
                dismiss();
            }
        });
        bt2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mContext.cameraInfo();
                dismiss();
            }
        });
        bt3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mContext.cameraShare();
                dismiss();
            }
        });
        bt4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mContext.cameraDelete();
                dismiss();
            }
        });
        bt5.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        });
        if(type == 1){
            infoL.setVisibility(View.GONE);
            shareInfoL.setVisibility(View.GONE);
        }else if(type == 2){
            infoL.setVisibility(View.VISIBLE);
            shareInfoL.setVisibility(View.VISIBLE);
        }
    }

}