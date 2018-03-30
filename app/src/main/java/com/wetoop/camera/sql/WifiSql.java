package com.wetoop.camera.sql;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/1/13.
 */
public class WifiSql {
    private Context context;
    public static final String DATABASE_NAME = "WifiSql.db";//数据库名称
    public static final int DATABASE_VERSION = 1;//数据库版本
    public static final String TABLENAME1 = "wifi_list";//表名

    public WifiSql(Context context) {
        this.context = context;
    }


    /**
     * 重新建立数据表
     */
    public void createTable() {
        //打开数据库（以可写的方式打开）
        SQLiteDatabase db = WifiDao.getInstance(context).getWriteDataBase();
        //String sql_findtable = "SELECT count(*) FROM sqlite_master WHERE type='table' AND name='add_sn_table';";
        //Cursor cursor = db.rawQuery(sql_findtable,new String[]{});
        //while (cursor.moveToNext()) {
        //if (cursor.getInt(cursor.getColumnIndex("count(*)")) == 0){
        db.execSQL("drop table if exists " + TABLENAME1);
        String sql = "create table " + TABLENAME1 + "(wifiName text,wifiPwd text)";
        db.execSQL(sql);
        db.close();
        // } else {
        return;
        //}
        //}
    }

    /**
     * 插入数据
     */
    public void insert(String sql) {
        SQLiteDatabase db = WifiDao.getInstance(context).getWriteDataBase();
        //执行SQL语句
        db.execSQL(sql);
        db.close();
    }

    /**
     * 查询数据
     */
    public ArrayList<WifiBean> queryDataToSQLite() {
        ArrayList<WifiBean> wifiBean = new ArrayList<WifiBean>();
        SQLiteDatabase db = WifiDao.getInstance(context).getWriteDataBase();
        Cursor cursor = db.rawQuery("select * from " + TABLENAME1, new String[]{});
        while (cursor.moveToNext()) {
            WifiBean bean = new WifiBean();
            bean.setWifiName(cursor.getString(cursor.getColumnIndex("wifiName")));
            bean.setWifiPwd(cursor.getString(cursor.getColumnIndex("wifiPwd")));
            wifiBean.add(bean);
        }
        return wifiBean;
    }
}
