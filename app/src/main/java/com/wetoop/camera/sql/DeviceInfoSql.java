package com.wetoop.camera.sql;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.wetoop.camera.bean.DeviceInfoBean;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/5/6.
 */
public class DeviceInfoSql {
    private Context context;
    public static final String DATABASE_NAME = "Camera_sn1.db";//数据库名称
    public static final int DATABASE_VERSION = 1;//数据库版本
    public static final String TABLENAME = "camera_info1";//摄像头详细信息
    public DeviceInfoSql(Context context){
        this.context = context;
    }


    /**
     * 重新建立数据表
     */
    public void createTable() {
        //打开数据库（以可写的方式打开）
        SQLiteDatabase db = DaoSn.getInstance(context).getWriteDataBase();

        db.execSQL("drop table if exists " + TABLENAME);
        String sql = "create table " + TABLENAME + "(devname text,model text,seriano text,softwareversion text,hardwareversion text)";
        db.execSQL(sql);
        db.close();
        return;

    }

    /**
     * 初始化时，将所有地址的数据填入数据表中
     */
    public void setDataToSQLite(ArrayList<DeviceInfoBean> list) {
        //createTable();
        SQLiteDatabase db = DaoSn.getInstance(context).getWriteDataBase();
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                db.execSQL("insert into " + TABLENAME + " values(?,?,?,?,?)",
                        new Object[]{list.get(i).getDevname(), list.get(i).getModel(),list.get(i).getSeriano(),list.get(i).getSoftwareversion(),list.get(i).getHardwareversion()});
            }
        }
        db.close();
    }
    /**
     * 查询数据
     * */
    public ArrayList<DeviceInfoBean> queryDataToSQLite(){
        ArrayList<DeviceInfoBean> cameraList=new ArrayList<DeviceInfoBean>();
        SQLiteDatabase db = DaoSn.getInstance(context).getWriteDataBase();
        Cursor cursor = db.rawQuery("select * from " + TABLENAME, new String[]{});
        while(cursor.moveToNext()){
            DeviceInfoBean bean = new DeviceInfoBean();
            bean.setDevname(cursor.getString(cursor.getColumnIndex("devname")));//sn
            bean.setModel(cursor.getString(cursor.getColumnIndex("model")));//ip
            bean.setSeriano(cursor.getString(cursor.getColumnIndex("seriano")));
            bean.setSoftwareversion(cursor.getString(cursor.getColumnIndex("softwareversion")));
            bean.setHardwareversion(cursor.getString(cursor.getColumnIndex("hardwareversion")));
            cameraList.add(bean);
        }
        return cameraList;
    }

    /**
     *增加数据
     */
    public void addDataToSQLite(ArrayList<DeviceInfoBean> list){
        SQLiteDatabase db = DaoSn.getInstance(context).getWriteDataBase();
        if (list != null) {
            db.execSQL("insert into " + TABLENAME + " values(?,?,?,?)",
                    new Object[]{list.get(0).getDevname(), list.get(0).getModel(),list.get(0).getSeriano(),list.get(0).getSoftwareversion(),list.get(0).getHardwareversion()});
        }
        db.close();
    }
    /**
     * 删除数据
     * */
    public void deleteDataToSQLite(String devname){
        SQLiteDatabase db = DaoSn.getInstance(context).getWriteDataBase();
        db.delete(TABLENAME, "devname=?", new String[]{devname});
        db.close();
    }
}
