package com.wetoop.camera.sql;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.wetoop.camera.bean.CameraPwdListBean;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/7/25.
 */
public class CameraListSql {

    private Context context;
    public static final String DATABASE_NAME = "camera_list.db";//数据库名称
    public static final int DATABASE_VERSION = 1;//数据库版本
    public static final String TABLENAME1 = "camera_list_table";//表名
    public CameraListSql(Context context){
        this.context = context;
    }


    /**
     * 重新建立数据表
     */
    public void createTable() {
        //打开数据库（以可写的方式打开）
        SQLiteDatabase db = CameraListDao.getInstance(context).getWriteDataBase();
        db.execSQL("drop table if exists " + TABLENAME1);
        String sql = "create table " + TABLENAME1 + "(net_id text,devicePwd text)";
        db.execSQL(sql);
        db.close();
    }

    /**
     * 插入数据
     * */
    public void insert(String sql){
        SQLiteDatabase db = CameraListDao.getInstance(context).getWriteDataBase();
        //执行SQL语句
        db.execSQL(sql);
        db.close();
    }

    /**
     * 查询数据
     * */
    public ArrayList<CameraPwdListBean> queryDataToSQLite(){
        ArrayList<CameraPwdListBean> cameraList=new ArrayList<CameraPwdListBean>();
        SQLiteDatabase db = CameraListDao.getInstance(context).getWriteDataBase();
        Cursor cursor = db.rawQuery("select * from " + TABLENAME1, new String[]{});
        while(cursor.moveToNext()){
            CameraPwdListBean bean = new CameraPwdListBean();
            bean.setNetID(cursor.getString(cursor.getColumnIndex("net_id")));//sn
            bean.setDevicePwd(cursor.getString(cursor.getColumnIndex("devicePwd")));//net_id
            cameraList.add(bean);
        }
        return cameraList;
    }

    /**
     * 修改数据
     * */
    public void updatetable(String sql){
        SQLiteDatabase db = CameraListDao.getInstance(context).getWriteDataBase();
        db.execSQL(sql);
        db.close();
    }//

    /**
     * 删除数据
     * */
    public void deletetable(String sql){
        SQLiteDatabase db = CameraListDao.getInstance(context).getWriteDataBase();
        db.execSQL(sql);
        db.close();
    }

}
