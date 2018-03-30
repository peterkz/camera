package com.wetoop.camera.sql;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.wetoop.camera.bean.JoinBean;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/5/22.
 */

public class JoinList {


    private Context context;
    public static final String DATABASE_NAME = "join_list.db";//数据库名称
    public static final int DATABASE_VERSION = 1;//数据库版本
    public static final String TABLENAME1 = "join_list_table";//表名
    public JoinList(Context context){
        this.context = context;
    }


    /**
     * 重新建立数据表
     */
    public void createTable() {
        //打开数据库（以可写的方式打开）
        SQLiteDatabase db = JoinListDao.getInstance(context).getWriteDataBase();
        //String sql_findtable = "SELECT count(*) FROM sqlite_master WHERE type='table' AND name='add_sn_table';";
        //Cursor cursor = db.rawQuery(sql_findtable,new String[]{});
        //while (cursor.moveToNext()) {
        //if (cursor.getInt(cursor.getColumnIndex("count(*)")) == 0){
        db.execSQL("drop table if exists " + TABLENAME1);
        String sql = "create table " + TABLENAME1 + "(share_id text,online text,from text,peer text)";
        db.execSQL(sql);
        db.close();
        // } else {
        return;
        //}
        //}
    }

    /**
     * 初始化时，将所有数据填入数据表中
     */
    public void setDataToSQLite(ArrayList<JoinBean> list) {
        //createTable();
        SQLiteDatabase db = JoinListDao.getInstance(context).getWriteDataBase();
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                db.execSQL("insert into " + TABLENAME1 + " values(?,?,?,?)",
                        new Object[]{list.get(i).getShareId(), list.get(i).getOnline(),list.get(i).getFrom(),list.get(i).getPeer()});
            }
        }
        db.close();
    }

    /**
     * 插入数据
     * */
    public void insert(String sql){
        SQLiteDatabase db = JoinListDao.getInstance(context).getWriteDataBase();
        //执行SQL语句
        db.execSQL(sql);
        db.close();
    }

    /**
     * 查询数据
     * */
    public ArrayList<JoinBean> queryDataToSQLite(){
        ArrayList<JoinBean> cameraList=new ArrayList<JoinBean>();
        SQLiteDatabase db = JoinListDao.getInstance(context).getWriteDataBase();
        Cursor cursor = db.rawQuery("select * from " + TABLENAME1, new String[]{});
        while(cursor.moveToNext()){
            JoinBean bean = new JoinBean();
            bean.setShareId(cursor.getString(cursor.getColumnIndex("share_id")));
            bean.setOnline(cursor.getString(cursor.getColumnIndex("online")));
            bean.setFrom(cursor.getString(cursor.getColumnIndex("from")));
            bean.setPeer(cursor.getString(cursor.getColumnIndex("peer")));
            cameraList.add(bean);
        }
        return cameraList;
    }

    /**
     * 修改数据
     * */
    public void updatetable(String sql){
        SQLiteDatabase db = JoinListDao.getInstance(context).getWriteDataBase();
        db.execSQL(sql);
        db.close();
    }//

    /**
     * 删除数据
     * */
    public void deletetable(String sql){
        SQLiteDatabase db = JoinListDao.getInstance(context).getWriteDataBase();
        db.execSQL(sql);
        db.close();
    }

}
