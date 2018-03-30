package com.wetoop.camera.ui.vlc;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wetoop.camera.App;
import com.wetoop.camera.tools.PermissionUtil;
import com.wetoop.camera.tools.UlawEncoderInputStream;
import com.wetoop.camera.service.VideoService;
import com.wetoop.camera.CameraJni;
import com.wetoop.cameras.R;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by User on 2017/7/4.
 */

public class VideoActivity extends Activity implements IVLCVout.Callback {
    // display surface
    private SurfaceHolder surfaceHolder;

    // media player
    private LibVLC libvlc;
    private MediaPlayer mMediaPlayer = null;
    private int mVideoWidth;
    private int mVideoHeight;
    private final static int VideoSizeChanged = -1;
    public String filePath = "rtsp://admin:admin@192.168.0.100:554";
    private View parentView;

    private static AudioRecord mRecord;
    // 音频获取源
    private int audioSource = MediaRecorder.AudioSource.MIC;
    // 设置音频采样率，44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    private static int sampleRateInHz = 8000;// 44100;32000
    // 设置音频的录制的声道CHANNEL_IN_STEREO为双声道，CHANNEL_CONFIGURATION_MONO为单声道
    private static int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;// AudioFormat.CHANNEL_IN_STEREO;
    // 音频数据格式:PCM 16位每个样本。保证设备支持。PCM 8位每个样本。不一定能得到设备支持。
    private static int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    // 音频大小
    public static int bufSize=0;
    private  int readsize = 0;

    private SurfaceView surfaceView = null;
    private TextView mTextTitle;
    //private Button snapShot;//截图
    private ProgressBar pb;
    private Chronometer chronometerTimeLandscape,chronometerTimePortrait;
    private RelativeLayout back,talkingR,media_title_landscape,media_title_portrait,galaeye_fullscreen;
    private ImageView talkingImage,galaeye_fullscreenImageView;
    private String snStr,netId,deviceName,admin_pwd,devicePwd,joined,pathUri;
    private boolean isLongClick=false,isSending=false,manifest=false,isRuning;
    private Media m;
    private IVLCVout vout;
    private boolean isStop=false;
    private int check = 0;
    private int count_down = 0, count_up = 0;
    /*************
     * Activity
     *************/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        parentView = this.getLayoutInflater().inflate(R.layout.activity_video, null);

