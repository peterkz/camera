package com.wetoop.camera;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.wetoop.camera.api.ApiService;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

/**
 * Created by Administrator on 2015/12/16.
 */
public class App extends Application {

    private static App sInstance;
    private ApiService apiService;
    public final static String SLEEP_INTENT = "org.videolan.vlc.SleepIntent";
    //运用list来保存们每一个activity是关键
    private List<Activity> mList = new LinkedList<Activity>();
    private Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
    public static Map<String, Object> session = new Hashtable<>();
    public static final String CAMERA_INFO = "CAMERA_INFO";
    public static final String FCM_MESSAGE_EXTRAS = "FCM_MESSAGE_EXTRAS";

    @Override
    public void onCreate() {
        super.onCreate();

        sInstance = this;
        MyCrashHandler crashHandler = MyCrashHandler.getInstance();
        crashHandler.init(getApplicationContext());
        /*Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                File logPath = getExternalFilesDir("crash");
                String timestamp = String.valueOf(System.currentTimeMillis());
                final StringWriter result = new StringWriter();
                final PrintWriter printWriter = new PrintWriter(result);
                ex.printStackTrace(printWriter);
                String stacktrace = result.toString();
                printWriter.close();
                String filename = timestamp + ".log";

                if (logPath != null) {
                    try {
                        BufferedWriter bos = new BufferedWriter(new FileWriter(new File(logPath, filename)));
                        bos.write(stacktrace);
                        bos.flush();
                        bos.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                Toast.makeText(getApplicationContext(), "toast", Toast.LENGTH_SHORT).show();
                defaultUncaughtExceptionHandler.uncaughtException(thread, ex);
            }
        });*/

        BooleanSerializer booleanSerializer = new BooleanSerializer();
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .registerTypeAdapter(Boolean.class, booleanSerializer)
                .registerTypeAdapter(boolean.class, booleanSerializer)
                .create();

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(" https://galaeye.wetoop.com/api")
                .setConverter(new GsonConverter(gson))
                .setLogLevel(RestAdapter.LogLevel.NONE)//调试开关
                .build();
        apiService = restAdapter.create(ApiService.class);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String p = pref.getString("set_locale", "");
        if (p != null && !p.equals("")) {
            Locale locale;
            // workaround due to region code
            if (p.equals("zh-TW")) {
                locale = Locale.TRADITIONAL_CHINESE;
            } else if (p.startsWith("zh")) {
                locale = Locale.CHINA;
            } else if (p.equals("pt-BR")) {
                locale = new Locale("pt", "BR");
            } else if (p.equals("bn-IN") || p.startsWith("bn")) {
                locale = new Locale("bn", "IN");
            } else {
                /**
                 * Avoid a crash of
                 * java.lang.AssertionError: couldn't initialize LocaleData for locale
                 * if the user enters nonsensical region codes.
                 */
                if (p.contains("-"))
                    p = p.substring(0, p.indexOf('-'));
                locale = new Locale(p);
            }
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config,
                    getBaseContext().getResources().getDisplayMetrics());
        }
    }

    //在这个函数里面，把获取的gson里面的true或者1转换成程序能识别的布尔值
    public static class BooleanSerializer implements JsonSerializer<Boolean>, JsonDeserializer<Boolean> {

        @Override
        public JsonElement serialize(Boolean arg0, Type arg1, JsonSerializationContext arg2) {
            return new JsonPrimitive(arg0 ? 1 : 0);
        }

        @Override
        public Boolean deserialize(JsonElement arg0, Type arg1, JsonDeserializationContext arg2) throws JsonParseException {
            return arg0.getAsInt() > 0;
        }
    }

    public void setAdminName(String adminName) {
        SharedPreferences settings = getSharedPreferences("VLCApplication", 0);
        settings.edit().putString("adminName", adminName).commit();
    }

    public String getAdminName() {
        SharedPreferences settings = getSharedPreferences("VLCApplication", 0);
        return settings.getString("adminName", "admin");
    }

    public void setAdminPwd(String adminPwd) {
        SharedPreferences settings = getSharedPreferences("VLCApplication", 0);
        settings.edit().putString("adminPwd", adminPwd).commit();
    }

    public String getAdminPwd() {
        SharedPreferences settings = getSharedPreferences("VLCApplication", 0);
        return settings.getString("adminPwd", "");
    }

    public void setWifiName(String wifiName) {
        SharedPreferences settings = getSharedPreferences("VLCApplication", 0);
        settings.edit().putString("wifiName", wifiName).commit();
    }

    public String getWifiName() {
        SharedPreferences settings = getSharedPreferences("VLCApplication", 0);
        return settings.getString("wifiName", "");
    }

    public void setWifiPwd(String wifiPwd) {
        SharedPreferences settings = getSharedPreferences("VLCApplication", 0);
        settings.edit().putString("wifiPwd", wifiPwd).commit();
    }

    public String getWifiPwd() {
        SharedPreferences settings = getSharedPreferences("VLCApplication", 0);
        return settings.getString("wifiPwd", "");
    }

    public void setWifiType(int wifiType) {
        SharedPreferences settings = getSharedPreferences("VLCApplication", 0);
        settings.edit().putInt("wifiType", wifiType).commit();
    }

    public int getWifiType() {
        SharedPreferences settings = getSharedPreferences("VLCApplication", 0);
        return settings.getInt("wifiType", 0);
    }

    public void setCard_spinner(String card_spinner) {
        SharedPreferences settings = getSharedPreferences("VLCApplication", 0);
        settings.edit().putString("card_spinner", card_spinner).commit();
    }

    public String getCard_spinner() {
        SharedPreferences settings = getSharedPreferences("VLCApplication", 0);
        return settings.getString("card_spinner", "");
    }

    public void setLoginNetId(String loginNetId) {
        SharedPreferences settings = getSharedPreferences("VLCApplication", 0);
        settings.edit().putString("loginNetId", loginNetId).commit();
    }

    public void setToken(String token) {
        SharedPreferences settings = getSharedPreferences("VLCApplication", 0);
        settings.edit().putString("token", token).commit();
    }

    public String getToken() {
        SharedPreferences settings = getSharedPreferences("VLCApplication", 0);
        return settings.getString("token", "");
    }

    public void setFCMToken(String token) {
        SharedPreferences settings = getSharedPreferences("VLCApplication", 0);
        settings.edit().putString("fcm_token", token).commit();
    }

    public String getFCMToken() {
        SharedPreferences settings = getSharedPreferences("VLCApplication", 0);
        return settings.getString("fcm_token", "");
    }

    public String getLoginNetId() {
        SharedPreferences settings = getSharedPreferences("VLCApplication", 0);
        return settings.getString("loginNetId", "");
    }

    public void setLoginNetToken(String loginNetToken) {
        SharedPreferences settings = getSharedPreferences("VLCApplication", 0);
        settings.edit().putString("loginNetToken", loginNetToken).commit();
    }

    public String getLoginNetToken() {
        SharedPreferences settings = getSharedPreferences("VLCApplication", 0);
        return settings.getString("loginNetToken", "");
    }

    public void setVideoPos(int videoPos) {
        SharedPreferences settings = getSharedPreferences("VLCApplication", 0);
        settings.edit().putInt("videoPos", videoPos).commit();
    }

    public int getVideoPos() {
        SharedPreferences settings = getSharedPreferences("VLCApplication", 0);
        return settings.getInt("videoPos", 0);
    }

    public void setVersion(String version) {
        SharedPreferences settings = getSharedPreferences("VLCApplication", 0);
        settings.edit().putString("version", version).commit();
    }

    public String getVersion() {
        SharedPreferences settings = getSharedPreferences("VLCApplication", 0);
        return settings.getString("version", "");
    }

    public void setAlarm_getcfg(String alarm_getcfg) {
        SharedPreferences settings = getSharedPreferences("VLCApplication", 0);
        settings.edit().putString("alarm_getcfg", alarm_getcfg).commit();
    }

    public String getAlarm_getcfg() {
        SharedPreferences settings = getSharedPreferences("VLCApplication", 0);
        return settings.getString("alarm_getcfg", "");
    }

    public void setVideoPort(int videoPort) {
        SharedPreferences settings = getSharedPreferences("VLCApplication", 0);
        settings.edit().putInt("videoPort", videoPort).commit();
    }

    public int getVideoPort() {
        SharedPreferences settings = getSharedPreferences("VLCApplication", 0);
        return settings.getInt("videoPort", 2505);
    }

    public void setVideoPortConn(int videoPortConn) {//视频加密之后端口
        SharedPreferences settings = getSharedPreferences("VLCApplication", 0);
        settings.edit().putInt("videoPortConn", videoPortConn).commit();
    }

    public int getVideoPortConn() {
        SharedPreferences settings = getSharedPreferences("VLCApplication", 0);
        return settings.getInt("videoPortConn", 3005);
    }

    public void setAudioPort(int audioPort) {
        SharedPreferences settings = getSharedPreferences("VLCApplication", 0);
        settings.edit().putInt("audioPort", audioPort).commit();
    }

    public int getAudioPort() {
        SharedPreferences settings = getSharedPreferences("VLCApplication", 0);
        return settings.getInt("audioPort", 3500);
    }

    public void setAudioPortConn(int audioPortConn) {//音频加密端口
        SharedPreferences settings = getSharedPreferences("VLCApplication", 0);
        settings.edit().putInt("audioConnPort", audioPortConn).commit();
    }

    public int getAudioPortConn() {
        SharedPreferences settings = getSharedPreferences("VLCApplication", 0);
        return settings.getInt("audioConnPort", 4000);
    }

    public void setUsername(String username) {
        SharedPreferences settings = getSharedPreferences("VLCApplication", 0);
        settings.edit().putString("loginName", username).commit();
    }

    public String getUsername() {
        SharedPreferences settings = getSharedPreferences("VLCApplication", 0);
        return settings.getString("loginName", "");
    }

    public void setLoginUsername(String loginUsername) {
        SharedPreferences settings = getSharedPreferences("VLCApplication", 0);
        settings.edit().putString("loginUsername", loginUsername).commit();
    }

    public String getLoginUsername() {
        SharedPreferences settings = getSharedPreferences("VLCApplication", 0);
        return settings.getString("loginUsername", "");
    }

    public void setLoginPwd(String loginPwd) {
        SharedPreferences settings = getSharedPreferences("VLCApplication", 0);
        settings.edit().putString("loginPwd", loginPwd).commit();
    }

    public String getLoginPwd() {
        SharedPreferences settings = getSharedPreferences("VLCApplication", 0);
        return settings.getString("loginPwd", "");
    }

    public void setAudioCome(int audioCome) {
        SharedPreferences settings = getSharedPreferences("VLCApplication", 0);
        settings.edit().putInt("audioCome", audioCome).commit();
    }

    public int getAudioCome() {
        SharedPreferences settings = getSharedPreferences("VLCApplication", 0);
        return settings.getInt("audioCome", 0);
    }

    public void setFirstUser(int firstUser) {
        SharedPreferences settings = getSharedPreferences("VLCApplication", 0);
        settings.edit().putInt("firstUser", firstUser).commit();
    }

    public int getFirstUser() {
        SharedPreferences settings = getSharedPreferences("VLCApplication", 0);
        return settings.getInt("firstUser", 1);
    }

    public void setNode(String node) {
        SharedPreferences settings = getSharedPreferences("VLCApplication", 0);
        settings.edit().putString("node", node).commit();
    }

    public String getNode() {
        SharedPreferences settings = getSharedPreferences("VLCApplication", 0);
        return settings.getString("node", "");
    }

    public void setTcpStart(String tcpStart) {
        SharedPreferences settings = getSharedPreferences("VLCApplication", 0);
        settings.edit().putString("tcpStart", tcpStart).commit();
    }

    public String getTcpStart() {
        SharedPreferences settings = getSharedPreferences("VLCApplication", 0);
        return settings.getString("tcpStart", "false");
    }
    public void setBroadcast(int broadcast) {//
        SharedPreferences settings = getSharedPreferences("VLCApplication", 0);
        settings.edit().putInt("broadcast", broadcast).commit();
    }
    public int getBroadcast() {
        SharedPreferences settings = getSharedPreferences("VLCApplication", 0);
        return settings.getInt("broadcast", 0);
    }
    public void setTcpStop(int tcpStop) {//tcpStop调用标记
        SharedPreferences settings = getSharedPreferences("VLCApplication", 0);
        settings.edit().putInt("tcpStop", tcpStop).commit();
    }
    public int getTcpStop() {
        SharedPreferences settings = getSharedPreferences("VLCApplication", 0);
        return settings.getInt("tcpStop", 0);
    }
    public void setNetIDFd(String netIDFd) {//tcpStop调用标记
        SharedPreferences settings = getSharedPreferences("VLCApplication", 0);
        settings.edit().putString("netIDFd", netIDFd).commit();
    }
    public String getNetIDFd() {
        SharedPreferences settings = getSharedPreferences("VLCApplication", 0);
        return settings.getString("netIDFd", "false");
    }

    public static Context getAppContext() {
        return sInstance;
    }

    public ApiService getApiService() {
        return apiService;
    }

    public static Resources getAppResources() {
        if (sInstance == null) return null;
        return sInstance.getResources();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        System.gc();
    }

    // add Activity
    public void addActivity(Activity activity) {
        mList.add(activity);
    }

    //关闭每一个list内的activity
    public void exit() {
        try {
            for (Activity activity : mList) {
                if (activity != null)
                    activity.finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }

    /**
     * 获取当前activity
     *
     * @return
     */
    public static Activity getCurrentActivity() {
        Class activityThreadClass = null;
        try {
            activityThreadClass = Class.forName("android.app.ActivityThread");
            Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null);
            Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
            activitiesField.setAccessible(true);
            Map activities = (Map) activitiesField.get(activityThread);
            for (Object activityRecord : activities.values()) {
                Class activityRecordClass = activityRecord.getClass();
                Field pausedField = activityRecordClass.getDeclaredField("paused");
                pausedField.setAccessible(true);
                if (!pausedField.getBoolean(activityRecord)) {
                    Field activityField = activityRecordClass.getDeclaredField("activity");
                    activityField.setAccessible(true);
                    Activity activity = (Activity) activityField.get(activityRecord);
                    return activity;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 判断应用是否是在后台
     */
    public static boolean isAppBackground(Context context) {
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(KEYGUARD_SERVICE);

        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (TextUtils.equals(appProcess.processName, context.getPackageName())) {
                return appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND;
            }
        }
        return false;
    }
}
