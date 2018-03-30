package com.wetoop.camera.ui.vlc;

/**
 * Created by User on 2018/3/20.
 */

public class ResultConstant {
    public static final int FORWARD_RESULT_INITIAL = 0;
    public static final int FORWARD_RESULT_OK = 1;
    public static final int FORWARD_RESULT_CONN_LOST = -2;
    public static final int FORWARD_RESULT_PEER_OFFLINE = -3;
    public static final int FORWARD_RESULT_CERT_ERROR = -4;
    public static final int FORWARD_RESULT_KICKED_OUT = -5;
    public static final int FORWARD_RESULT_CONN_CLOSE = -6;
    public static final int FORWARD_RESULT_SYS_ERR = -9;
    public static final int FORWARD_RESULT_NOT_EXISTS = -10;

    public static final int FORWARD_MODE_UNKNOWN = 0;
    public static final int FORWARD_MODE_LAN = 1;
    public static final int FORWARD_MODE_P2P = 2;
    public static final int FORWARD_MODE_RELAY = 3;
}
