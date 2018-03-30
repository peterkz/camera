package com.wetoop.camera.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.wetoop.camera.bean.SqlSnBean;
import com.wetoop.camera.sql.CameraSqlSn;
import com.wetoop.cameras.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by User on 2018/3/19.
 */

public class AddDeviceAdapter extends BaseAdapter {

    private Context context;
    private List list;

    public AddDeviceAdapter(Context context, List list){
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount(){
        // TODO Auto-generated method stub
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        if(convertView==null){
            convertView= LayoutInflater.from(context).inflate(R.layout.adapter_add_device, null);
        }
        TextView sn = (TextView)convertView.findViewById(R.id.user_title);
        TextView ip = (TextView)convertView.findViewById(R.id.serial_number);
        ArrayList<SqlSnBean> cameraList= sql();
        sn.setText("序列号："+cameraList.get(position).getSn());
        ip.setText("ip：" + cameraList.get(position).getIp());

        return convertView;
    }

    private ArrayList<SqlSnBean> sql(){
        CameraSqlSn cameraSql = new CameraSqlSn(context.getApplicationContext());
        return cameraSql.queryDataToSQLite();
    }

}
