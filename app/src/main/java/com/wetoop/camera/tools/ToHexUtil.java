package com.wetoop.camera.tools;

/**
 * @Author WETOOP
 * @Date 2018/3/20.
 * @Description
 */

public class ToHexUtil {
    public static String byte2hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(Integer.toHexString(b));
        }
        return sb.toString();
    }

    public static byte[] hex2byte(String content) {
        return content.getBytes();
    }
}
