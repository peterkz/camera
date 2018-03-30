package com.wetoop.camera.sql;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.wetoop.camera.bean.SqlSnBean;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/3/17.
 */
public class CameraSqlSn {
    private Context context;
    public static final String DATABASE_NAME = "Camera_sn1.db";//数据库名称
    public static final int DATABASE_VERSION = 1;//数据库版本
    public static final String TABLENAME1 = "add_sn_table1";//表名
    public static final String TABLENAME2 = "camera_info1";//摄像头详细信息
    public CameraSqlSn(Context context){
        this.context = context;
    }


    /**
     * 重新建立数据表
     */
    public void createTable() {
        //打开数据库（以可写的方式打开）
        SQLiteDatabase db = DaoSn.getInstance(context).getWriteDataBase();
        //String sql_findtable = "SELECT count(*) FROM sqlite_master WHERE type='table' AND name='add_sn_table';";
        //Cursor cursor = db.rawQuery(sql_findtable,new String[]{});
        //while (cursor.moveToNext()) {
            //if (cursor.getInt(cursor.getColumnIndex("count(*)")) == 0){
                db.execSQL("drop table if exists " + TABLENAME1);
                String sql = "create table " + TABLENAME1 + "(sn text,ip text,uuid text,username text)";
                db.execSQL(sql);
                db.close();
           // } else {
                return;
            //}
        //}
    }

    /**
     * 初始化时，将所有地址的数据填入数据表中
     */
    public void setDataToSQLite(ArrayList<SqlSnBean> list) {
        //createTable();
        SQLiteDatabase db = DaoSn.getInstance(context).getWriteDataBase();
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                db.execSQL("insert into " + TABLENAME1 + " values(?,?,?,?)",
                        new Object[]{list.get(i).getSn(), list.get(i).getIp(),list.get(i).getUuid(),list.get(i).getUsername()});
            }
        }
        db.close();
    }
    /**
     * 查询数据
     * */
    public ArrayList<SqlSnBean> queryDataToSQLite(){
        ArrayList<SqlSnBean> cameraList=new ArrayList<SqlSnBean>();
        SQLiteDatabase db = DaoSn.getInstance(context).getWriteDataBase();
        Cursor cursor = db.rawQuery("select * from " + TABLENAME1, new String[]{});
        while(cursor.moveToNext()){
            SqlSnBean bean = new SqlSnBean();
            bean.setSn(cursor.getString(cursor.getColumnIndex("sn")));//sn
            bean.setIp(cursor.getString(cursor.getColumnIndex("ip")));//ip
            bean.setUuid(cursor.getString(cursor.getColumnIndex("uuid")));
            bean.setUsername(cursor.getString(cursor.getColumnIndex("username")));
            cameraList.add(bean);
        }
        return cameraList;
    }

    /**
     *增加数据
     */
    public void addDataToSQLite(ArrayList<SqlSnBean> list){
        SQLiteDatabase db = DaoSn.getInstance(context).getWriteDataBase();
        if (list != null) {
            db.execSQL("insert into " + TABLENAME1 + " values(?,?,?,?)",
                    new Object[]{list.get(0).getSn(), list.get(0).getIp(),list.get(0).getUuid(),list.get(0).getUsername()});
        }
        db.close();
    }
    /**
     * 删除数据
     * */
    public void deleteDataToSQLite(String username){
        SQLiteDatabase db = DaoSn.getInstance(context).getWriteDataBase();
        db.delete(TABLENAME1, "username=?", new String[]{username});
        db.close();
    }
}