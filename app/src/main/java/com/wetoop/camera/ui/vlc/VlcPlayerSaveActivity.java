package com.wetoop.camera.ui.vlc;

import android.app.Activity;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.wetoop.cameras.R;

import java.io.IOException;

/**
 * Created by User on 2017/8/24.
 */

public class VlcPlayerSaveActivity extends Activity{
    private android.media.MediaPlayer mediaPlayer;
    private SurfaceView surfaceView;
    private SeekBar seekBar;
    private int currentPosition;
    private boolean isPlaying;
    private ImageView playButton,back;
    private String path="";
    private RelativeLayout title,seekBarR,seekBarTime,button_playR;
    private TextView textView_title,nowTimeText,totalTimeText;
    private View parentView;
    private boolean backStop = false;
    private Uri uri;
    /*************
     * Activity
     *************/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vlc_player_save);
        parentView = this.getLayoutInflater().inflate(R.layout.activity_vlc_player_save, null);
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
        }//4.4以下没有做
        systemUiVisibility |= flags;
        this.getWindow().getDecorView().setSystemUiVisibility(systemUiVisibility);

        textView_title = (TextView)findViewById(R.id.textView_title);
        title = (RelativeLayout)findViewById(R.id.back);
        seekBarR = (RelativeLayout)findViewById(R.id.seekBarR);
        seekBarTime = (RelativeLayout)findViewById(R.id.seekBarTime);
        uri = Uri.parse("android.resource:"  + R.raw.reset_4g);
        textView_title.setText("4G版本重新设置WiFi");
        surfaceView = (SurfaceView) findViewById(R.id.sv);
        surfaceView.setVisibility(View.GONE);
        playButton = (ImageView)findViewById(R.id.button_play);
        button_playR = (RelativeLayout)findViewById(R.id.button_playR);
        nowTimeText = (TextView)findViewById(R.id.nowTime);
        totalTimeText = (TextView)findViewById(R.id.totalTime);
        mediaPlayer = new MediaPlayer();
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int process = seekBar.getProgress();
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.seekTo(process);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                if(!backStop) {
                    int nowTime = Math.round(mediaPlayer.getCurrentPosition() / 1000);
                    String nowTimeStr = String.format("%s%02d:%02d", "当前时间 ", nowTime / 60, nowTime % 60);
                    nowTimeText.setText(nowTimeStr);
                }
            }
        });
        back = (ImageView) findViewById(R.id.back_image);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backStop = true;
                stop();
                mediaPlayer.release();
                finish();
            }
        });
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        double width = metric.widthPixels;
        double high = width*0.6;
        RelativeLayout.LayoutParams LayoutParamsBig = new RelativeLayout.LayoutParams((new Double(width)).intValue(), (new Double(high)).intValue());
        LayoutParamsBig.addRule(RelativeLayout.CENTER_IN_PARENT);
        surfaceView.setVisibility(View.VISIBLE);
        surfaceView.setLayoutParams(LayoutParamsBig);
        mediaPlayerInit();
        full(true);
        play(0);

    }
    private void mediaPlayerInit(){
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                /**
                 * 当点击手机上home键（或其他使SurfaceView视图消失的键）时，调用该方法，获取到当前视频的播放值，currentPosition。
                 * 并停止播放。
                 */
                if(!backStop) {
                    System.out.println("surfaceDestroyed");
                    currentPosition = mediaPlayer.getCurrentPosition();
                    stop();
                }
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                /**
                 * 当重新回到该视频应当视图的时候，调用该方法，获取到currentPosition，并从该currentPosition开始继续播放。
                 */

                if (currentPosition > 0) {
                    System.out.println("surfaceCreated,,"+currentPosition);
                    play(currentPosition);
                }else{
                    play(0);
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format,
                                       int width, int height) {
            }
        });
        button_playR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying) {
                    playButton.setImageResource(R.mipmap.galaeye_record_play);
                    pause();
                    isPlaying = false;
                } else {
                    playButton.setImageResource(R.mipmap.galaeye_record_pause);
                    isPlaying = true;
                    if (mediaPlayer.getCurrentPosition() > 0) {
                        mediaPlayer.start();
                        new Thread(){
                            public void run() {
                                isPlaying = true;
                                while (isPlaying) {
                                    try {
                                        int position = mediaPlayer.getCurrentPosition();
                                        seekBar.setProgress(position);
                                        if (position == mediaPlayer.getDuration()) {
                                            Message msg = handler.obtainMessage();
                                            msg.what = 1;
                                            handler.sendMessage(msg);
                                        }
                                    }catch (Exception e){
                                    }
                                    try {
                                        Thread.sleep(500);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }

                            };
                        }.start();
                    }else if(mediaPlayer.getCurrentPosition() == mediaPlayer.getDuration()){
                        replay();
                    }
                }
            }
        });
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig){
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        double width = metric.widthPixels;
        double high = width*0.6;
        RelativeLayout.LayoutParams LayoutParamsBig = new RelativeLayout.LayoutParams((new Double(width)).intValue(), (new Double(high)).intValue());
        LayoutParamsBig.addRule(RelativeLayout.CENTER_IN_PARENT);
        surfaceView.setLayoutParams(LayoutParamsBig);
        super.onConfigurationChanged(newConfig);
    }
    //重播
    private void replay() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(0);
        } else {
            play(0);
        }
    }
    //停止
    private void stop() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(0);
            mediaPlayer.stop();
        }
    }
    //暂停
    private void pause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        } else {
            mediaPlayer.start();
        }
    }
    //播放
    private void play(final int currentPosition) {
        try {
            mediaPlayer.reset();
            mediaPlayer = MediaPlayer.create(VlcPlayerSaveActivity.this,R.raw.reset_4g);//读取视频
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDisplay(surfaceView.getHolder());//设置屏幕
            // TODO Auto-generated method stub
            try {
                mediaPlayer.prepare();
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalStateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                @Override
                public void onPrepared(MediaPlayer mp) {
                    mediaPlayer.start();
                    int max = mediaPlayer.getDuration();
                    playButton.setImageResource(R.mipmap.galaeye_record_pause);
                    seekBar.setMax(max);
                    mediaPlayer.seekTo(currentPosition);
                    int totalTime = Math.round(mediaPlayer.getDuration()/1000);
                    String totalTimeStr = String.format("%s%02d:%02d","",totalTime/60,totalTime%60);
                    totalTimeText.setText(totalTimeStr);
                    Log.d("VlcPlayerSaveActivity:","文件时间："+mediaPlayer.getDuration());
                    Log.d("VlcPlayerSaveActivity:","视频高度："+mediaPlayer.getVideoHeight());
                    Log.d("VlcPlayerSaveActivity:","视频宽度："+mediaPlayer.getVideoWidth());
                    new Thread(){
                        public void run() {
                            isPlaying = true;
                            while (isPlaying) {
                                if(!backStop) {
                                    int position = mediaPlayer.getCurrentPosition();
                                    seekBar.setProgress(position);
                                    if (position == mediaPlayer.getDuration()) {
                                        Message msg = handler.obtainMessage();
                                        msg.what = 1;
                                        handler.sendMessage(msg);
                                    }
                                }
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                        };
                    }.start();
                }
            });

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    playButton.setEnabled(true);
                }
            });

            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {

                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    playButton.setEnabled(true);
                    return false;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    isPlaying = false;
                    playButton.setImageResource(R.mipmap.galaeye_record_play);
                    break;
            }
        }
    };

    private int check = 0;
    private int count_down = 0, count_up = 0;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            count_down++;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            count_up++;
        }
        if (count_down == 1 && count_up == 1) {
            if (check == 0) {
                check = 1;
                title.setVisibility(View.INVISIBLE);
                seekBarR.setVisibility(View.INVISIBLE);
                seekBarTime.setVisibility(View.INVISIBLE);
                count_down = 0;
                count_up = 0;
            } else if (check == 1) {
                check = 0;
                title.setVisibility(View.VISIBLE);
                seekBarR.setVisibility(View.VISIBLE);
                seekBarTime.setVisibility(View.VISIBLE);
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
            backStop = true;
            isPlaying = false;
            stop();
            mediaPlayer.release();
            finish();
            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }
}