        int systemUiVisibility = parentView.getSystemUiVisibility();
        int flags = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                flags = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            }else{
                flags = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN; // hide status bar
            }
        }
        systemUiVisibility |= flags;
        this.getWindow().getDecorView().setSystemUiVisibility(systemUiVisibility);
        setupView();

    }

    @Override
    protected void onPause() {
        super.onPause();
        //releasePlayer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * 初始化组件
     */
    private void setupView() {
        pb = (ProgressBar) findViewById(R.id.probar);
        surfaceView = (SurfaceView) findViewById(R.id.main_surface);
        surfaceHolder = surfaceView.getHolder();
        mTextTitle = (TextView) findViewById(R.id.textView_title);
        talkingR = (RelativeLayout)findViewById(R.id.talkingR);
        talkingImage = (ImageView)findViewById(R.id.talkingImage);
        Intent intent = getIntent();
        if(intent.getStringExtra("sn")!=null){
            snStr = intent.getStringExtra("sn");
            mTextTitle.setText(snStr);
        }
        if(intent.getStringExtra("netId")!=null){
            netId = intent.getStringExtra("netId");
        }
        if(intent.getStringExtra("deviceName")!=null){
            deviceName = intent.getStringExtra("deviceName");
        }
        if(intent.getStringExtra("devicePwd")!=null){
            devicePwd = intent.getStringExtra("devicePwd");
        }
        if(intent.getStringExtra("admin_pwd")!=null){
            admin_pwd = intent.getStringExtra("admin_pwd");
        }

        chronometerTimeLandscape = (Chronometer) this.findViewById(R.id.chronometerTimeLandscape);//计时器
        chronometerTimePortrait = (Chronometer) this.findViewById(R.id.chronometerTimePortrait);//计时器
        chronometerTimeLandscape.setBase(SystemClock.elapsedRealtime());
        chronometerTimePortrait.setBase(SystemClock.elapsedRealtime());
        media_title_landscape = (RelativeLayout) findViewById(R.id.media_title_landscape);
        media_title_portrait = (RelativeLayout) findViewById(R.id.media_title_portrait);
        galaeye_fullscreen = (RelativeLayout) findViewById(R.id.galaeye_fullscreen);
        galaeye_fullscreenImageView = (ImageView) findViewById(R.id.galaeye_fullscreenImageView);
        back = (RelativeLayout)findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(VideoActivity.this, VideoService.class);
                intent1.setPackage(getPackageName());
                VideoActivity.this.stopService(intent1);

                App app = (App)getApplication();
                int auth = CameraJni.tcpAuthResult(app.getVideoPort(),true);
                int authResult = CameraJni.tcpResult(app.getVideoPort(),true);
                if(!(auth == -1 || authResult == 0 || authResult == -10))
                    CameraJni.tcpRemove(app.getVideoPort(),false);
                CameraJni.tcpStop(false);
                app.setTcpStop(1);
                app.setTcpStart("false");
                releasePlayer();
                finish();
            }
        });
        surfaceView.setOnTouchListener(new VideoActivity.MyClickListener());
        surfaceView.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {
                System.out.println("长按屏幕");
                showContacts();
                if(manifest){
                    App app = (App)getApplication();
                    //videoService.wrapperThread(app,"true");
                    //之后最好把下面new的这东西删了，就只调用wrapperThread这个方法来获取新的接口，通话结束之后再remove掉
                    Intent intent2 = new Intent(VideoActivity.this, VideoService.class);
                    intent2.setPackage(getPackageName());
                    intent2.putExtra("netId",netId);
                    intent2.putExtra("sn",snStr);
                    intent2.putExtra("usernameStr",app.getLoginNetId());
                    intent2.putExtra("userpawStr",app.getLoginNetToken());
                    intent2.putExtra("node",app.getNode());
                    intent2.putExtra("audioFrom","true");
                    VideoActivity.this.startService(intent2);

                    talkingImage.setVisibility(View.VISIBLE);
                    talkingR.setVisibility(View.VISIBLE);
                    isLongClick = true;
                    isSending = true;
                    StartVoiceThread();
                }
                return true;
            }
        });
        surfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (check == 0) {
                    check = 1;
                    media_title_portrait.setVisibility(View.INVISIBLE);
                    media_title_landscape.setVisibility(View.INVISIBLE);
                    back.setVisibility(View.INVISIBLE);
                    galaeye_fullscreen.setVisibility(View.GONE);
                    count_down = 0;
                    count_up = 0;
                } else if (check == 1) {
                    check = 0;
                    back.setVisibility(View.VISIBLE);
                    galaeye_fullscreen.setVisibility(View.VISIBLE);
                    Display display = getWindowManager().getDefaultDisplay();
                    int width = display.getWidth();
                    int height = display.getHeight();
                    if (width > height) {
                        media_title_landscape.setVisibility(View.INVISIBLE);
                        media_title_portrait.setVisibility(View.VISIBLE);
                    } else {
                        media_title_landscape.setVisibility(View.VISIBLE);
                        media_title_portrait.setVisibility(View.INVISIBLE);
                    }
                    count_down = 0;
                    count_up = 0;
                }
            }
        });
        galaeye_fullscreenImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(VideoActivity.this.getResources().getConfiguration().orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE||
                        VideoActivity.this.getRequestedOrientation()== ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){//横屏
                    VideoActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    galaeye_fullscreenImageView.setImageDrawable(getResources().getDrawable(R.mipmap.galaeye_btn_enlarge));
                }else if(VideoActivity.this.getResources().getConfiguration().orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT||
                        VideoActivity.this.getRequestedOrientation()== ActivityInfo.SCREEN_ORIENTATION_PORTRAIT){//竖屏
                    VideoActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    galaeye_fullscreenImageView.setImageDrawable(getResources().getDrawable(R.mipmap.galaeye_btnnarrow));
                }
            }
        });
        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();
        if (width > height) {
            media_title_landscape.setVisibility(View.INVISIBLE);
            media_title_portrait.setVisibility(View.VISIBLE);
        } else {
            media_title_landscape.setVisibility(View.VISIBLE);
            media_title_portrait.setVisibility(View.INVISIBLE);
        }
        full(true);
        App app = (App)getApplication();
        String pathUri = "rtsp://admin:" + admin_pwd + "@127.0.0.1:" + String.valueOf(app.getVideoPortConn()) + "/Streaming/Channels/102";
        createPlayer(pathUri);
    }

    public void showContacts() {

        if (ActivityCompat.checkSelfPermission(VideoActivity.this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat
                    .requestPermissions(VideoActivity.this, new String[]{Manifest.permission.RECORD_AUDIO},
                            1);
        }else{
            bufSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig,
                    audioFormat);
            mRecord = new AudioRecord(audioSource, sampleRateInHz, channelConfig,
                    audioFormat, 640*3);
            manifest=true;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode==1){
            if (PermissionUtil.verifyPermissions(grantResults)) {
                bufSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig,
                        audioFormat);
                mRecord = new AudioRecord(audioSource, sampleRateInHz, channelConfig,
                        audioFormat, 640*3);
                manifest=true;
            }
        }else{
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    public void StartVoiceThread(){
        Thread EncoderThread1 = new Thread(new Runnable(){
            @Override
            public void run(){
                App app = (App)getApplication();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                boolean loop = true;
                while (loop) {
                    if (app.getBroadcast() == 1) {
                        app.setBroadcast(0);
                        loop = false;
                        CameraJni.voiceTalkOpen(app.getAdminName(), devicePwd, "127.0.0.1", app.getAudioPortConn());
                        isRuning = true;
                        mRecord.startRecording();
                        byte[] tempByteData = new byte[640 * 2];
                        byte[] tempByteAdd = new byte[]{0x56, 0x56, 0x50, (byte) 0x99, 0x00, 0x00, 0x01, 0x40};
                        while (isRuning) {
                            readsize = mRecord.read(tempByteData, 0, 640 * 2);
                            InputStream is = new ByteArrayInputStream(tempByteData);
                            UlawEncoderInputStream uis = null;
                            try {
                                uis = new UlawEncoderInputStream(is, 0);
                                byte buff[] = new byte[640];
                                int len = uis.read(buff);
                                while (len > 0) {
                                    CameraJni.voiceTalkWrite(buff);
                                    len = uis.read(buff);
                                }
                            } catch (Exception e) {
                            } finally {
                                try {
                                    uis.close();
                                } catch (Exception e) {
                                }
                            }
                            long v = 0;
                            // 将 buffer 内容取出，进行平方和运算
                            for (int i = 0; i < tempByteData.length; i++) {
                                v += tempByteData[i] * tempByteData[i];
                            }
                            // 平方和除以数据总长度，得到音量大小。
                            double mean = v / (double) readsize;
                            double volume = 10 * Math.log10(mean);
                            if (volume < 34.5) {
                                Message msg = handlervoice.obtainMessage();
                                msg.what = 1;
                                handlervoice.sendMessage(msg);
                            } else if (volume >= 34.5 && volume < 36) {
                                Message msg = handlervoice.obtainMessage();
                                msg.what = 2;
                                handlervoice.sendMessage(msg);
                            } else if (volume >= 36 && volume < 38) {
                                Message msg = handlervoice.obtainMessage();
                                msg.what = 3;
                                handlervoice.sendMessage(msg);
                            } else if (volume >= 38 && volume < 45) {
                                Message msg = handlervoice.obtainMessage();
                                msg.what = 4;
                                handlervoice.sendMessage(msg);
                            } else if (volume >= 45) {
                                Message msg = handlervoice.obtainMessage();
                                msg.what = 5;
                                handlervoice.sendMessage(msg);
                            }
                        }
                        CameraJni.voiceTalkClose();
                        Message msg = handlervoice.obtainMessage();
                        msg.what = 6;
                        handlervoice.sendMessage(msg);
                    }
                }
            }
        });
        EncoderThread1.start();
    }
    private Handler handlervoice = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    talkingImage.setImageResource(R.mipmap.galaeye_audio1);
                    break;
                case 2:
                    talkingImage.setImageResource(R.mipmap.galaeye_audio2);
                    break;
                case 3:
                    talkingImage.setImageResource(R.mipmap.galaeye_audio3);
                    break;
                case 4:
                    talkingImage.setImageResource(R.mipmap.galaeye_audio4);
                    break;
                case 5:
                    talkingImage.setImageResource(R.mipmap.galaeye_audio5);
                    break;
                case 6:
                    talkingImage.setImageResource(R.mipmap.galaeye_audio1);
                    //talkingImage.setVisibility(View.GONE);
                    break;
                case 7:
                    App app = (App)getApplication();
                    int tcpResult = CameraJni.tcpResult(app.getAudioPort(),true);
                    if(tcpResult != 0)
                        CameraJni.tcpRemove(app.getAudioPort(),false);
                    /*CameraJni.tcpStop(false);
                    app.setTcpStop(1);*/
                    break;
            }
        }
    };

    class MyClickListener implements View.OnTouchListener {
        public boolean onTouch(View v, MotionEvent event) {

            if (isLongClick)
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        talkingImage.setVisibility(View.GONE);
                        talkingR.setVisibility(View.GONE);
                        //System.out.println("抬起。。。。。。。。。。。。。。。。");
                        isLongClick = false;
                        isRuning=false;
                        isSending=false;
                        //tsClose();
                        //audio_image.setVisibility(View.GONE);
                        Message msg = handlervoice.obtainMessage();
                        msg.what = 7;
                        handlervoice.sendMessage(msg);
                        break;
                    default:
                        Message msg1 = handlervoice.obtainMessage();
                        msg1.what = 6;
                        handlervoice.sendMessage(msg1);
                        break;
                }
            return false;
        }
    }
    /**
     * 路径是否存在  不存在则创建
     */
    private void pathIsExist()
    {
        File file = new File(getSDPath() + "/FGCamera/") ;
        if(!file.exists())
            file.mkdirs();

    }
    //获取sd卡路径
    public String getSDPath() {
        File sdDir = null;
        //boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);//判断sd卡是否存在
        //if (sdCardExist) {
        //sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        sdDir = VideoActivity.this.getFilesDir();
        File destDir = new File(sdDir + "/FGCamera");
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        //}
        return sdDir.toString();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setSize(mVideoWidth, mVideoHeight);
        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();
        if (width > height) {
            media_title_landscape.setVisibility(View.INVISIBLE);
            media_title_portrait.setVisibility(View.VISIBLE);
        } else {
            media_title_landscape.setVisibility(View.VISIBLE);
            media_title_portrait.setVisibility(View.INVISIBLE);
        }
    }

    private void setSize(int width, int height) {
        mVideoWidth = width;
        mVideoHeight = height;
        if (mVideoWidth * mVideoHeight <= 1)
            return;

        if(surfaceHolder == null || surfaceView == null)
            return;

        // get screen size
        WindowManager winManager=(WindowManager)getSystemService(Context.WINDOW_SERVICE);
        int w = winManager.getDefaultDisplay().getWidth();
        int h = winManager.getDefaultDisplay().getHeight();

        // getWindow().getDecorView() doesn't always take orientation into
        // account, we have to correct the values
        boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        if (w > h && isPortrait || w < h && !isPortrait) {
            int i = w;
            w = h;
            h = i;
        }

        float videoAR = (float) mVideoWidth / (float) mVideoHeight;
        float screenAR = (float) w / (float) h;

        if (screenAR < videoAR)
            h = (int) (w / videoAR);
        else
            w = (int) (h * videoAR);

        // force surface buffer size
        surfaceHolder.setFixedSize(mVideoWidth, mVideoHeight);

        // set display size
        ViewGroup.LayoutParams lp = surfaceView.getLayoutParams();
        lp.width = w;
        lp.height = h;
        surfaceView.setLayoutParams(lp);
        surfaceView.invalidate();
    }

    /*************
     * Player
     *************/

    private void createPlayer(String pathUri) {
        try {
            // Create LibVLC
            // TODO: make this more robust, and sync with audio demo
            ArrayList<String> options = new ArrayList<String>();
            //options.add("--subsdec-encoding <encoding>");
            //options.add("--aout=opensles");
            //options.add("--audio-time-stretch"); // time stretching
            options.add("--rtsp-tcp");
            //options.add("--rtsp-user="+"admin");
            //options.add("--rtsp-pwd="+"admin");
            options.add("-vvv"); // verbosity
            libvlc = new LibVLC(VideoActivity.this,options);
            //libvlc.setOnHardwareAccelerationError(this);
            //surfaceHolder.setKeepScreenOn(true);

            // Create media player
            mMediaPlayer = new MediaPlayer(libvlc);
                    /*mMediaPlayer.getVLCVout().setVideoSurface(surfaceView.getHolder().getSurface(), surfaceView.getHolder());
                    //播放前还要调用这个方法
                    mMediaPlayer.getVLCVout().attachViews();*/

            mMediaPlayer.setEventListener(mPlayerListener);

            // Set up video output
            vout = mMediaPlayer.getVLCVout();
            vout.setVideoView(surfaceView);
            //vout.setSubtitlesView(mSurfaceSubtitles);
            vout.addCallback(VideoActivity.this);
            vout.attachViews();
                    /*Message msg = handler.obtainMessage();
                    msg.what = 1;
                    handler.sendMessage(msg);*/
        } catch (Exception e) {
            Toast.makeText(VideoActivity.this, "创建播放器错误", Toast.LENGTH_LONG).show();
        }

        Uri uri = Uri.parse(pathUri);
        m = new Media(libvlc, uri);
        mMediaPlayer.setMedia(m);
        mMediaPlayer.play();
        surfaceView.setVisibility(View.VISIBLE);

        /*Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    if (!mMediaPlayer.isPlaying() && !isStop) {
                        Message msg = new Message();
                        msg.what = 1;
                        mHandler_code.sendMessage(msg);
                    }
                }catch (Exception e){
                }
            }
        };//检查播放是否暂停
        timer.schedule(task, 1000 * 4); //3秒后*/

    }

    // TODO: handle this cleaner
    private void releasePlayer() {
        if (libvlc == null)
            return;
        if(vout == null)
            return;
        vout.removeCallback(this);
        vout.detachViews();
        //mMediaPlayer.stop();
        //mMediaPlayer.release();
        surfaceHolder = null;
        libvlc.release();
        libvlc = null;
        //m.release();
        /*if(mMediaPlayer!=null){
            mMediaPlayer.stop();
            mMediaPlayer.release();
        }*/
        mVideoWidth = 0;
        mVideoHeight = 0;

        new Thread(new Runnable() {
            @Override
            public void run() {
                /*try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
                Media media = mMediaPlayer.getMedia();
                if (media != null) {
                    media.setEventListener(null);
                    mMediaPlayer.setEventListener(null);
                    mMediaPlayer.setMedia(null);
                    media.release();
                    mMediaPlayer.stop();
                    mMediaPlayer.release();
                }
            }
        }).start();

    }

    /*************
     * Events
     *************/

    private MediaPlayer.EventListener mPlayerListener = new MyPlayerListener(this);

    @Override
    public void onNewLayout(IVLCVout vlcVout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {
        if (width * height == 0)
            return;
        // store video size
        mVideoWidth = width;
        mVideoHeight = height;
        setSize(mVideoWidth, mVideoHeight);
    }

    @Override
    public void onSurfacesCreated(IVLCVout vout) {

    }

    @Override
    public void onSurfacesDestroyed(IVLCVout vout) {

    }

    @Override
    public void onHardwareAccelerationError(IVLCVout vlcVout) {
        Log.e("onEvent", "onHardwareAccelerationError" );
        isStop=true;
        Message msg = new Message();
        msg.what = 2;
        mHandler_code.sendMessage(msg);
    }

    private class MyPlayerListener implements MediaPlayer.EventListener {
        private WeakReference<VideoActivity> mOwner;

        public MyPlayerListener(VideoActivity owner) {
            mOwner = new WeakReference<VideoActivity>(owner);
        }

        @Override
        public void onEvent(MediaPlayer.Event event) {
            VideoActivity player = mOwner.get();

            switch(event.type) {
                case MediaPlayer.Event.EndReached:
                    Log.e("onEvent", "onEvent: EndReached" );
                    //releasePlayer();
                    //pb.setVisibility(View.VISIBLE);

                    break;
                case MediaPlayer.Event.Playing:
                    Log.e("onEvent", "onEvent: Playing" );
                    Message msg1 = new Message();
                    msg1.what = 1;
                    mHandler_code.sendMessage(msg1);
                    break;
                case MediaPlayer.Event.Paused:
                    Log.e("onEvent", "onEvent: Paused" );
                    break;
                case MediaPlayer.Event.Stopped:
                    Log.e("onEvent", "onEvent: Stopped" );
                    isStop=true;
                    Message msg = new Message();
                    msg.what = 2;
                    mHandler_code.sendMessage(msg);
                    break;
                default:
                    break;
            }
        }
    }
    Handler mHandler_code = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    surfaceView.setVisibility(View.VISIBLE);
                    pb.setVisibility(View.GONE);
                    chronometerTimePortrait.start();
                    chronometerTimeLandscape.start();
                    break;
                case 2:
                    App app = (App)getApplication();
                    int tcpResult = CameraJni.tcpResult(app.getVideoPort(),false);
                    String hintTip = "";
                    if(tcpResult <= 0) switch (tcpResult) {
                        case ResultConstant.FORWARD_RESULT_PEER_OFFLINE:
                            hintTip = "设备不在线";
                            break;
                        case ResultConstant.FORWARD_RESULT_CONN_LOST:
                            hintTip = "连接被关闭";
                            break;
                        case ResultConstant.FORWARD_RESULT_CERT_ERROR:
                            hintTip = String.format("证书校验错误 (%ld)", CameraJni.tcpErrorResult(app.getVideoPort(),false));
                            break;
                        case ResultConstant.FORWARD_RESULT_KICKED_OUT:
                            hintTip = "别处已登录，连接被关闭";
                            break;
                        case ResultConstant.FORWARD_RESULT_CONN_CLOSE:
                            hintTip = "本机连接断开";
                            break;
                    }
                    int auth_result = CameraJni.tcpAuthResult(app.getVideoPort(),false);
                    if(auth_result == 401)
                        hintTip = "访问密码错误";
                    if(auth_result == -1)
                        hintTip = "获取信息出错";
                    Toast.makeText(VideoActivity.this, hintTip, Toast.LENGTH_SHORT).show();
                    Intent intent1 = new Intent(VideoActivity.this, VideoService.class);
                    intent1.setPackage(getPackageName());
                    VideoActivity.this.stopService(intent1);
                    if(!(auth_result == -1 || tcpResult == 0 || tcpResult == -10))
                        CameraJni.tcpRemove(app.getVideoPort(),false);
                    CameraJni.tcpStop(false);
                    app.setTcpStop(1);
                    releasePlayer();
                    finish();
                    break;
            }
        }
    };

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            //isSending=false;
            count_down++;
            //System.out.println("ACTION_DOWN");
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            //isSending=true;
            count_up++;
            //System.out.println("ACTION_UP");
        }
        if (count_down == 1 && count_up == 1) {
            if (check == 0) {
                //full(true);
                check = 1;
                media_title_portrait.setVisibility(View.INVISIBLE);
                media_title_landscape.setVisibility(View.INVISIBLE);
                back.setVisibility(View.INVISIBLE);
                galaeye_fullscreen.setVisibility(View.GONE);
                count_down = 0;
                count_up = 0;
            } else if (check == 1) {
                //full(false);
                check = 0;
                back.setVisibility(View.VISIBLE);
                galaeye_fullscreen.setVisibility(View.VISIBLE);
                Display display = getWindowManager().getDefaultDisplay();
                int width = display.getWidth();
                int height = display.getHeight();
                if (width > height) {
                    media_title_landscape.setVisibility(View.INVISIBLE);
                    media_title_portrait.setVisibility(View.VISIBLE);
                } else {
                    media_title_landscape.setVisibility(View.VISIBLE);
                    media_title_portrait.setVisibility(View.INVISIBLE);
                }
                count_down = 0;
                count_up = 0;
            }
        }
        return super.onTouchEvent(event);
    }
    //用于是否要显示状态栏
    private void full(boolean enable) {
        if (enable) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setAttributes(lp);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else {
            WindowManager.LayoutParams attr = getWindow().getAttributes();
            attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(attr);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            releasePlayer();
            Intent intent1 = new Intent(VideoActivity.this, VideoService.class);
            intent1.setPackage(getPackageName());
            VideoActivity.this.stopService(intent1);
            App app = (App)getApplication();
            int auth = CameraJni.tcpAuthResult(app.getVideoPort(),true);
            int authResult = CameraJni.tcpResult(app.getVideoPort(),true);
            if(!(auth == -1 || authResult == 0 || authResult == -10))
                CameraJni.tcpRemove(app.getVideoPort(),false);
            CameraJni.tcpStop(false);
            app.setTcpStop(1);
            app.setTcpStart("false");
            finish();
            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }
}
