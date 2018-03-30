package com.wetoop.camera.tools;

/**
 * Created by User on 2018/1/26.
 */

public class SqlOperation {
    public static String CameraListSqlInit(String netId,String pwd){
        return "insert into camera_list_table values ('"+netId+"','"+pwd+"')";
    }
    public static String CameraListSqlUpDatePwd(String netId,String pwd){
        return "update camera_list_table set devicePwd = '"+pwd+"' where net_id = '"+netId+"'";
    }
    public static String CameraListSqlUpDateName(String sn,String name){
        return "update camera_list_table set deviceName = '"+name+"' where sn = '"+sn+"'";
    }
}
